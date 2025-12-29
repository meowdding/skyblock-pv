package me.owdding.skyblockpv.screens.fullscreen

import me.owdding.lib.layouts.setPos
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import net.minecraft.client.gui.layouts.Layout

interface FullScreenTab {

    context(screen: BaseFullScreenPvScreen, profile: SkyBlockProfile) fun create(x: Int, y: Int, width: Int, height: Int)

    context(screen: BaseFullScreenPvScreen) fun Layout.applyLayout(x: Int, y: Int) {
        this.setPos(x, y).visitWidgets(screen::addRenderableWidget)
    }
}
