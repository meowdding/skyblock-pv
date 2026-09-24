package me.owdding.skyblockpv.utils.render.inventory

import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.renderpearl.api.pipeline.RenderPipeline
import earth.terrarium.olympus.client.pipelines.pips.OlympusPictureInPictureRenderState
import earth.terrarium.olympus.client.pipelines.renderer.PipelineSubmit
import earth.terrarium.olympus.client.pipelines.uniforms.RenderPipelineUniforms
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.utils.theme.ThemeSupport
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.gui.render.TextureSetup
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer
import net.minecraft.client.renderer.DynamicGpuDataStorage
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.texture.AbstractTexture
import org.joml.Matrix3x2f
import org.joml.Vector2i
import tech.thatgravyboat.skyblockapi.helpers.McClient
import java.util.function.Supplier
import com.mojang.blaze3d.vertex.BufferBuilder
import com.mojang.blaze3d.vertex.ByteBufferBuilder

//? >= 26.2
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology
//? 26.1 {
/*import com.mojang.blaze3d.vertex.Tesselator
import com.mojang.renderpearl.api.vertex.VertexFormat
import net.minecraft.client.renderer.MultiBufferSource
import java.util.function.Function
*///? }

val MONO_TEXTURE = SkyBlockPv.id("textures/gui/inventory/mono.png")
val POLY_TEXTURE = SkyBlockPv.id("textures/gui/inventory/poly.png")

abstract class SkyBlockPvPipState<T : OlympusPictureInPictureRenderState<T>>() : OlympusPictureInPictureRenderState<T> {
    abstract val x0: Int
    abstract val y0: Int
    abstract val x1: Int
    abstract val y1: Int

    open val scale: Float = 1f

    abstract val scissorArea: ScreenRectangle?
    abstract val pose: Matrix3x2f

    open val shrinkToScissor: Boolean = true

    val bounds: ScreenRectangle? by lazy {
        if (scissorArea != null && shrinkToScissor) {
            scissorArea!!.intersection(ScreenRectangle(x0, y0, x1 - x0, y1 - y0).transformMaxBounds(pose))
        } else {
            ScreenRectangle(x0, y0, x1 - x0, y1 - y0).transformMaxBounds(pose)
        }
    }

    override fun x0() = x0
    override fun y0() = y0
    override fun x1() = x1
    override fun y1() = y1
    override fun scissorArea() = scissorArea
    override fun pose() = pose
    override fun scale() = scale
    override fun bounds() = bounds
}

data class MonoInventoryPipState(
    override val x0: Int, override val y0: Int, override val x1: Int, override val y1: Int,
    override val scissorArea: ScreenRectangle?,
    override val pose: Matrix3x2f,
    val size: Int,
    val color: Int,
    val vertical: Boolean,
) : SkyBlockPvPipState<MonoInventoryPipState>() {
    //? if >= 26.2 {
    override fun getFactory(): Supplier<PictureInPictureRenderer<MonoInventoryPipState>> = Supplier { MonoInventoryPipRenderer() }
    //? } else
    //override fun getFactory(): Function<MultiBufferSource.BufferSource, PictureInPictureRenderer<MonoInventoryPipState>> = Function { buffer -> MonoInventoryPipRenderer(buffer) }

    override val shrinkToScissor: Boolean = false
}

data class PolyInventoryPipState(
    override val x0: Int, override val y0: Int, override val x1: Int, override val y1: Int,
    override val scissorArea: ScreenRectangle?,
    override val pose: Matrix3x2f,
    val size: Vector2i,
    val color: Int,
) : SkyBlockPvPipState<PolyInventoryPipState>() {
    //? if >= 26.2 {
    override fun getFactory(): Supplier<PictureInPictureRenderer<PolyInventoryPipState>> = Supplier { PolyInventoryPipRenderer() }
    //? } else
    //override fun getFactory(): Function<MultiBufferSource.BufferSource, PictureInPictureRenderer<PolyInventoryPipState>> = Function { buffer -> PolyInventoryPipRenderer(buffer) }

    override val shrinkToScissor: Boolean = false
}

