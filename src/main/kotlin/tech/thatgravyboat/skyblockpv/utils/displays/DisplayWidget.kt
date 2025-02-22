package tech.thatgravyboat.skyblockpv.utils.displays

import com.teamresourceful.resourcefullib.client.screens.CursorScreen
import earth.terrarium.olympus.client.components.base.BaseWidget
import earth.terrarium.olympus.client.components.base.renderer.WidgetRenderer
import earth.terrarium.olympus.client.components.base.renderer.WidgetRendererContext
import net.minecraft.client.gui.GuiGraphics

class DisplayWidget(private val display: Display): BaseWidget() {
    private var renderer: WidgetRenderer<DisplayWidget?> = WidgetRenderer.empty()

    init {
        active = false
        width = display.getWidth()
        height = display.getHeight()
    }

    override fun renderWidget(graphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTicks: Float) {
        display.render(graphics, x, y)
        renderer.render(graphics, WidgetRendererContext<DisplayWidget?>(this, mouseX, mouseY), partialTicks)
    }

    fun withRenderer(renderer: WidgetRenderer<DisplayWidget?>): DisplayWidget {
        this.renderer = renderer
        return this
    }

    override fun withSize(width: Int, height: Int): DisplayWidget = super.withSize(width, height) as DisplayWidget

    override fun getCursor(): CursorScreen.Cursor {
        return CursorScreen.Cursor.DEFAULT
    }
}
