package tech.thatgravyboat.skyblockpv.screens.tabs.inventory

import com.mojang.authlib.GameProfile
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.utils.displays.Display

class AccessoryScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BasePagedInventoryScreen(gameProfile, profile) {
    private val accessories get() = profile?.inventory?.talismans ?: emptyList()

    override fun getInventories(): List<Display> = accessories.map { createInventory(it.talismans.inventory) }

    override fun getIcons(): List<ItemStack> = List(accessories.size) { ItemStack.EMPTY }
}
