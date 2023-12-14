package com.github.zly2006.reden.network

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.access.ServerData
import com.github.zly2006.reden.utils.isClient
import com.github.zly2006.reden.utils.sendMessage
import com.github.zly2006.reden.utils.translateMessage
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.loader.api.Version
import net.minecraft.client.MinecraftClient
import net.minecraft.network.PacketByteBuf

class Hello(
    val version: Version,
    val featureSet: Set<String>,
) {
    companion object {
        private val id = Reden.identifier("hello")

        fun register() {
            if (isClient) {
                ClientPlayNetworking.registerGlobalReceiver(id) { _, _, buf, _ ->
                    val version = Version.parse(buf.readString())
                    val featureSet = mutableSetOf<String>()
                    repeat(buf.readVarInt()) {
                        featureSet.add(buf.readString())
                    }
                    Reden.LOGGER.info("Hello from server: $version")
                    Reden.LOGGER.info("Feature set: " + featureSet.joinToString())
                    val mc = MinecraftClient.getInstance()
                    (mc as ServerData.ClientSideServerDataAccess).`serverData$reden` =
                        ServerData(version, null).apply {
                            this.featureSet.addAll(featureSet)
                        }
                    featureSet.forEach { name ->
                        when (name) {
                            "undo" -> ClientPlayNetworking.registerReceiver(Undo.id) { packet, player, buf, _ ->
                                mc.player!!.sendMessage(
                                    when (buf.readVarInt()) {
                                        0 -> translateMessage("undo", "rollback_success")
                                        1 -> translateMessage("undo", "restore_success")
                                        2 -> translateMessage("undo", "no_blocks_info")
                                        16 -> translateMessage("undo", "no_permission")
                                        32 -> translateMessage("undo", "not_recording")
                                        64 -> translateMessage("undo", "busy")
                                        65536 -> translateMessage("undo", "unknown_error")
                                        else -> translateMessage("undo", "unknown_status")
                                    }
                                )
                            }
                        }
                    }
                }
            }
            ServerPlayConnectionEvents.JOIN.register { _, sender, _ ->
                sender.sendPacket(id, PacketByteBufs.create().apply {
                    val featureSet = setOf("undo")
                    val version = Reden.MOD_VERSION
                    writeString(version.toString())
                    writeVarInt(featureSet.size)
                    featureSet.forEach {
                        writeString(it)
                    }
                })
            }
        }
    }
}
