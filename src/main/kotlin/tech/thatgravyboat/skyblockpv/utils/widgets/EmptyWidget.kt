package tech.thatgravyboat.skyblockpv.utils.widgets

import earth.terrarium.olympus.client.components.base.BaseWidget
import earth.terrarium.olympus.client.components.base.renderer.WidgetRenderer
import earth.terrarium.olympus.client.components.base.renderer.WidgetRendererContext
import net.minecraft.client.gui.GuiGraphics

class EmptyWidget : BaseWidget() {
    private var renderer: WidgetRenderer<in EmptyWidget?> = WidgetRenderer.empty<EmptyWidget?>()

    override fun renderWidget(graphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        this.isHovered = graphics.containsPointInScissor(mouseX, mouseY) && this.isMouseOver(mouseX.toDouble(), mouseY.toDouble())
        this.renderer.render(graphics, WidgetRendererContext(this, mouseX, mouseY), partialTick)
    }

    fun withRenderer(renderer: WidgetRenderer<in EmptyWidget?>): EmptyWidget {
        this.renderer = renderer
        return this
    }

    override fun withSize(width: Int, height: Int): EmptyWidget = super.withSize(width, height) as EmptyWidget

}
