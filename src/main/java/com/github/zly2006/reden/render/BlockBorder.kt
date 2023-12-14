package com.github.zly2006.reden.render

import com.github.zly2006.reden.malilib.BLOCK_BORDER_ALPHA
import com.github.zly2006.reden.malilib.MAX_RENDER_DISTANCE
import com.github.zly2006.reden.network.TagBlockPos
import com.mojang.blaze3d.systems.RenderSystem
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormats
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.Vec3f

@Environment(EnvType.CLIENT)
object BlockBorder {
    internal val tags = mutableMapOf<Long, Int>()

    @JvmStatic operator fun set(pos: BlockPos, status: Int?) {
        if (status == null) {
            tags.remove(pos.asLong())
        } else {
            tags[pos.asLong()] = status
        }
    }

    @JvmStatic operator fun get(pos: BlockPos): Int {
        return tags[pos.asLong()] ?: 0
    }
}

private operator fun Vec3d.minus(pos: Vec3d): Vec3d {
    return Vec3d(x - pos.x, y - pos.y, z - pos.z)
}

private fun BlockPos.vec3d() = Vec3d(x.toDouble(), y.toDouble(), z.toDouble())
