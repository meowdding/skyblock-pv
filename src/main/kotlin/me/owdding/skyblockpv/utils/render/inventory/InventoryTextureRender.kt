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
import com.mojang.renderpearl.api.pipeline.BindGroupLayout

object InventoryTextureRender {

    val INVENTORY_BACKGROUND: RenderPipeline = RenderPipelines.register(
        RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
            .withLocation(SkyBlockPv.id("inventory"))
            .withFragmentShader(SkyBlockPv.id("core/inventory"))
            .withShaderDefine("VERSION", 330)
            //? < 26.3
            //.withShaderDefine("NO_LAYOUT")
            .withBindGroupLayout(
                BindGroupLayout.builder()
                    .withUniform(POLY_UNIFORM_NAME, UNIFORM_BUFFER)
                    .build(),
            )
            .build(),
    )
    val MONO_INVENTORY_BACKGROUND: RenderPipeline = RenderPipelines.register(
        RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
            .withLocation(SkyBlockPv.id("mono_inventory"))
            .withFragmentShader(SkyBlockPv.id("core/mono_inventory"))
            .withShaderDefine("VERSION", 330)
            //? < 26.3
            //.withShaderDefine("NO_LAYOUT")
            .withCull(false)
            .withBindGroupLayout(
                BindGroupLayout.builder()
                    .withUniform(MONO_UNIFORM_NAME, UNIFORM_BUFFER)
                    .build(),
            )
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
