package me.owdding.skyblockpv.screens.windowed.tabs.inventory

import com.mojang.authlib.GameProfile
import me.owdding.lib.displays.Display
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.utils.components.PvWidgets
import net.minecraft.world.item.ItemStack

class BackpackScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BasePagedInventoryScreen(gameProfile, profile) {
    private val backpacks get() = profile.inventory?.backpacks ?: emptyList()

    override fun getInventories(): List<Display> = backpacks.map { PvWidgets.createInventory(it.items) }

    override fun getIcons(): List<ItemStack> = backpacks.map { it.icon }
}
