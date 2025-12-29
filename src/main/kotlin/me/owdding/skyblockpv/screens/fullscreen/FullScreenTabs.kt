package me.owdding.skyblockpv.screens.fullscreen

import me.owdding.skyblockpv.screens.fullscreen.tabs.main.MainTab
import net.minecraft.network.chat.Component
import tech.thatgravyboat.skyblockapi.utils.text.Text

enum class FullScreenTabs(tab: FullScreenTab, name: Component) {
    MAIN(MainTab, Text.of("Main"))
}
