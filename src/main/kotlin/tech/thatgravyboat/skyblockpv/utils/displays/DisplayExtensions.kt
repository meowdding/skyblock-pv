package tech.thatgravyboat.skyblockpv.utils.displays

import net.minecraft.network.chat.Component
import tech.thatgravyboat.lib.displays.Display
import tech.thatgravyboat.lib.displays.Displays

fun Display.withTranslatedTooltip(key: String, vararg args: Any?): Display {
    return Displays.tooltip(this, Component.translatable(key, args))
}

fun Display.withDropdown(display: Display, context: DropdownContext): Display = ExtraDisplays.dropdown(this, display, context)

