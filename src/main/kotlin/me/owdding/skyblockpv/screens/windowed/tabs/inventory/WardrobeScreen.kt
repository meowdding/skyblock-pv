package me.owdding.skyblockpv.screens.windowed.tabs.inventory

import com.mojang.authlib.GameProfile
import me.owdding.lib.displays.Display
import me.owdding.skyblockpv.api.data.InventoryData
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.utils.components.PvWidgets
import me.owdding.skyblockpv.utils.displays.ExtraDisplays
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

class WardrobeScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BasePagedInventoryScreen<List<List<ItemStack>>>(gameProfile, profile) {

    private val inventory get() = profile.inventory
    private val activeArmor get() = inventory?.armorItems.orEmpty(4).asReversed()
    private val wardrobe get() = inventory?.wardrobe
    private val selected get() = wardrobe?.equippedArmor?.takeUnless { it == -1 }?.minus(1) ?: -1

    override fun getRawInventory(): List<List<ItemStack>>? = inventory?.wardrobe?.chunked(36)

    override fun getExtraLine() = if (selected == -1) {
        "No Armor Selected"
    } else {
        "Selected Armor: ${selected + 1}"
    }.let { ExtraDisplays.grayText(it) }

    override fun List<List<ItemStack>>.getInventories(): List<Display> = mapIndexed { index, inventory ->
        if (selected != -1 && selected / 9 == index) {
            inventory.chunked(9).flatMapIndexed { index, row ->
                val row = row.toMutableList()
                row[selected % 9] = activeArmor[index]
                row
            }
        } else {
            inventory
        }.let { PvWidgets.createInventory(it) }
    }

    override fun List<List<ItemStack>>.getIcons() = List(size) { Items.LEATHER_CHESTPLATE.defaultInstance }
}
