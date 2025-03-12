package tech.thatgravyboat.skyblockpv.screens.tabs.inventory

import com.mojang.authlib.GameProfile
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.utils.displays.Display

class WardrobeScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BasePagedInventoryScreen(gameProfile, profile) {

    private val inventory get() = profile?.inventory
    private val activeArmor get() = inventory?.armorItems?.inventory.orEmpty(4).asReversed()
    private val wardrobe get() = inventory?.wardrobe
    private val selected get() = wardrobe?.equippedArmor?.minus(1) ?: -1
    private val armor get() = wardrobe?.armor?.armor?.inventory?.chunked(36) ?: emptyList()

    override fun getInventories(): List<Display> = armor.mapIndexed { index, inventory ->
        if (selected != -1 && selected / 9 == index) {
            inventory.chunked(9).mapIndexed { index, row ->
                var row = row.toMutableList()
                row[selected % 9] = activeArmor[index]
                row
            }.flatten()
        } else {
            inventory
        }.let { createInventory(it) }
    }

    override fun getIcons() = List(armor.size) { Items.LEATHER_CHESTPLATE.defaultInstance }
}
