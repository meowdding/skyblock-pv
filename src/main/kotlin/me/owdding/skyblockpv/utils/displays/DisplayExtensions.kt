package me.owdding.skyblockpv.utils.displays

import me.owdding.lib.displays.Display
import me.owdding.lib.displays.Displays
import net.minecraft.network.chat.Component

fun Display.withTranslatedTooltip(key: String, vararg args: Any?): Display {
    return Displays.tooltip(this, Component.translatable(key, args))
}

fun Display.withDropdown(display: Display, context: DropdownContext): Display = ExtraDisplays.dropdown(this, display, context)

