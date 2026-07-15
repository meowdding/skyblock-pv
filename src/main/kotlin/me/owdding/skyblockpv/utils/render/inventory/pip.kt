package me.owdding.skyblockpv.utils.render.inventory

//? 26.1 {
/*import com.mojang.blaze3d.vertex.Tesselator
import com.mojang.blaze3d.vertex.VertexFormat
*///? }
//? >= 26.2
import com.mojang.blaze3d.PrimitiveTopology
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.textures.FilterMode
import com.mojang.blaze3d.vertex.BufferBuilder
import com.mojang.blaze3d.vertex.ByteBufferBuilder
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.PoseStack
import earth.terrarium.olympus.client.pipelines.pips.OlympusPictureInPictureRenderState
import earth.terrarium.olympus.client.pipelines.renderer.PipelineRenderer
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.utils.theme.ThemeSupport
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.gui.render.TextureSetup
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer
//? 26.1
//import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.SubmitNodeCollector
import org.joml.Matrix3x2f
import org.joml.Vector2i
import tech.thatgravyboat.skyblockapi.helpers.McClient
import java.util.function.Supplier

//? 26.1
//import java.util.function.Function

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

//~ if >= 26.2 '(source: MultiBufferSource.BufferSource) : ' -> '() : ', '(source)' -> '()'
class MonoInventoryPipRenderer() : PictureInPictureRenderer<MonoInventoryPipState>() {
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

        //? >= 26.2 {
        ByteBufferBuilder.exactlySized(DefaultVertexFormat.POSITION_TEX_COLOR.vertexSize * 4).use {
            val buffer = BufferBuilder(it, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR)
            //?} else
            //val buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR)

            buffer.addVertex(0f, 0f, 0f).setUv(0f, 0f).setColor(state.color)
            buffer.addVertex(0f, scaledHeight, 0f).setUv(0f, 1f).setColor(state.color)
            buffer.addVertex(scaledWidth, scaledHeight, 0f).setUv(1f, 1f).setColor(state.color)
            buffer.addVertex(scaledWidth, 0f, 0f).setUv(1f, 0f).setColor(state.color)

            val texture = McClient.self.textureManager.getTexture(ThemeSupport.texture(MONO_TEXTURE))

            PipelineRenderer.builder(InventoryTextureRender.MONO_INVENTORY_BACKGROUND, buffer.buildOrThrow())
                .uniform(MonoInventoryUniform.STORAGE, MonoInventoryUniform(state.size, if (state.vertical) 1 else 0))
                .textures(
                    TextureSetup.singleTexture(
                        texture.textureView,
                        RenderSystem.getSamplerCache().getRepeat(FilterMode.NEAREST),
                    )
                )
                .color(state.color)
                .draw()

            this.lastState = state
            //? >= 26.2
        }
    }

    override fun getTextureLabel() = "skyblockpv_mono_inventory"

}

//~ if >= 26.2 '(source: MultiBufferSource.BufferSource) : ' -> '() : ', '(source)' -> '()'
class PolyInventoryPipRenderer() : PictureInPictureRenderer<PolyInventoryPipState>() {
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

        //? >= 26.2 {
        ByteBufferBuilder.exactlySized(DefaultVertexFormat.POSITION_TEX_COLOR.vertexSize * 4).use {
            val buffer = BufferBuilder(it, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR)
            //?} else
            //val buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR)

            buffer.addVertex(0f, 0f, 0f).setUv(0f, 0f).setColor(state.color)
            buffer.addVertex(0f, scaledHeight, 0f).setUv(0f, 1f).setColor(state.color)
            buffer.addVertex(scaledWidth, scaledHeight, 0f).setUv(1f, 1f).setColor(state.color)
            buffer.addVertex(scaledWidth, 0f, 0f).setUv(1f, 0f).setColor(state.color)

            val texture = McClient.self.textureManager.getTexture(ThemeSupport.texture(POLY_TEXTURE))

            PipelineRenderer.builder(InventoryTextureRender.INVENTORY_BACKGROUND, buffer.buildOrThrow())
                .uniform(PolyInventoryUniform.STORAGE, PolyInventoryUniform(state.size))
                .textures(
                    TextureSetup.singleTexture(
                        texture.textureView,
                        RenderSystem.getSamplerCache().getRepeat(FilterMode.NEAREST),
                    )
                )
                .color(state.color)
                .draw()

            this.lastState = state
            //? >= 26.2
        }
    }

    override fun getTextureLabel() = "skyblockpv_poly_inventory"

}
