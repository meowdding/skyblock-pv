package me.owdding.skyblockpv.screens.windowed.tabs.inventory

import com.mojang.authlib.GameProfile
import me.owdding.lib.displays.Display
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.utils.components.PvWidgets
import net.minecraft.world.item.Items

class EnderChestScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BasePagedInventoryScreen(gameProfile, profile) {
    private val enderChests get() = profile.inventory?.enderChestPages ?: emptyList()

    override fun getInventories(): List<Display> = enderChests.map { PvWidgets.createInventory(it) }

    override fun getIcons() = List(enderChests.size) { Items.ENDER_CHEST.defaultInstance }
}
