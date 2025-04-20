package me.owdding.skyblockpv.screens.tabs.inventory

import com.mojang.authlib.GameProfile
import me.owdding.skyblockpv.api.data.SkyBlockProfile
import me.owdding.skyblockpv.utils.LayoutUtils.center
import me.owdding.skyblockpv.utils.components.PvWidgets
import tech.thatgravyboat.lib.builder.LayoutBuild
import tech.thatgravyboat.lib.displays.DisplayWidget
import tech.thatgravyboat.lib.displays.asWidget

class PotionBagScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseInventoryScreen(gameProfile, profile) {
    override fun getLayout(bg: DisplayWidget) = LayoutBuild.horizontal {
        val inventoryItems = profile?.inventory?.potionBag?.inventory.orEmpty(45)
        widget(PvWidgets.createInventory(inventoryItems).asWidget().center(-1, height))
    }
}
