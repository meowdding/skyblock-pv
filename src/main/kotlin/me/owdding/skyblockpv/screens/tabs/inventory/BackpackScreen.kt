package me.owdding.skyblockpv.screens.tabs.inventory

import com.mojang.authlib.GameProfile
import me.owdding.skyblockpv.api.data.SkyBlockProfile
import me.owdding.skyblockpv.utils.components.PvWidgets
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.lib.displays.Display

class BackpackScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BasePagedInventoryScreen(gameProfile, profile) {
    private val backpacks get() = profile?.inventory?.backpacks ?: emptyList()

    override fun getInventories(): List<Display> = backpacks.map { PvWidgets.createInventory(it.items.inventory) }

    override fun getIcons(): List<ItemStack> = backpacks.map { it.icon }
}
