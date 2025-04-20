package me.owdding.skyblockpv.screens.tabs.inventory

import com.mojang.authlib.GameProfile
import me.owdding.skyblockpv.api.data.SkyBlockProfile
import me.owdding.skyblockpv.utils.components.PvWidgets
import net.minecraft.world.item.Items
import tech.thatgravyboat.lib.displays.Display

class EnderChestScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BasePagedInventoryScreen(gameProfile, profile) {
    private val enderChests get() = profile?.inventory?.enderChestPages ?: emptyList()

    override fun getInventories(): List<Display> = enderChests.map { PvWidgets.createInventory(it.items.inventory) }

    override fun getIcons() = List(enderChests.size) { Items.ENDER_CHEST.defaultInstance }
}
