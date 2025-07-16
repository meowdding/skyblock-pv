package me.owdding.skyblockpv.utils.render.inventory

import com.mojang.blaze3d.buffers.Std140Builder
import com.mojang.blaze3d.buffers.Std140SizeCalculator
import earth.terrarium.olympus.client.pipelines.uniforms.RenderPipelineUniforms
import earth.terrarium.olympus.client.pipelines.uniforms.RenderPipelineUniformsStorage
import net.minecraft.client.renderer.DynamicUniformStorage
import org.joml.Vector2i
import java.nio.ByteBuffer
import java.util.function.Supplier


const val MONO_UNIFORM_NAME = "MonoInventoryUniform"

data class MonoInventoryUnfirom(
    val size: Int,
    val vertical: Int,
) : RenderPipelineUniforms {
    companion object {
        val STORAGE: Supplier<DynamicUniformStorage<MonoInventoryUnfirom>> =
            RenderPipelineUniformsStorage.register<MonoInventoryUnfirom>("SkyblockPv Mono Inventory UBO", 5, Std140SizeCalculator().putInt().putInt())
    }

    override fun name() = MONO_UNIFORM_NAME

    override fun write(byteBuffer: ByteBuffer) {
        Std140Builder.intoBuffer(byteBuffer)
            .putInt(size)
            .putInt(vertical)
            .get()
    }
}

const val POLY_UNIFORM_NAME = "PolyInventoryUniform"

data class PolyInventoryUnfirom(
    val size: Vector2i,
) : RenderPipelineUniforms {
    companion object {
        val STORAGE: Supplier<DynamicUniformStorage<PolyInventoryUnfirom>> =
            RenderPipelineUniformsStorage.register<PolyInventoryUnfirom>("SkyblockPv Poly Inventory UBO", 5, Std140SizeCalculator().putIVec2())
    }

    override fun name() = POLY_UNIFORM_NAME

    override fun write(byteBuffer: ByteBuffer) {
        Std140Builder.intoBuffer(byteBuffer)
            .putIVec2(size)
            .get()
    }
}
