package tech.thatgravyboat.skyblockpv.screens.tabs.inventory

import com.mojang.authlib.GameProfile
import tech.thatgravyboat.lib.builder.LayoutBuild
import tech.thatgravyboat.lib.displays.DisplayWidget
import tech.thatgravyboat.lib.displays.asWidget
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.utils.LayoutUtils.center
import tech.thatgravyboat.skyblockpv.utils.components.PvWidgets

class FishingBagScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseInventoryScreen(gameProfile, profile) {
    override fun getLayout(bg: DisplayWidget) = LayoutBuild.horizontal {
        val inventoryItems = profile?.inventory?.fishingBag?.inventory.orEmpty(45)
        widget(PvWidgets.createInventory(inventoryItems).asWidget().center(-1, height))
    }
}
