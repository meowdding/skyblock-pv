package me.owdding.skyblockpv.utils.render.inventory

import com.mojang.blaze3d.pipeline.BlendFunction
import com.mojang.blaze3d.pipeline.ColorTargetState
import com.mojang.blaze3d.pipeline.DepthStencilState
import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.platform.CompareOp
import com.mojang.blaze3d.shaders.UniformType
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.VertexFormat
import earth.terrarium.olympus.client.utils.Orientation
import me.owdding.lib.layouts.ScalableWidget
import me.owdding.skyblockpv.SkyBlockPv
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.renderer.RenderPipelines
import org.joml.Matrix3x2f
import org.joml.Vector2i
import tech.thatgravyboat.skyblockapi.utils.extentions.scaled

object InventoryTextureRender {

    val INVENTORY_BACKGROUND: RenderPipeline = RenderPipelines.register(
        RenderPipeline.builder()
            .withLocation(SkyBlockPv.id("inventory"))
            .withVertexShader(SkyBlockPv.id("core/inventory"))
            .withFragmentShader(SkyBlockPv.id("core/inventory"))
            .withCull(false)
            .withDepthStencilState(DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, true))
            .withColorTargetState(ColorTargetState(BlendFunction.TRANSLUCENT))
            .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
            .withSampler("Sampler0")
            .withUniform(POLY_UNIFORM_NAME, UniformType.UNIFORM_BUFFER)
            .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
            .withUniform("Projection", UniformType.UNIFORM_BUFFER)
            .build(),
    )
    val MONO_INVENTORY_BACKGROUND: RenderPipeline = RenderPipelines.register(
        RenderPipeline.builder()
            .withLocation(SkyBlockPv.id("mono_inventory"))
            .withVertexShader(SkyBlockPv.id("core/inventory"))
            .withFragmentShader(SkyBlockPv.id("core/mono_inventory"))
            .withCull(false)
            .withDepthStencilState(DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, true))
            .withColorTargetState(ColorTargetState(BlendFunction.TRANSLUCENT))
            .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
            .withSampler("Sampler0")
            .withUniform(MONO_UNIFORM_NAME, UniformType.UNIFORM_BUFFER)
            .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
            .withUniform("Projection", UniformType.UNIFORM_BUFFER)
            .build(),
    )


    fun drawInventory(
        graphics: GuiGraphicsExtractor,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        size: Int,
        orientation: Orientation,
        color: Int,
    ) {
        graphics.scaled(1 / ScalableWidget.getCurrentScale(), 1 / ScalableWidget.getCurrentScale()) {
            graphics.guiRenderState.addPicturesInPictureState(
                MonoInventoryPipState(
                    x,
                    y,
                    ((x + width) * ScalableWidget.getCurrentScale()).toInt(),
                    ((y + height) * ScalableWidget.getCurrentScale()).toInt(),
                    graphics.scissorStack.peek(),
                    Matrix3x2f(graphics.pose()),
                    size,
                    color,
                    orientation == Orientation.VERTICAL,
                ),
            )
        }
    }

    fun drawInventory(
        graphics: GuiGraphicsExtractor,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        columns: Int,
        rows: Int,
        color: Int,
    ) {
        graphics.scaled(1 / ScalableWidget.getCurrentScale(), 1 / ScalableWidget.getCurrentScale()) {
            graphics.guiRenderState.addPicturesInPictureState(
                PolyInventoryPipState(
                    x,
                    y,
                    ((x + width) * ScalableWidget.getCurrentScale()).toInt(),
                    ((y + height) * ScalableWidget.getCurrentScale()).toInt(),
                    graphics.scissorStack.peek(),
                    Matrix3x2f(graphics.pose()),
                    Vector2i(columns, rows),
                    color,
                ),
            )
        }
    }
}
