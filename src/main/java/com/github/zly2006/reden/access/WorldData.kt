package com.github.zly2006.reden.access

import net.minecraft.server.world.ServerWorld
import net.minecraft.world.World

class WorldData(serverWorld: ServerWorld) {
    var status: Long = 0
    interface WorldDataAccess {
        fun getRedenWorldData(): WorldData
    }

    fun addStatus(status: Long): Long {
        this.status = this.status or status
        return this.status
    }
    fun removeStatus(status: Long): Long {
        this.status = this.status and status.inv()
        return this.status
    }

    companion object {
        fun ServerWorld.data(): WorldData {
            return (this as WorldDataAccess).getRedenWorldData()
        }
        fun World.data(): WorldData? {
            return (this as? ServerWorld)?.data()
        }
    }
}