package tech.thatgravyboat.skyblockpv.screens.tabs.inventory

import com.mojang.authlib.GameProfile
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.utils.components.PvWidgets
import tech.thatgravyboat.skyblockpv.utils.displays.Display

class EnderChestScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BasePagedInventoryScreen(gameProfile, profile) {
    private val enderChests get() = profile?.inventory?.enderChestPages ?: emptyList()

    override fun getInventories(): List<Display> = enderChests.map { PvWidgets.createInventory(it.items.inventory) }

    override fun getIcons() = List(enderChests.size) { Items.ENDER_CHEST.defaultInstance }
}
