package me.owdding.skyblockpv.screens.windowed.tabs.inventory

import com.mojang.authlib.GameProfile
import me.owdding.lib.displays.Display
import me.owdding.skyblockpv.api.data.InventoryData.Backpack
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.utils.components.PvWidgets
import net.minecraft.world.item.ItemStack

class BackpackScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BasePagedInventoryScreen<List<Backpack>>(gameProfile, profile) {
    override fun getRawInventory(): List<Backpack>? = profile.inventory?.backpacks
    override fun List<Backpack>.getInventories(): List<Display> = map { PvWidgets.createInventory(it.items) }
    override fun List<Backpack>.getIcons(): List<ItemStack> = map { it.icon }
}
