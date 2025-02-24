package tech.thatgravyboat.skyblockpv.utils

import earth.terrarium.olympus.client.components.Widgets
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.layouts.LinearLayout
import net.minecraft.network.chat.Component

object LayoutBuild {
    fun vertical(spacing: Int = 2, builder: VerticalLayoutBuilder.() -> Unit): LinearLayout {
        val builder = VerticalLayoutBuilder()
        builder.builder()
        return builder.build(spacing)
    }

    fun horizontal(spacing: Int = 0, builder: HorizontalLayoutBuilder.() -> Unit): LinearLayout {
        val builder = HorizontalLayoutBuilder()
        builder.builder()
        return builder.build(spacing)
    }
}

abstract class LayoutBuilder {
    protected val widgets = mutableListOf<AbstractWidget>()

    fun widget(widget: AbstractWidget) {
        widgets.add(widget)
    }

    fun string(text: String) {
        widgets.add(Widgets.text(text))
    }

    fun string(component: Component) {
        widgets.add(Widgets.text(component))
    }


    abstract fun build(spacing: Int = 0): LinearLayout
}

class VerticalLayoutBuilder : LayoutBuilder() {
    override fun build(spacing: Int): LinearLayout {
        val layout = LinearLayout.vertical().spacing(spacing)
        widgets.forEach { layout.addChild(it) }
        layout.arrangeElements()
        return layout
    }
}

class HorizontalLayoutBuilder : LayoutBuilder() {
    override fun build(spacing: Int): LinearLayout {
        val layout = LinearLayout.horizontal().spacing(spacing)
        widgets.forEach { layout.addChild(it) }
        layout.arrangeElements()
        return layout
    }
}
