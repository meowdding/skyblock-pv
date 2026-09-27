package me.owdding.skyblockpv.utils.render.inventory

import com.mojang.renderpearl.api.pipeline.RenderPipeline
import earth.terrarium.olympus.client.utils.Orientation
import me.owdding.lib.layouts.ScalableWidget
import me.owdding.skyblockpv.SkyBlockPv
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.renderer.RenderPipelines
import org.joml.Matrix3x2f
import org.joml.Vector2i
import tech.thatgravyboat.skyblockapi.utils.extentions.scaled
import com.mojang.renderpearl.api.pipeline.BlendFunction
import com.mojang.renderpearl.api.pipeline.ColorTargetState
import com.mojang.renderpearl.api.pipeline.DepthStencilState
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.renderpearl.api.pipeline.CompareOp
import com.mojang.renderpearl.api.pipeline.UniformType
import com.mojang.renderpearl.api.vertex.VertexFormat

//? > 26.1
import com.mojang.renderpearl.api.pipeline.BindGroupLayout

object InventoryTextureRender {

    val INVENTORY_BACKGROUND: RenderPipeline = RenderPipelines.register(
        //~ if >= 26.2 '()' -> '(RenderPipelines.GUI_TEXTURED_SNIPPET)'
        RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
            .withLocation(SkyBlockPv.id("inventory"))
            //? < 26.2
            //.withVertexShader(SkyBlockPv.id("core/inventory"))
            .withFragmentShader(SkyBlockPv.id("core/inventory"))
            //? 26.1
            //.withShaderDefine("VERSION", 150)
            //? >= 26.2
            .withShaderDefine("VERSION", 330)
            //? < 26.3
            //.withShaderDefine("NO_LAYOUT")
            .withCull(false)
            //? >= 26.2 {
            .withBindGroupLayout(
                BindGroupLayout.builder()
                    .withUniform(POLY_UNIFORM_NAME, UNIFORM_BUFFER)
                    .build(),
            )
            //? } else {
            /*.withDepthStencilState(DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, true))
            .withColorTargetState(ColorTargetState(BlendFunction.TRANSLUCENT))
            .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
            .withSampler("Sampler0")
            .withUniform(POLY_UNIFORM_NAME, UniformType.UNIFORM_BUFFER)
            .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
            .withUniform("Projection", UniformType.UNIFORM_BUFFER)
            *///? }
            .build(),
    )
    val MONO_INVENTORY_BACKGROUND: RenderPipeline = RenderPipelines.register(
        //~ if >= 26.2 '()' -> '(RenderPipelines.GUI_TEXTURED_SNIPPET)'
        RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
            .withLocation(SkyBlockPv.id("mono_inventory"))
            //? < 26.2
            //.withVertexShader(SkyBlockPv.id("core/inventory"))
            .withFragmentShader(SkyBlockPv.id("core/mono_inventory"))
            //? 26.1
            //.withShaderDefine("VERSION", 150)
            //? >= 26.2
            .withShaderDefine("VERSION", 330)
            //? < 26.3
            //.withShaderDefine("NO_LAYOUT")
            .withCull(false)
            //? >= 26.2 {
            .withBindGroupLayout(
                BindGroupLayout.builder()
                    .withUniform(MONO_UNIFORM_NAME, UNIFORM_BUFFER)
                    .build(),
            )
            //? } else {
            /*.withDepthStencilState(DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, true))
            .withColorTargetState(ColorTargetState(BlendFunction.TRANSLUCENT))
            .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
            .withSampler("Sampler0")
            .withUniform(MONO_UNIFORM_NAME, UniformType.UNIFORM_BUFFER)
            .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
            .withUniform("Projection", UniformType.UNIFORM_BUFFER)
            *///? }
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
                    orientation == VERTICAL,
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
