package tech.thatgravyboat.skyblockpv.screens.tabs.inventory

import com.mojang.authlib.GameProfile
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.utils.displays.Display

class WardrobeScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BasePagedInventoryScreen(gameProfile, profile) {
    private val inventory get() = profile?.inventory
    private val activeArmor get() = inventory?.armorItems?.inventory.orEmpty(4)
    private val wardrobe get() = inventory?.wardrobe
    private val armor get() = wardrobe?.armor?.armor?.inventory?.chunked(36) ?: emptyList()

    // todo: hypixel doesnt send your active wardrobe armor
    private fun handleActiveArmor(): List<ItemStack> {
        return emptyList()
    }

    override fun getInventories(): List<Display> = armor.map { createInventory(it) }

    override fun getIcons() = List(armor.size) { Items.LEATHER_CHESTPLATE.defaultInstance }
}
