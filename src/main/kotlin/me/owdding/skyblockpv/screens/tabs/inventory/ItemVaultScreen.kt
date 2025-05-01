package me.owdding.skyblockpv.screens.tabs.inventory

import com.mojang.authlib.GameProfile
import me.owdding.lib.builder.LayoutFactory
import me.owdding.lib.displays.DisplayWidget
import me.owdding.lib.displays.asWidget
import me.owdding.skyblockpv.api.data.SkyBlockProfile
import me.owdding.skyblockpv.utils.LayoutUtils.center
import me.owdding.skyblockpv.utils.components.PvWidgets

class ItemVaultScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseInventoryScreen(gameProfile, profile) {
    override fun getLayout(bg: DisplayWidget) = LayoutFactory.horizontal {
        val inventoryItems = profile?.inventory?.personalVault?.inventory.orEmpty(36)
        widget(PvWidgets.createInventory(inventoryItems).asWidget().center(-1, height))
    }
}
