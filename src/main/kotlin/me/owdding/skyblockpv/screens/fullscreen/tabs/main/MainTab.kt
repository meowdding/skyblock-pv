package me.owdding.skyblockpv.screens.fullscreen.tabs.main

import me.owdding.lib.builder.LayoutFactory
import me.owdding.lib.displays.Displays
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.screens.fullscreen.BaseFullScreenPvScreen
import me.owdding.skyblockpv.screens.fullscreen.FullScreenTab

object MainTab : FullScreenTab {
    context(screen: BaseFullScreenPvScreen, profile: SkyBlockProfile)
    override fun create(x: Int, y: Int, width: Int, height: Int) {
        LayoutFactory.vertical {
            display(Displays.background(0x80000000u, Displays.empty(10, 10)))
        }.applyLayout(x, y)
    }
}
