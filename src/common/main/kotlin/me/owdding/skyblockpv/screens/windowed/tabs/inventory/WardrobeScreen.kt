package me.owdding.skyblockpv.screens.windowed.tabs.inventory

import com.mojang.authlib.GameProfile
import me.owdding.lib.displays.Display
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.utils.components.PvWidgets
import me.owdding.skyblockpv.utils.displays.ExtraDisplays
import net.minecraft.world.item.Items

class WardrobeScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BasePagedInventoryScreen(gameProfile, profile) {

    private val inventory get() = profile.inventory
    private val activeArmor get() = inventory?.armorItems.orEmpty(4).asReversed()
    private val wardrobe get() = inventory?.wardrobe
    private val selected get() = wardrobe?.equippedArmor?.takeUnless { it == -1 }?.minus(1) ?: -1
    private val armor get() = wardrobe?.chunked(36) ?: emptyList()

    override fun getExtraLine() = if (selected == -1) {
        "No Armor Selected"
    } else {
        "Selected Armor: ${selected + 1}"
    }.let { ExtraDisplays.grayText(it) }

    override fun getInventories(): List<Display> = armor.mapIndexed { index, inventory ->
        if (selected != -1 && selected / 9 == index) {
            inventory.chunked(9).mapIndexed { index, row ->
                val row = row.toMutableList()
                row[selected % 9] = activeArmor[index]
                row
            }.flatten()
        } else {
            inventory
        }.let { PvWidgets.createInventory(it) }
    }

    override fun getIcons() = List(armor.size) { Items.LEATHER_CHESTPLATE.defaultInstance }
}
