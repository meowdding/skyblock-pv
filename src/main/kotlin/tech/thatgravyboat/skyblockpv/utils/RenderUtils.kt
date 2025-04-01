package tech.thatgravyboat.skyblockpv.utils

import com.mojang.blaze3d.pipeline.BlendFunction
import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.platform.DepthTestFunction
import com.mojang.blaze3d.platform.LogicOp
import com.mojang.blaze3d.shaders.UniformType
import com.mojang.blaze3d.systems.RenderPass
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.textures.GpuTexture
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.Tesselator
import com.mojang.blaze3d.vertex.VertexFormat
import earth.terrarium.olympus.client.pipelines.PipelineRenderer
import earth.terrarium.olympus.client.utils.Orientation
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.RenderPipelines
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockpv.SkyBlockPv

object RenderUtils {

    val INVENTORY_BACKGROUND = RenderPipelines.register(
        RenderPipeline.builder()
            .withLocation(SkyBlockPv.id("inventory"))
            .withVertexShader(SkyBlockPv.id("core/inventory"))
            .withFragmentShader(SkyBlockPv.id("core/inventory"))
            .withCull(false)
            .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
            .withColorLogic(LogicOp.NONE)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withVertexFormat(DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS)
            .withSampler("Sampler0")
            .withUniform("ModelViewMat", UniformType.MATRIX4X4)
            .withUniform("ProjMat", UniformType.MATRIX4X4)
            .withUniform("Size", UniformType.VEC2)
            .build(),
    )
    val MONO_INVENTORY_BACKGROUND = RenderPipelines.register(
        RenderPipeline.builder()
            .withLocation(SkyBlockPv.id("mono_inventory"))
            .withVertexShader(SkyBlockPv.id("core/inventory"))
            .withFragmentShader(SkyBlockPv.id("core/mono_inventory"))
            .withCull(false)
            .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
            .withColorLogic(LogicOp.NONE)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withVertexFormat(DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS)
            .withSampler("Sampler0")
            .withUniform("ModelViewMat", UniformType.MATRIX4X4)
            .withUniform("ProjMat", UniformType.MATRIX4X4)
            .withUniform("Size", UniformType.INT)
            .withUniform("Vertical", UniformType.INT)
            .build(),
    )

    fun drawInventory(
        graphics: GuiGraphics,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        size: Int,
        orientation: Orientation,
    ) {
        val matrix = graphics.pose().last().pose()
        val buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX)
        buffer.addVertex(matrix, (x).toFloat(), (y).toFloat(), 1.0f).setUv(0f, 0f)
        buffer.addVertex(matrix, (x).toFloat(), (y + height).toFloat(), 1.0f).setUv(0f, 1f)
        buffer.addVertex(matrix, (x + width).toFloat(), (y + height).toFloat(), 1.0f).setUv(1f, 1f)
        buffer.addVertex(matrix, (x + width).toFloat(), (y).toFloat(), 1.0f).setUv(1f, 0f)


        val gpuTexture: GpuTexture = McClient.self.textureManager.getTexture(SkyBlockPv.id("textures/gui/inventory-3x1.png")).texture
        RenderSystem.setShaderTexture(0, gpuTexture)
        PipelineRenderer.draw(MONO_INVENTORY_BACKGROUND, buffer.buildOrThrow()) { pass: RenderPass ->
            pass.bindSampler("Sampler0", gpuTexture)
            pass.setUniform("Size", size)
            pass.setUniform("Vertical", orientation.getValue(0, 1))
        }
    }

    fun drawInventory(
        graphics: GuiGraphics,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        columns: Int,
        rows: Int,
    ) {
        val matrix = graphics.pose().last().pose()
        val buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX)
        buffer.addVertex(matrix, (x).toFloat(), (y).toFloat(), 1.0f).setUv(0f, 0f)
        buffer.addVertex(matrix, (x).toFloat(), (y + height).toFloat(), 1.0f).setUv(0f, 1f)
        buffer.addVertex(matrix, (x + width).toFloat(), (y + height).toFloat(), 1.0f).setUv(1f, 1f)
        buffer.addVertex(matrix, (x + width).toFloat(), (y).toFloat(), 1.0f).setUv(1f, 0f)

        val gpuTexture: GpuTexture = McClient.self.textureManager.getTexture(SkyBlockPv.id("textures/gui/base_inventory.png")).texture
        RenderSystem.setShaderTexture(0, gpuTexture)
        PipelineRenderer.draw(INVENTORY_BACKGROUND, buffer.buildOrThrow()) { pass: RenderPass ->
            pass.bindSampler("Sampler0", gpuTexture)
            pass.setUniform("Size", columns.toFloat(), rows.toFloat())
        }
    }

}
