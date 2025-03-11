package tech.thatgravyboat.skyblockpv.screens.tabs.inventory

import com.mojang.authlib.GameProfile
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.utils.LayoutBuild
import tech.thatgravyboat.skyblockpv.utils.Utils.center
import tech.thatgravyboat.skyblockpv.utils.displays.asWidget

class ItemVaultScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseInventoryScreen(gameProfile, profile) {
    override fun createInventoryWidget() = LayoutBuild.horizontal {
        val inventoryItems = profile?.inventory?.personalVault?.inventory.orEmpty(36)
        widget(createInventory(inventoryItems).asWidget().center(-1, height))
    }
}
