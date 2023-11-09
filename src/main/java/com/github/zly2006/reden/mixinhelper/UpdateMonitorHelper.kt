package com.github.zly2006.reden.mixinhelper

import com.github.zly2006.reden.access.PlayerData
import com.github.zly2006.reden.access.PlayerData.Companion.data
import com.github.zly2006.reden.carpet.RedenCarpetSettings
import com.github.zly2006.reden.malilib.iEVER_USED_UNDO
import com.github.zly2006.reden.mixinhelper.UpdateMonitorHelper.monitorSetBlock
import com.github.zly2006.reden.mixinhelper.UpdateMonitorHelper.playerStartRecording
import com.github.zly2006.reden.mixinhelper.UpdateMonitorHelper.playerStopRecording
import com.github.zly2006.reden.mixinhelper.UpdateMonitorHelper.popRecord
import com.github.zly2006.reden.mixinhelper.UpdateMonitorHelper.pushRecord
import com.github.zly2006.reden.mixinhelper.UpdateMonitorHelper.recordId
import com.github.zly2006.reden.mixinhelper.UpdateMonitorHelper.recording
import com.github.zly2006.reden.mixinhelper.UpdateMonitorHelper.undoRecords
import com.github.zly2006.reden.mixinhelper.UpdateMonitorHelper.undoRecordsMap
import com.github.zly2006.reden.utils.debugLogger
import com.github.zly2006.reden.utils.isClient
import com.github.zly2006.reden.utils.isDebug
import com.github.zly2006.reden.utils.server
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.block.BlockState
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.Entity
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.ClickEvent
import net.minecraft.text.HoverEvent
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.math.BlockPos

/**
 * # Undo
 *
 * This is the handler for undo feature.
 *
 * ## Players
 *
 * When players do some operation that is tracked by reden,
 * reden will create a backup (we call it `UndoRecord`, see [com.github.zly2006.reden.access.PlayerData.UndoRecord]) for it.
 * Player tracking starts at [playerStartRecording], and ends at [playerStopRecording].
 *
 * Then all changes will be recorded in the UndoRecord.
 * If the player wants to undo the operation, reden will restore the UndoRecord to the world.
 *
 * The id of the UndoRecord is unique (see [recordId]), and it will be used to identify the UndoRecord by [undoRecordsMap].
 *
 * ## Undo Record
 *
 * the current available UndoRecords are stored in [undoRecords].
 * It is a stack and the top is the current UndoRecord([recording]).
 *
 * ## Blocks
 *
 * Each time your world changes, reden will add the state before the change to the UndoRecord,
 * see [com.github.zly2006.reden.access.PlayerData.UndoRedoRecord.fromWorld].
 *
 * If you want to know how reden monitors block changes, see [monitorSetBlock].
 *
 * ## Entities
 *
 * For entities, we monitor all [net.minecraft.entity.data.TrackedData] changes.
 * see [com.github.zly2006.reden.mixin.undo.MixinDataTracker.beforeDataSet]
 *
 * ## Async changes
 *
 * (idk how to describe things like BE and scheduled tick that dont make changes immediately, just call them async changes)
 *
 * Primed TNTs, block events, scheduled ticks, they dont make changes immediately.
 *
 * So reden added a field [com.github.zly2006.reden.access.UndoableAccess.undoId] for them.
 *
 * When they are created, reden will assign them an undo id from [recording].
 *
 * When the async changes are applied, reden will check if the id is in the [undoRecordsMap].
 *
 * If it is, reden will push the specified UndoRecord to the stack by [pushRecord].
 * Then the game continues to process, making more changes.
 * And all changes will be recorded to the UndoRecord.
 * After the async changes are applied, reden will pop the UndoRecord from the stack by [popRecord].
 */
object UpdateMonitorHelper {
    class UndoRecordEntry(val id: Long, val record: PlayerData.UndoRecord?, val reason: String)
    private var recordId = 20060210L
    val undoRecordsMap: MutableMap<Long, PlayerData.UndoRecord> = HashMap()
    internal val undoRecords = mutableListOf<UndoRecordEntry>()
    @JvmStatic
    fun pushRecord(id: Long, reasonSupplier: () -> String): Boolean {
        val reason = if (isDebug) reasonSupplier() else ""
        debugLogger("[${undoRecords.size + 1}] id $id: push, $reason")
        return undoRecords.add(
            UndoRecordEntry(
                id,
                undoRecordsMap[id],
                reason
            )
        )
    }
    @JvmStatic
    fun popRecord(reasonSupplier: () -> String): UndoRecordEntry {
        val reason = if (isDebug) reasonSupplier() else ""
        debugLogger("[${undoRecords.size}] id ${undoRecords.last().id}: pop, $reason")
        if (reason != undoRecords.last().reason) {
            throw IllegalStateException("Cannot pop record with different reason: $reason != ${undoRecords.last().reason}")
        }
        return undoRecords.removeLast()
    }
    data class Changed(
        val record: PlayerData.UndoRecord,
        val pos: BlockPos
    )
    var lastTickChanged: MutableSet<Changed> = hashSetOf(); private set
    var thisTickChanged: MutableSet<Changed> = hashSetOf(); private set
    val recording: PlayerData.UndoRecord? get() = undoRecords.lastOrNull()?.record
    enum class LifeTime {
        PERMANENT,
        TICK,
        CHAIN,
        ONCE
    }

