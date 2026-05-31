package me.owdding.skyblockpv.screens.windowed.tabs.inventory

import com.mojang.authlib.GameProfile
import me.owdding.lib.displays.Display
import me.owdding.skyblockpv.api.data.Inventory
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.utils.components.PvWidgets
import net.minecraft.world.item.Items

class EnderChestScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BasePagedInventoryScreen<List<Inventory>>(gameProfile, profile) {
    override fun getRawInventory(): List<Inventory>? = profile.inventory?.enderChestPages
    override fun List<Inventory>.getInventories(): List<Display> = map { PvWidgets.createInventory(it) }

    override fun List<Inventory>.getIcons() = List(size) { Items.ENDER_CHEST.defaultInstance }
}