private fun <Uniform : RenderPipelineUniforms> submit(
    pipeline: RenderPipeline,
    width: Float,
    height: Float,
    uniformStorage: Supplier<DynamicGpuDataStorage<Uniform>>,
    uniform: Uniform,
    texture: AbstractTexture,
    color: Int,/*? >= 26.2 >> ')'*/
    submitNodeCollector: SubmitNodeCollector,
) {
    //? if 26.1 {
/*
    val bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR)

    *///? else 26.2 {
/*
    ByteBufferBuilder.exactlySized(DefaultVertexFormat.POSITION_TEX_COLOR.vertexSize * 4).use {
        val bufferBuilder = BufferBuilder(it, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR)
    *///? }

    //? if >= 26.3 {

    PipelineSubmit.builder(pipeline)
        .vertices(DefaultVertexFormat.POSITION_TEX_COLOR, PrimitiveTopology.QUADS) { bufferBuilder ->

            //? }

            bufferBuilder.addVertex(0f, 0f, 0f).setUv(0f, 0f).setColor(color)
            bufferBuilder.addVertex(0f, height, 0f).setUv(0f, 1f).setColor(color)
            bufferBuilder.addVertex(width, height, 0f).setUv(1f, 1f).setColor(color)
            bufferBuilder.addVertex(width, 0f, 0f).setUv(1f, 0f).setColor(color)

            //? 26.3
        }
        //? < 26.3
        //PipelineSubmit.builder(pipeline, bufferBuilder.buildOrThrow())
        .uniform(uniformStorage, uniform)
        .textures(TextureSetup.singleTexture(texture.textureView, texture.sampler)).color(color)
        //~ if >= 26.3 'draw(' -> 'submit(submitNodeCollector'
        .submit(submitNodeCollector)

    //? = 26.2
    //}

}

//~ if >= 26.2 '(source: MultiBufferSource.BufferSource) : ' -> ' : ', '(source)' -> '()'
class MonoInventoryPipRenderer : PictureInPictureRenderer<MonoInventoryPipState>() {
    private var lastState: MonoInventoryPipState? = null

    override fun getRenderStateClass() = MonoInventoryPipState::class.java

    override fun textureIsReadyToBlit(state: MonoInventoryPipState): Boolean {
        return lastState != null && lastState == state
    }

    override fun renderToTexture(state: MonoInventoryPipState, stack: PoseStack/*? >= 26.2 >> ')'*/, submitNodeCollector: SubmitNodeCollector) {
        val bounds = state.bounds ?: return

        val scale = McClient.window.guiScale.toFloat()
        val scaledWidth = (bounds.width) * scale
        val scaledHeight = bounds.height * scale

        val texture = McClient.self.textureManager.getTexture(ThemeSupport.texture(MONO_TEXTURE))

        submit(
            InventoryTextureRender.MONO_INVENTORY_BACKGROUND,
            scaledWidth,
            scaledHeight,
            MonoInventoryUniform.STORAGE,
            MonoInventoryUniform(state.size, if (state.vertical) 1 else 0),
            texture,
            state.color,
            //? >= 26.2
            submitNodeCollector,
        )
    }

    override fun getTextureLabel() = "skyblockpv_mono_inventory"

}

//~ if >= 26.2 '(source: MultiBufferSource.BufferSource) : ' -> ' : ', '(source)' -> '()'
class PolyInventoryPipRenderer : PictureInPictureRenderer<PolyInventoryPipState>() {
    private var lastState: PolyInventoryPipState? = null

    override fun getRenderStateClass() = PolyInventoryPipState::class.java

    override fun textureIsReadyToBlit(state: PolyInventoryPipState): Boolean {
        return lastState != null && lastState == state
    }

    override fun renderToTexture(state: PolyInventoryPipState, stack: PoseStack/*? >= 26.2 >> ')'*/, submitNodeCollector: SubmitNodeCollector) {
        val bounds = state.bounds ?: return

        val scale = McClient.window.guiScale.toFloat()
        val scaledWidth = bounds.width * scale
        val scaledHeight = bounds.height * scale


        val texture = McClient.self.textureManager.getTexture(ThemeSupport.texture(POLY_TEXTURE))

        submit(
            InventoryTextureRender.INVENTORY_BACKGROUND,
            scaledWidth,
            scaledHeight,
            PolyInventoryUniform.STORAGE,
            PolyInventoryUniform(state.size),
            texture,
            state.color,
            //? >= 26.2
            submitNodeCollector,
        )
    }

    override fun getTextureLabel() = "skyblockpv_poly_inventory"

}
