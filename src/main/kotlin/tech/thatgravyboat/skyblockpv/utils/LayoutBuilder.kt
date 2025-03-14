package tech.thatgravyboat.skyblockpv.utils

import earth.terrarium.olympus.client.components.Widgets
import net.minecraft.client.gui.layouts.LayoutElement
import net.minecraft.client.gui.layouts.LinearLayout
import net.minecraft.client.gui.layouts.SpacerElement
import net.minecraft.network.chat.Component
import tech.thatgravyboat.skyblockpv.utils.displays.Display
import tech.thatgravyboat.skyblockpv.utils.displays.asWidget

object LayoutBuild {
    fun vertical(spacing: Int = 0, alignment: Float = 0f, builder: VerticalLayoutBuilder.() -> Unit): LinearLayout {
        val builder = VerticalLayoutBuilder()
        builder.builder()
        return builder.build(spacing, alignment)
    }

    fun horizontal(spacing: Int = 0, alignment: Float = 0f, builder: HorizontalLayoutBuilder.() -> Unit): LinearLayout {
        val builder = HorizontalLayoutBuilder()
        builder.builder()
        return builder.build(spacing, alignment)
    }
}

abstract class LayoutBuilder {
    protected val widgets = mutableListOf<LayoutElement>()

    fun widget(widget: LayoutElement) {
        widgets.add(widget)
    }

    fun string(text: String) {
        widgets.add(Widgets.text(text))
    }

    fun display(display: Display) {
        widgets.add(display.asWidget())
    }

    fun string(component: Component) {
        widgets.add(Widgets.text(component))
    }

    fun spacer(width: Int = 0, height: Int = 0) {
        widgets.add(SpacerElement(width, height))
    }

    fun vertical(spacing: Int = 0, alignment: Float = 0f, builder: VerticalLayoutBuilder.() -> Unit) {
        val builder = VerticalLayoutBuilder()
        builder.builder()
        widgets.add(builder.build(spacing, alignment))
    }

    fun horizontal(spacing: Int = 0, alignment: Float = 0f, builder: HorizontalLayoutBuilder.() -> Unit) {
        val builder = HorizontalLayoutBuilder()
        builder.builder()
        widgets.add(builder.build(spacing, alignment))
    }

    abstract fun build(spacing: Int = 0, alignment: Float): LinearLayout

    companion object {
        fun LinearLayout.setPos(x: Int, y: Int): LinearLayout {
            this.setPosition(x, y)
            return this
        }
    }
}

class VerticalLayoutBuilder : LayoutBuilder() {
    override fun build(spacing: Int, alignment: Float): LinearLayout {
        val layout = LinearLayout.vertical().spacing(spacing)
        widgets.forEach { layout.addChild(it, layout.newCellSettings().alignHorizontally(alignment)) }
        layout.arrangeElements()
        return layout
    }
}

class HorizontalLayoutBuilder : LayoutBuilder() {
    override fun build(spacing: Int, alignment: Float): LinearLayout {
        val layout = LinearLayout.horizontal().spacing(spacing)
        widgets.forEach { layout.addChild(it, layout.newCellSettings().alignVertically(alignment)) }
        layout.arrangeElements()
        return layout
    }
}
