package me.owdding.skyblockpv.utils.render.dropdown

import me.owdding.lib.displays.Display
import me.owdding.skyblockpv.utils.displays.DropdownContext
import net.msrandom.stub.Stub

@Stub
expect fun createDropdownDisplay(original: Display, dropdown: Display, context: DropdownContext): Display

@Stub
expect fun createDropdownOverlay(original: Display, color: Int, context: DropdownContext): Display
