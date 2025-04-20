package me.owdding.skyblockpv.screens.elements

import earth.terrarium.olympus.client.ui.UIConstants
import me.owdding.skyblockpv.SkyBlockPv.id
import net.minecraft.client.gui.components.WidgetSprites

object ExtraConstants {
    val BUTTON_DARK = WidgetSprites(UIConstants.id("buttons/dark/normal"), id("buttons/dark/disabled"), UIConstants.id("buttons/dark/hovered"))

    var TAB_TOP = WidgetSprites(id("tabs/top/normal"), id("tabs/top/normal"), id("tabs/top/hovered"))
    var TAB_TOP_SELECTED = WidgetSprites(id("tabs/top/selected"), id("tabs/top/selected"), id("tabs/top/selected_hovered"))

    var TAB_RIGHT = WidgetSprites(id("tabs/right/normal"), id("tabs/right/normal"), id("tabs/right/hovered"))
    var TAB_RIGHT_SELECTED = WidgetSprites(id("tabs/right/selected"), id("tabs/right/selected"), id("tabs/right/selected_hovered"))
}
