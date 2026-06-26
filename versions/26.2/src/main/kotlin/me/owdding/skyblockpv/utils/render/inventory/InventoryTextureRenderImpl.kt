package me.owdding.skyblockpv.utils.render.inventory

import com.mojang.blaze3d.pipeline.BindGroupLayout
import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.shaders.UniformType
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
        RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
            .withLocation(SkyBlockPv.id("inventory"))
            .withFragmentShader(SkyBlockPv.id("core/inventory"))
            .withCull(false)
            .withBindGroupLayout(
                BindGroupLayout.builder()
                    .withUniform(POLY_UNIFORM_NAME, UniformType.UNIFORM_BUFFER)
                    .build(),
            )
            .build(),
    )

    val MONO_INVENTORY_BACKGROUND: RenderPipeline = RenderPipelines.register(
        RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
            .withLocation(SkyBlockPv.id("mono_inventory"))
            .withFragmentShader(SkyBlockPv.id("core/mono_inventory"))
            .withCull(false)
            .withBindGroupLayout(
                BindGroupLayout.builder()
                    .withUniform(MONO_UNIFORM_NAME, UniformType.UNIFORM_BUFFER)
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
