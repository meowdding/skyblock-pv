package me.owdding.skyblockpv.utils.render.dropdown

import me.owdding.lib.displays.Display
import me.owdding.skyblockpv.utils.displays.DropdownContext
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.util.ARGB
import tech.thatgravyboat.skyblockapi.platform.pushPop

actual fun createDropdownOverlay(original: Display, color: Int, context: DropdownContext): Display = DropdownOverlay(original, color, context)

class DropdownOverlay(val original: Display, val color: Int, val context: DropdownContext) : Display {
    override fun getWidth() = original.getWidth()
    override fun getHeight() = original.getHeight()
    override fun render(graphics: GuiGraphics) {
        original.render(graphics)
        graphics.pushPop {
            val difference = System.currentTimeMillis() - context.lastUpdated
            if (context.currentDropdown != null) {
                if (difference < context.fadeTime) {
                    val value = ((difference / context.fadeTime.toDouble()) * 255).toInt()
                    graphics.fill(0, 0, getWidth(), getHeight(), ARGB.multiply(color, ARGB.color(value, value, value, value)))
                } else {
                    graphics.fill(0, 0, getWidth(), getHeight(), color)
                }
                context.dorpdownDisplay?.invoke()
            } else if (difference < context.fadeTime) {
                val value = ((1 - (difference / context.fadeTime.toDouble())) * 255).toInt()
                graphics.fill(0, 0, getWidth(), getHeight(), ARGB.multiply(color, ARGB.color(value, value, value, value)))
            }
        }
    }
}
