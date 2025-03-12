package tech.thatgravyboat.skyblockpv.screens.tabs.inventory

import com.mojang.authlib.GameProfile
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockpv.SkyBlockPv
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.screens.tabs.base.AbstractCategorizedScreen
import tech.thatgravyboat.skyblockpv.utils.displays.Display
import tech.thatgravyboat.skyblockpv.utils.displays.Displays
import tech.thatgravyboat.skyblockpv.utils.displays.asTable

abstract class BaseInventoryScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : AbstractCategorizedScreen("INVENTORY", gameProfile, profile) {
    override val categories get() = InventoryCategory.entries

    protected fun createInventory(items: List<ItemStack>): Display {
        val itemDisplays = items.chunked(9).map { chunk ->
            val updatedChunk = chunk + List(9 - chunk.size) { ItemStack.EMPTY }
            updatedChunk.map { item ->
                Displays.padding(2, Displays.item(item, showTooltip = true, showStackSize = true))
            }
        }
        return Displays.background(
            SkyBlockPv.id("inventory/inventory-9x${itemDisplays.size}"),
            Displays.padding(2, itemDisplays.asTable()),
        )
    }

    protected fun List<ItemStack>?.orEmpty(size: Int) = this ?: List(size) { ItemStack.EMPTY }
}
