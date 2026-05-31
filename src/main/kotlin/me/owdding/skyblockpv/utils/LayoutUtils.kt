package me.owdding.skyblockpv.utils

import earth.terrarium.olympus.client.components.Widgets
import earth.terrarium.olympus.client.components.base.ListWidget
import earth.terrarium.olympus.client.components.compound.LayoutWidget
import earth.terrarium.olympus.client.components.string.TextWidget
import me.owdding.lib.displays.DisplayWidget
import me.owdding.skyblockpv.utils.components.PvLayouts
import net.minecraft.client.gui.layouts.FrameLayout
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.client.gui.layouts.LayoutElement
import net.minecraft.network.chat.Component

object LayoutUtils {

    fun LayoutElement.centerVertically(height: Int): LayoutElement {
        return FrameLayout(0, height).also { it.addChild(this) }
    }

    fun LayoutElement.centerHorizontally(width: Int): LayoutElement {
        return FrameLayout(width, 0).also { it.addChild(this) }
    }

    fun LayoutElement.center(width: Int, height: Int): LayoutElement {
        return FrameLayout(width, height).also { it.addChild(this) }
    }

    fun Layout.fitsIn(bg: DisplayWidget): Boolean {
        return this.height <= bg.height && this.width <= bg.width
    }

    fun Layout.asScrollable(width: Int, height: Int, init: ListWidget.() -> Unit = {}): Layout {
        this.arrangeElements()
        val widget = LayoutWidget(this).also { it.visible = true }.withStretchToContentSize()
        val scrollable = ListWidget((widget.width + 20).coerceAtMost(width), widget.height.coerceAtMost(height - 20))

        scrollable.add(widget)
        scrollable.init()

        return PvLayouts.frame(width - 20, height) {
            widget(scrollable)
        }
    }

    fun ListWidget.withScrollToBottom() {
        this.mouseScrolled(0.0, 0.0, 0.0, -this.height.toDouble())
    }

    fun Component.asWidget(): TextWidget = Widgets.text(this)
}
