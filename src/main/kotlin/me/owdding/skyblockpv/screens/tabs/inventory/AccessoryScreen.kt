package me.owdding.skyblockpv.screens.tabs.inventory

import com.mojang.authlib.GameProfile
import me.owdding.skyblockpv.api.data.SkyBlockProfile
import me.owdding.skyblockpv.utils.components.PvWidgets
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.lib.displays.Display

class AccessoryScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BasePagedInventoryScreen(gameProfile, profile) {
    private val accessories get() = profile?.inventory?.talismans ?: emptyList()

    override fun getInventories(): List<Display> = accessories.map { PvWidgets.createInventory(it.talismans.inventory) }

    override fun getIcons(): List<ItemStack> = List(accessories.size) { ItemStack.EMPTY }
}
