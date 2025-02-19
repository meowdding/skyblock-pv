package tech.thatgravyboat.skyblockpv.utils.widgets

import com.mojang.blaze3d.systems.RenderSystem
import earth.terrarium.olympus.client.components.base.BaseWidget
import earth.terrarium.olympus.client.components.base.renderer.WidgetRenderer
import earth.terrarium.olympus.client.components.base.renderer.WidgetRendererContext
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.RenderType
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.ARGB
import java.util.function.Function

class SpriteWidget : BaseWidget() {
    private var renderer: WidgetRenderer<in SpriteWidget?> = WidgetRenderer.empty<SpriteWidget?>()
    private var resourceLocation: ResourceLocation? = null

    override fun renderWidget(graphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        this.isHovered = graphics.containsPointInScissor(mouseX, mouseY) && this.isMouseOver(mouseX.toDouble(), mouseY.toDouble())
        val color = ARGB.color(255, 255, 255, (this.alpha * 255.0f).toInt())
        RenderSystem.enableBlend()
        RenderSystem.enableDepthTest()
        resourceLocation?.let {
            graphics.blitSprite(
                Function { location: ResourceLocation? -> RenderType.guiTextured(location) },
                it,
                this.x,
                this.y,
                this.getWidth(),
                this.getHeight(),
                color,
            )
        }

        this.renderer.render(graphics, WidgetRendererContext(this, mouseX, mouseY), partialTick)
    }

    fun withRenderer(renderer: WidgetRenderer<in SpriteWidget?>): SpriteWidget {
        this.renderer = renderer
        return this
    }

    fun withTexture(sprite: ResourceLocation?): SpriteWidget {
        this.resourceLocation = sprite
        return this
    }

    override fun withSize(width: Int, height: Int): SpriteWidget = super.withSize(width, height) as SpriteWidget

}
