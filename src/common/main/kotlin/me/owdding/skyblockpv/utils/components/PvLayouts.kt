package me.owdding.skyblockpv.utils.components

import me.owdding.lib.builder.LayoutBuilder
import me.owdding.lib.displays.Display
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import me.owdding.lib.builder.FrameLayoutBuilder as MLibFrameLayoutBuilder
import me.owdding.lib.builder.HorizontalLayoutBuilder as MLibHorizontalLayoutBuilder
import me.owdding.lib.builder.VerticalLayoutBuilder as MLibVerticalLayoutBuilder

object PvLayouts {
    fun vertical(spacing: Int = 0, alignment: Float = 0f, builder: LayoutBuilder.() -> Unit): Layout {
        val builder = VerticalLayoutBuilder()
        builder.builder()
        return builder.build(spacing, alignment)
    }

    fun horizontal(spacing: Int = 0, alignment: Float = 0f, builder: LayoutBuilder.() -> Unit): Layout {
        val builder = HorizontalLayoutBuilder()
        builder.builder()
        return builder.build(spacing, alignment)
    }

    fun frame(width: Int = 0, height: Int = 0, builder: LayoutBuilder.() -> Unit): Layout {
        val builder = FrameLayoutBuilder(width, height)
        builder.builder()
        return builder.build()
    }

    fun empty() = frame {}
}

class FrameLayoutBuilder(width: Int, height: Int) : MLibFrameLayoutBuilder(width, height) {
    override fun string(text: String) {
        widget(PvWidgets.text(text))
    }

    override fun string(component: Component) {
        widget(PvWidgets.text(component))
    }

    override fun vertical(spacing: Int, alignment: Float, builder: MLibVerticalLayoutBuilder.() -> Unit) {
        val builder = VerticalLayoutBuilder()
        builder.builder()
        widget(builder.build(spacing, alignment))
    }

    override fun horizontal(spacing: Int, alignment: Float, builder: MLibHorizontalLayoutBuilder.() -> Unit) {
        val builder = HorizontalLayoutBuilder()
        builder.builder()
        widget(builder.build(spacing, alignment))
    }

    @Deprecated("Use one of the other text things")
    override fun textDisplay(
        text: String,
        color: UInt,
        shadow: Boolean,
        displayModifier: Display.() -> Display,
        init: MutableComponent.() -> Unit,
    ) {
        super.textDisplay(text, color, shadow, displayModifier, init)
    }

    @Deprecated("Use one of the other text things")
    override fun textDisplay(text: String, color: UInt, shadow: Boolean, init: MutableComponent.() -> Unit) {
        super.textDisplay(text, color, shadow, init)
    }
}

class VerticalLayoutBuilder : MLibVerticalLayoutBuilder() {
    override fun string(text: String) {
        widget(PvWidgets.text(text))
    }

    override fun string(component: Component) {
        widget(PvWidgets.text(component))
    }

    override fun vertical(spacing: Int, alignment: Float, builder: MLibVerticalLayoutBuilder.() -> Unit) {
        val builder = VerticalLayoutBuilder()
        builder.builder()
        widget(builder.build(spacing, alignment))
    }

    override fun horizontal(spacing: Int, alignment: Float, builder: MLibHorizontalLayoutBuilder.() -> Unit) {
        val builder = HorizontalLayoutBuilder()
        builder.builder()
        widget(builder.build(spacing, alignment))
    }

    @Deprecated("Use one of the other text things")
    override fun textDisplay(
        text: String,
        color: UInt,
        shadow: Boolean,
        displayModifier: Display.() -> Display,
        init: MutableComponent.() -> Unit,
    ) {
        super.textDisplay(text, color, shadow, displayModifier, init)
    }

    @Deprecated("Use one of the other text things")
    override fun textDisplay(text: String, color: UInt, shadow: Boolean, init: MutableComponent.() -> Unit) {
        super.textDisplay(text, color, shadow, init)
    }
}

class HorizontalLayoutBuilder : MLibHorizontalLayoutBuilder() {
    override fun string(text: String) {
        widget(PvWidgets.text(text))
    }

    override fun string(component: Component) {
        widget(PvWidgets.text(component))
    }

    override fun vertical(spacing: Int, alignment: Float, builder: MLibVerticalLayoutBuilder.() -> Unit) {
        val builder = VerticalLayoutBuilder()
        builder.builder()
        widget(builder.build(spacing, alignment))
    }

    override fun horizontal(spacing: Int, alignment: Float, builder: MLibHorizontalLayoutBuilder.() -> Unit) {
        val builder = HorizontalLayoutBuilder()
        builder(builder)
        widget(builder.build(spacing, alignment))
    }

    @Deprecated("Use one of the other text things")
    override fun textDisplay(
        text: String,
        color: UInt,
        shadow: Boolean,
        displayModifier: Display.() -> Display,
        init: MutableComponent.() -> Unit,
    ) {
        super.textDisplay(text, color, shadow, displayModifier, init)
    }

    @Deprecated("Use one of the other text things")
    override fun textDisplay(text: String, color: UInt, shadow: Boolean, init: MutableComponent.() -> Unit) {
        super.textDisplay(text, color, shadow, init)
    }
}

