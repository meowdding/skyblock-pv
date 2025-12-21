package me.owdding.skyblockpv.utils.render.dropdown

import me.owdding.lib.displays.Display
import me.owdding.lib.displays.Displays.isMouseOver
import me.owdding.skyblockpv.utils.Utils.copy
import me.owdding.skyblockpv.utils.accessors.withExclusiveScissor
import me.owdding.skyblockpv.utils.displays.DropdownContext
import net.minecraft.client.gui.GuiGraphics
import tech.thatgravyboat.skyblockapi.helpers.McClient

fun createDropdownDisplay(original: Display, dropdown: Display, context: DropdownContext): Display = DropdownDisplay(original, dropdown, context)

class DropdownDisplay(val original: Display, val dropdown: Display, val context: DropdownContext) : Display {
    override fun getWidth() = original.getWidth()
    override fun getHeight() = original.getHeight()
    var isOpen = false
    override fun render(graphics: GuiGraphics) {
        original.render(graphics)

        graphics.withExclusiveScissor(-0, 0, McClient.window.width, McClient.window.height) {
            if (context.isCurrentDropdown(this) && (isMouseOver(original, graphics) || (isOpen && isMouseOver(dropdown, graphics)))) {
                isOpen = true
                context.currentDropdown = this
                val copy = graphics.pose().copy()
                dropdown.render(graphics)
                context.dorpdownDisplay = {
                    val previous = graphics.pose().copy()
                    graphics.pose().set(copy)
                    dropdown.render(graphics)
                    graphics.pose().set(previous)
                }
            } else {
                if (context.currentDropdown === this) {
                    context.currentDropdown = null
                    context.dorpdownDisplay = null
                }
                isOpen = false
            }
        }
    }
}
