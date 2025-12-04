package me.owdding.skyblockpv.utils

import earth.terrarium.olympus.client.components.base.renderer.WidgetRenderer
import me.owdding.lib.displays.Display
import net.minecraft.client.gui.components.AbstractWidget

object ExtraWidgetRenderers {

    fun <T : AbstractWidget> conditional(ifTrue: WidgetRenderer<T>, ifFalse: WidgetRenderer<T>, condition: () -> Boolean): WidgetRenderer<T> =
        WidgetRenderer<T> { graphics, context, partialTicks ->
            if (condition())
                ifTrue.render(graphics, context, partialTicks)
            else
                ifFalse.render(graphics, context, partialTicks)
        }

    fun <T : AbstractWidget> display(display: Display) = WidgetRenderer<T> { graphics, context, partialTicks ->
        display.render(graphics, context.x, context.y)
    }

    fun <T : AbstractWidget> supplied(supplier: () -> WidgetRenderer<T>) = WidgetRenderer<T> { graphics, context, partial ->
        supplier().render(graphics, context, partial)
    }
}
