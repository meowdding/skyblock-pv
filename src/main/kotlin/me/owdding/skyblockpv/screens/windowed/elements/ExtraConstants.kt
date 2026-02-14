package me.owdding.skyblockpv.screens.windowed.elements

import me.owdding.skyblockpv.SkyBlockPv.id
import me.owdding.skyblockpv.SkyBlockPv.olympusId
import me.owdding.skyblockpv.utils.theme.ThemeSupport.ThemedWidgetSprites
import me.owdding.skyblockpv.utils.theme.ThemeSupport.withThemeSupport

object ExtraConstants {
    val BUTTON_DARK = ThemedWidgetSprites(olympusId("buttons/dark/normal"), id("buttons/disabled"), olympusId("buttons/dark/hovered")).withThemeSupport()
    val TEXTBOX = ThemedWidgetSprites(olympusId("textbox/normal"), olympusId("textbox/disabled"), olympusId("textbox/hovered"))

    val BUTTON_DARK_OPAQUE = ThemedWidgetSprites(id("opaque/buttons/dark/normal"), id("opaque/buttons/disabled"), id("opaque/buttons/dark/hovered"))
    val BUTTON_PRIMARY_OPAQUE = ThemedWidgetSprites(id("opaque/buttons/primary/normal"), id("opaque/buttons/disabled"), id("opaque/buttons/primary/hovered"))

    var TAB_TOP = ThemedWidgetSprites(id("tabs/top/normal"), id("tabs/top/normal"), id("tabs/top/hovered"))
    var TAB_TOP_OFFSET = ThemedWidgetSprites(id("tabs/top/normal_offset"), id("tabs/top/normal_offset"), id("tabs/top/hovered_offset"))
    var TAB_TOP_SELECTED = ThemedWidgetSprites(id("tabs/top/selected"), id("tabs/top/selected"), id("tabs/top/selected_hovered"))

    var TAB_RIGHT = ThemedWidgetSprites(id("tabs/right/normal"), id("tabs/right/normal"), id("tabs/right/hovered"))
    var TAB_RIGHT_SELECTED = ThemedWidgetSprites(id("tabs/right/selected"), id("tabs/right/selected"), id("tabs/right/selected_hovered"))

    var TAB_LEFT = ThemedWidgetSprites(id("tabs/left/normal"), id("tabs/left/normal"), id("tabs/left/hovered"))
    var TAB_LEFT_SELECTED = ThemedWidgetSprites(id("tabs/left/selected"), id("tabs/left/selected"), id("tabs/left/selected_hovered"))
}
