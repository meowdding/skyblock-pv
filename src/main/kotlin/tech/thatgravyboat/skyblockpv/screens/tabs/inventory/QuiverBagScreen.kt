package tech.thatgravyboat.skyblockpv.screens.tabs.inventory

import com.mojang.authlib.GameProfile
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.utils.LayoutBuild
import tech.thatgravyboat.skyblockpv.utils.LayoutUtils.center
import tech.thatgravyboat.skyblockpv.utils.displays.asWidget

class QuiverBagScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseInventoryScreen(gameProfile, profile) {
    override fun getLayout() = LayoutBuild.horizontal {
        val inventoryItems = profile?.inventory?.quiver?.inventory.orEmpty(45)
        widget(createInventory(inventoryItems).asWidget().center(-1, height))
    }
}
