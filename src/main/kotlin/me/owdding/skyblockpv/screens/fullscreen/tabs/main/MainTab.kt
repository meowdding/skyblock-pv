package me.owdding.skyblockpv.screens.fullscreen.tabs.main

import earth.terrarium.olympus.client.components.Widgets
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.screens.fullscreen.BaseFullScreenPvScreen
import me.owdding.skyblockpv.screens.fullscreen.FullScreenTab
import net.minecraft.client.gui.layouts.GridLayout

object MainTab : FullScreenTab {
    context(screen: BaseFullScreenPvScreen, profile: SkyBlockProfile)
    override fun create(x: Int, y: Int, width: Int, height: Int) {
        val layout = GridLayout(x, y)
        val rows = layout.createRowHelper(4)
        rows.addChild(Widgets.button { it.withSize(60, 20) }, 3)
        rows.addChild(Widgets.button { it.withSize(80, 20) }, 4)
        layout.arrangeElements()
        layout.applyLayout()
    }
}
