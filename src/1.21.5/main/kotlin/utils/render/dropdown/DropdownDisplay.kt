package me.owdding.skyblockpv.utils.render.dropdown

import me.owdding.lib.displays.Display
import me.owdding.lib.displays.Displays.isMouseOver
import me.owdding.skyblockpv.utils.accessors.withExclusiveScissor
import me.owdding.skyblockpv.utils.displays.DropdownContext
import net.minecraft.client.gui.GuiGraphics
import tech.thatgravyboat.skyblockapi.helpers.McClient
import utils.extensions.pushPop
import utils.extensions.translate

actual fun createDropdownDisplay(original: Display, dropdown: Display, context: DropdownContext): Display = DropdownDisplay(original, dropdown, context)

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
                graphics.pushPop {
                    graphics.pose().translate(0, 0, 201)
                    dropdown.render(graphics)
                }
            } else {
                if (context.currentDropdown === this) {
                    context.currentDropdown = null
                }
                isOpen = false
            }
        }
    }
}
