package tech.thatgravyboat.skyblockpv.screens.tabs.inventory

import com.mojang.authlib.GameProfile
import tech.thatgravyboat.skyblockpv.SkyBlockPv
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.utils.LayoutBuild
import tech.thatgravyboat.skyblockpv.utils.Utils.center
import tech.thatgravyboat.skyblockpv.utils.displays.*

class InventoryScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseInventoryScreen(gameProfile, profile) {
    override fun getLayout() = LayoutBuild.horizontal {
        val inventory = profile?.inventory ?: return@horizontal
        val armor = inventory.armorItems?.inventory.orEmpty(4)
        val equipment = inventory.equipmentItems?.inventory.orEmpty(4)

        spacer(10)
        val armorAndEquipment = listOf(
            armor.reversed().map { Displays.padding(2, Displays.item(it, showTooltip = true, showStackSize = true)) }.toColumn(),
            equipment.map { Displays.padding(2, Displays.item(it, showTooltip = true, showStackSize = true)) }.toColumn(),
        ).toRow()

        display(
            Displays.background(
                SkyBlockPv.id("inventory/inventory-2x4"),
                Displays.padding(2, armorAndEquipment),
            ).centerIn(-1, height),
        )

        spacer(width = 10)
        val inventoryItems = inventory.inventoryItems?.inventory.orEmpty(36).chunked(9)
        val reorderedItems = (inventoryItems.drop(1) + inventoryItems.take(1)).flatten()
        widget(createInventory(reorderedItems).asWidget().center(-1, height))
    }
}
