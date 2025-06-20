package me.owdding.skyblockpv.utils

import earth.terrarium.olympus.client.components.base.renderer.WidgetRenderer
import me.owdding.lib.displays.Display
import me.owdding.skyblockpv.SkyBlockPv
import net.minecraft.client.gui.components.AbstractWidget
import tech.thatgravyboat.skyblockapi.helpers.McFont

object ExtraWidgetRenderers {

    fun <T : AbstractWidget> conditional(ifTrue: WidgetRenderer<T>, ifFalse: WidgetRenderer<T>, condition: () -> Boolean): WidgetRenderer<T> =
        WidgetRenderer<T> { graphics, context, partialTicks ->
            if (SkyBlockPv.isDevMode) {
                graphics.drawString(McFont.self, context.mouseX.toString(), 0, 150, -1)
            }

            if (condition()) ifTrue.render(graphics, context, partialTicks) else ifFalse.render(graphics, context, partialTicks)
        }

    fun <T : AbstractWidget> display(display: Display) = WidgetRenderer<T> { graphics, context, partialTicks ->
        display.render(graphics, context.x, context.y)
    }
}
