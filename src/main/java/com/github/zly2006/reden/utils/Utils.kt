package com.github.zly2006.reden.utils

import com.github.zly2006.reden.Reden
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import net.fabricmc.api.EnvType
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.MinecraftServer
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.Heightmap
import net.minecraft.world.World

lateinit var server: MinecraftServer

fun Vec3d.toBlockPos(): BlockPos {
    return BlockPos.ofFloored(this)
}

fun PlayerEntity.sendMessage(s: String) {
    sendMessage(Text.literal(s))
}

fun World.setBlockNoPP(pos: BlockPos, state: BlockState, flags: Int) {
    if (isClient) {

    }
    val stateBefore = getBlockState(pos)
    if (stateBefore.hasBlockEntity()) {
        removeBlockEntity(pos)
    }
    getChunk(pos).run { getSection(getSectionIndex(pos.y)) }
        .setBlockState(pos.x and 15, pos.y and 15, pos.z and 15, state, false)
    getChunk(pos).run {
        this.heightmaps[Heightmap.Type.MOTION_BLOCKING]!!.trackUpdate(pos.x and 15, pos.y, pos.z and 15, state)
        this.heightmaps[Heightmap.Type.MOTION_BLOCKING_NO_LEAVES]!!.trackUpdate(pos.x and 15, pos.y, pos.z and 15, state)
        this.heightmaps[Heightmap.Type.OCEAN_FLOOR]!!.trackUpdate(pos.x and 15, pos.y, pos.z and 15, state)
        this.heightmaps[Heightmap.Type.WORLD_SURFACE]!!.trackUpdate(pos.x and 15, pos.y, pos.z and 15, state)
        setNeedsSaving(true)
    }
    if (this is ServerWorld) {
        chunkManager.markForUpdate(pos)
    }
    if (flags and Block.NOTIFY_LISTENERS != 0) {
        updateListeners(pos, getBlockState(pos), state, flags)
    }
}

val isClient: Boolean get() = FabricLoader.getInstance().environmentType == EnvType.CLIENT

object ResourceLoader {
    fun loadBytes(path: String): ByteArray? {
        return Reden::class.java.classLoader.getResourceAsStream(path)?.readAllBytes()
    }

    fun loadString(path: String): String {
        return loadBytes(path)!!.decodeToString()
    }

    fun loadStringOrNull(path: String): String? {
        return loadBytes(path)?.decodeToString()
    }

    fun loadTexture(path: String): Identifier {
        return Reden.identifier(path)
    }

    @JvmStatic
    fun loadLang(lang: String) =
        loadStringOrNull("assets/reden/lang/$lang.json")?.let {
            Json.decodeFromString(MapSerializer(String.serializer(), String.serializer()), it)
        }
}

val isSinglePlayerAndCheating: Boolean get() {
    infix fun Boolean?.and(other: Boolean?) = this ?: false && other ?: false
    return MinecraftClient.getInstance()?.let {
        (it.server?.isSingleplayer and it.player?.hasPermissionLevel(2))
    } == true
}

fun MutableText.red() = formatted(Formatting.RED)
