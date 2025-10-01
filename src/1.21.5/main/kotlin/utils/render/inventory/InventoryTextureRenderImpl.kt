package me.owdding.skyblockpv.utils.render.inventory

import com.mojang.blaze3d.pipeline.BlendFunction
import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.platform.DepthTestFunction
import com.mojang.blaze3d.platform.LogicOp
import com.mojang.blaze3d.shaders.UniformType
import com.mojang.blaze3d.systems.RenderPass
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.textures.GpuTexture
import com.mojang.blaze3d.vertex.BufferBuilder
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.Tesselator
import com.mojang.blaze3d.vertex.VertexFormat
import earth.terrarium.olympus.client.pipelines.PipelineRenderer
import earth.terrarium.olympus.client.utils.Orientation
import me.owdding.lib.rendering.text.TextShader
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.utils.theme.ThemeSupport
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.RenderPipelines
import tech.thatgravyboat.skyblockapi.helpers.McClient

actual object InventoryTextureRender {

    var TEXT_SHADER: TextShader? = null
        private set

    val MONO_TEXTURE = SkyBlockPv.id("textures/gui/inventory/mono.png")
    val POLY_TEXTURE = SkyBlockPv.id("textures/gui/inventory/poly.png")

    val INVENTORY_BACKGROUND = RenderPipelines.register(
        RenderPipeline.builder()
            .withLocation(SkyBlockPv.id("inventory"))
            .withVertexShader(SkyBlockPv.id("core/inventory"))
            .withFragmentShader(SkyBlockPv.id("core/inventory"))
            .withCull(false)
            .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
            .withColorLogic(LogicOp.NONE)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
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
            .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
            .withSampler("Sampler0")
            .withUniform("ModelViewMat", UniformType.MATRIX4X4)
            .withUniform("ProjMat", UniformType.MATRIX4X4)
            .withUniform("Size", UniformType.INT)
            .withUniform("Vertical", UniformType.INT)
            .build(),
    )

    private fun drawBuffer(graphics: GuiGraphics, x: Int, y: Int, width: Int, height: Int, color: Int): BufferBuilder {
        val matrix = graphics.pose().last().pose()
        val buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR)
        buffer.addVertex(matrix, (x).toFloat(), (y).toFloat(), 1.0f).setUv(0f, 0f).setColor(color)
        buffer.addVertex(matrix, (x).toFloat(), (y + height).toFloat(), 1.0f).setUv(0f, 1f).setColor(color)
        buffer.addVertex(matrix, (x + width).toFloat(), (y + height).toFloat(), 1.0f).setUv(1f, 1f).setColor(color)
        buffer.addVertex(matrix, (x + width).toFloat(), (y).toFloat(), 1.0f).setUv(1f, 0f).setColor(color)
        return buffer
    }

    actual fun drawInventory(
        graphics: GuiGraphics,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        size: Int,
        orientation: Orientation,
        color: Int,
    ) {
        val gpuTexture: GpuTexture = McClient.self.textureManager.getTexture(ThemeSupport.texture(MONO_TEXTURE)).texture
        RenderSystem.setShaderTexture(0, gpuTexture)
        PipelineRenderer.draw(MONO_INVENTORY_BACKGROUND, drawBuffer(graphics, x, y, width, height, color).buildOrThrow()) { pass: RenderPass ->
            pass.bindSampler("Sampler0", gpuTexture)
            pass.setUniform("Size", size)
            pass.setUniform("Vertical", orientation.getValue(0, 1))
        }
    }

    actual fun drawInventory(
        graphics: GuiGraphics,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        columns: Int,
        rows: Int,
        color: Int,
    ) {
        val gpuTexture: GpuTexture = McClient.self.textureManager.getTexture(ThemeSupport.texture(POLY_TEXTURE)).texture
        RenderSystem.setShaderTexture(0, gpuTexture)
        PipelineRenderer.draw(INVENTORY_BACKGROUND, drawBuffer(graphics, x, y, width, height, color).buildOrThrow()) { pass: RenderPass ->
            pass.bindSampler("Sampler0", gpuTexture)
            pass.setUniform("Size", columns.toFloat(), rows.toFloat())
        }
    }
}