    @JvmStatic
    fun monitorSetBlock(world: ServerWorld, pos: BlockPos, blockState: BlockState) {
        debugLogger("id ${recording?.id ?: 0}: set$pos, ${world.getBlockState(pos)} -> $blockState")
        recording?.data?.computeIfAbsent(pos.asLong()) {
            recording!!.fromWorld(world, pos, true)
        }
        if (isClient && recording != null && !recording!!.notified
            && !iEVER_USED_UNDO.booleanValue) {
            // Send a notification that maybe he wants undo.
            if (recording!!.data.size > 2) {
                recording!!.notified = true
                val mc = MinecraftClient.getInstance()
                mc.player?.sendMessage(
                    Text.literal("Did you make it by mistake? Press Ctrl+Z to undo it!").formatted(Formatting.GOLD)
                        .append(Text.literal("Click here if you don't want to see this again.").setStyle(Style.EMPTY
                            .withColor(Formatting.GRAY)
                            .withClickEvent(ClickEvent(ClickEvent.Action.OPEN_URL, "reden:malilib:${iEVER_USED_UNDO.name}=true"))
                            .withHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("This is a feature provided by Reden Mod. Click to disable this notification.")))))
                )
            }
        }
        recording?.lastChangedTick = server.ticks
    }

    /**
     * 此函数有危险副作用
     *
     * 使用此函数将**立刻**产生缓存的副作用
     *
     * 此缓存可能在没有确认的情况下不经检查直接调用
     */
    private fun addRecord(
        cause: PlayerData.UndoRecord.Cause
    ): PlayerData.UndoRecord {
        if (undoRecords.size != 0) {
            throw IllegalStateException("Cannot add record when there is already one.")
        }
        val undoRecord = PlayerData.UndoRecord(
            id = recordId,
            lastChangedTick = server.ticks,
            cause = cause
        )
        undoRecordsMap[recordId] = undoRecord
        recordId++
        return undoRecord
    }

    internal fun removeRecord(id: Long) = undoRecordsMap.remove(id)

    @Suppress("unused")
    @JvmStatic
    fun playerStartRecording(player: ServerPlayerEntity) = playerStartRecording(player, PlayerData.UndoRecord.Cause.UNKNOWN)
    @JvmStatic
    fun playerStartRecording(
        player: ServerPlayerEntity,
        cause: PlayerData.UndoRecord.Cause
    ) {
        val playerView = player.data()
        if (!playerView.canRecord) return
        if (!playerView.isRecording) {
            playerView.isRecording = true
            val record = addRecord(cause)
            playerView.undo.add(record)
            pushRecord(record.id) { "player recording/${player.entityName}/$cause" }
        }
    }

    @JvmStatic
    fun playerStopRecording(player: ServerPlayerEntity) {
        val playerView = player.data()
        if (playerView.isRecording) {
            playerView.isRecording = false
            popRecord { "player recording/${player.entityName}/${recording?.cause}" }
            playerView.redo
                .onEach { removeRecord(it.id) }
                .clear()
            var sum = playerView.undo.map(PlayerData.UndoRecord::getMemorySize).sum()
            debugLogger("Undo size: $sum")
            if (RedenCarpetSettings.allowedUndoSizeInBytes >= 0) {
                while (sum > RedenCarpetSettings.allowedUndoSizeInBytes) {
                    removeRecord(playerView.undo.first().id)
                    playerView.undo.removeFirst()
                    debugLogger("Undo size: $sum, removing.")
                    sum = playerView.undo.map(PlayerData.UndoRecord::getMemorySize).sum()
                }
            }
        }
    }

    private fun playerQuit(player: ServerPlayerEntity) =
        player.data().undo.forEach { removeRecord(it.id) }

    @JvmStatic
    fun tryAddRelatedEntity(entity: Entity) {
        if (entity.noClip) return
        if (entity is ServerPlayerEntity) return
        if (!isInitializingEntity) {
            debugLogger("id ${recording?.id ?: 0}: add ${entity.uuid}, type ${entity.type.name}")
            recording?.entities?.computeIfAbsent(entity.uuid) {
                PlayerData.EntityEntryImpl(
                    entity.type,
                    NbtCompound().apply(entity::writeNbt),
                    entity.blockPos
                )
            }
        }
    }

    /**
     * starts at: [com.github.zly2006.reden.mixin.undo.MixinEntity.beforeEntitySpawn]
     *
     * ends at:   [com.github.zly2006.reden.mixin.undo.MixinServerWorld.afterSpawn]
     */
    @JvmField var isInitializingEntity = false

    @JvmStatic
    fun entitySpawned(entity: Entity) {
        if (entity is ServerPlayerEntity) return
        debugLogger("id ${recording?.id ?: 0}: spawn ${entity.uuid}, type ${entity.type.name}")
        recording?.entities?.putIfAbsent(entity.uuid, PlayerData.NotExistEntityEntry)
    }

    init {
        ServerPlayConnectionEvents.DISCONNECT.register { handler, _ -> playerQuit(handler.player) }
        ServerTickEvents.START_SERVER_TICK.register {
            lastTickChanged = thisTickChanged
            thisTickChanged = hashSetOf()
        }
    }
}
