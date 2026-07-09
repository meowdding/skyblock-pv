package me.owdding.skyblockpv.screens.windowed.tabs.inventory

import com.mojang.authlib.GameProfile
import me.owdding.lib.displays.Display
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.utils.components.PvWidgets
import me.owdding.skyblockpv.utils.displays.ExtraDisplays
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

class EquipmentScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BasePagedInventoryScreen<List<List<ItemStack>>>(gameProfile, profile) {
    private val inventory get() = profile.inventory
    private val activeArmor get() = inventory?.equipmentItems.orEmpty(4).asReversed()

    private val loadouts get() = inventory?.loadouts
    private val selected get() = loadouts?.equippedEquipmentSet?.takeUnless { it == -1 }?.minus(1) ?: -1

    override fun getRawInventory(): List<List<ItemStack>>? {
        val equipmentSets = loadouts?.equipmentSets ?: return null
        val pages = mutableListOf<List<ItemStack>>()

        val maxSetId = equipmentSets.keys.maxOrNull() ?: 0
        val totalPages = (maxSetId / 9) + 1

        for (page in 0 until totalPages) {
            val pageItems = mutableListOf<ItemStack>()
            for (row in 0..3) {
                for (col in 1..9) {
                    val setId = (page * 9) + col
                    val equipmentSet = equipmentSets[setId]

                    val item = when (row) {
                        0 -> equipmentSet?.slot1
                        1 -> equipmentSet?.slot2
                        2 -> equipmentSet?.slot3
                        3 -> equipmentSet?.slot4
                        else -> null
                    } ?: ItemStack.EMPTY

                    pageItems.add(item)
                }
            }
            pages.add(pageItems)
        }
        return pages.takeIf { it.isNotEmpty() }
    }

    override fun getExtraLine() = if (selected == -1) {
        "No Equipment Selected"
    } else {
        "Selected Equipment: ${selected + 1}"
    }.let { ExtraDisplays.grayText(it) }

    override fun List<List<ItemStack>>.getInventories(): List<Display> = mapIndexed { index, inventory ->
        if (selected != -1 && selected / 9 == index) {
            inventory.chunked(9).flatMapIndexed { rowIndex, row ->
                val mutableRow = row.toMutableList()
                mutableRow[selected % 9] = activeArmor.getOrElse(rowIndex) { ItemStack.EMPTY }
                mutableRow
            }
        } else {
            inventory
        }.let { PvWidgets.createInventory(it) }
    }

    override fun List<List<ItemStack>>.getIcons() = List(size) { Items.HARNESS.brown().defaultInstance }
}
