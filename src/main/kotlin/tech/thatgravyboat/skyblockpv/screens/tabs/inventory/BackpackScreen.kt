package tech.thatgravyboat.skyblockpv.screens.tabs.inventory

import com.mojang.authlib.GameProfile
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.utils.displays.Display

class BackpackScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BasePagedInventoryScreen(gameProfile, profile) {
    private val backpacks get() = profile?.inventory?.backpacks ?: emptyList()

    override fun getInventories(): List<Display> = backpacks.map { createInventory(it.items.inventory) }

    override fun getIcons(): List<ItemStack> = backpacks.map { it.icon }
}
