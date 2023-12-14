package com.github.zly2006.reden.access

import com.github.zly2006.reden.utils.isClient
import com.github.zly2006.reden.utils.server
import net.fabricmc.loader.api.Version
import net.minecraft.client.MinecraftClient
import net.minecraft.server.MinecraftServer
import java.util.*

/**
 * Note: in this fork it is client-side only.
 */
class ServerData(
    val version: Version,
    mcServer: MinecraftServer?
) {
    init {
        if (mcServer != null) {
            server = mcServer
        }
    }
    var uuid: UUID? = null
    val featureSet = mutableSetOf<String>()

    interface ServerDataAccess {
        val `serverData$reden`: ServerData
    }

    interface ClientSideServerDataAccess {
        var `serverData$reden`: ServerData?
    }

    companion object {
        @JvmStatic
        fun MinecraftServer.data(): ServerData {
            return (this as ServerDataAccess).`serverData$reden`
        }
        fun MinecraftClient.serverData(): ServerData? {
            return (this as ClientSideServerDataAccess).`serverData$reden`
        }
        @JvmStatic
        fun getServerData() = if (!isClient) {
            server.data()
        } else {
            val mc = MinecraftClient.getInstance()
            if (mc.isInSingleplayer) mc.server?.data()
            else mc.serverData()
        }
    }

}