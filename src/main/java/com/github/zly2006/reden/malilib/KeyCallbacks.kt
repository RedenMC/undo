package com.github.zly2006.reden.malilib

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.Sounds
import com.github.zly2006.reden.access.PlayerData.Companion.data
import com.github.zly2006.reden.access.ServerData.Companion.serverData
import com.github.zly2006.reden.network.Undo
import com.github.zly2006.reden.render.BlockBorder
import com.github.zly2006.reden.report.onFunctionUsed
import com.github.zly2006.reden.utils.red
import com.github.zly2006.reden.utils.sendMessage
import com.github.zly2006.reden.utils.toBlockPos
import com.github.zly2006.reden.utils.translateMessage
import fi.dy.masa.malilib.gui.GuiConfigsBase
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.network.PacketByteBuf
import net.minecraft.sound.SoundCategory
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.world.GameMode
import kotlin.random.Random

fun configureKeyCallbacks(mc: MinecraftClient) {
    REDEN_CONFIG_KEY.keybind.setCallback { _, _ ->
        mc.setScreen(GuiConfigs())
        true
    }
    var undoEasterEggLock = false
    UNDO_KEY.keybind.setCallback { _, _ ->
        if (undoEasterEggLock) {
            mc.player?.sendMessage(translateMessage("undo", "busy"))
            return@setCallback false
        }
        if (mc.serverData()?.featureSet?.contains("undo") != true) {
            mc.player?.sendMessage(LiteralText("Sorry, this server doesn't support undo.").red(), true)
            return@setCallback false
        }
        if (mc.interactionManager?.currentGameMode != GameMode.CREATIVE)
            return@setCallback false
        onFunctionUsed("undo")
        iEVER_USED_UNDO.booleanValue = true
        val playSound = Random.nextInt(100) < EASTER_EGG_RATE.integerValue
        if (playSound) {
            mc.world!!.playSound(
                mc.player,
                mc.player!!.blockPos,
                Sounds.THE_WORLD,
                SoundCategory.BLOCKS,
                1.0f,
                1.0f
            )
            undoEasterEggLock = true
            Thread {
                Thread.sleep(2000)
                undoEasterEggLock = false
                ClientPlayNetworking.send(Undo.id, PacketByteBufs.create().apply {
                    writeVarInt(0)
                })
            }.start()
        }
        else ClientPlayNetworking.send(Undo.id, PacketByteBufs.create().apply {
            writeVarInt(0)
        })
        true
    }
    REDO_KEY.keybind.setCallback { _, _ ->
        onFunctionUsed("redo")
        if (mc.interactionManager?.currentGameMode == GameMode.CREATIVE) {
            ClientPlayNetworking.send(Undo.id, PacketByteBufs.create().apply {
                writeVarInt(1)
            })
            true
        } else false
    }
    DEBUG_TAG_BLOCK_POS.keybind.setCallback { _, _ ->
        val pos = mc.crosshairTarget?.pos?.toBlockPos()
        if (pos != null) {
            val new = BlockBorder.tags.compute(pos.asLong()) { _, old ->
                when (old) {
                    3 -> 0
                    null -> 1
                    else -> old + 1
                }
            }
            mc.player?.sendMessage("OK $pos=$new")
            true
        } else false
    }
    DEBUG_PREVIEW_UNDO.keybind.setCallback { _, _ ->
        if (mc.interactionManager?.currentGameMode == GameMode.CREATIVE) {
            BlockBorder.tags.clear()
            val view = mc.server!!.playerManager.playerList[0].data()
            view.undo.lastOrNull()?.data?.keys?.forEach {
                BlockBorder.tags[it] = 1
            }
            return@setCallback true
        }
        return@setCallback false
    }
    DEBUG_VIEW_ALL_CONFIGS.keybind.setCallback { _, _ ->
        mc.setScreen(object : GuiConfigsBase(
            10,
            20,
            Reden.MOD_ID,
            null,
            "reden.widget.config.title") {
            override fun getConfigs() = ConfigOptionWrapper.createFor(getAllOptions())
        })
        true
    }
}
