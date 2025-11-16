package me.owdding.skyblockpv.screens.windowed.tabs.inventory

import com.mojang.authlib.GameProfile
import me.owdding.lib.displays.DisplayWidget
import me.owdding.lib.displays.Displays
import me.owdding.lib.displays.asWidget
import me.owdding.lib.displays.toRow
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.utils.components.PvLayouts
import me.owdding.skyblockpv.utils.components.PvWidgets
import me.owdding.skyblockpv.utils.displays.ExtraDisplays

class InventoryScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseInventoryScreen(gameProfile, profile) {
    override fun getLayout(bg: DisplayWidget) = PvLayouts.horizontal(10) {
        val inventory = profile.inventory ?: return@horizontal
        val armor = inventory.armorItems?.inventory.orEmpty(4)
        val equipment = inventory.equipmentItems?.inventory.orEmpty(4)

        val armorAndEquipment = listOf(
            PvWidgets.orderedArmorDisplay(armor.reversed()),
            PvWidgets.orderedEquipmentDisplay(equipment),
        ).toRow()

        display(
            ExtraDisplays.inventoryBackground(
                2, 4,
                Displays.padding(2, armorAndEquipment),
            ),
        )

        val inventoryItems = inventory.inventoryItems?.inventory.orEmpty(36).chunked(9)
        val reorderedItems = (inventoryItems.drop(1) + inventoryItems.take(1)).flatten()
        widget(PvWidgets.createInventory(reorderedItems).asWidget())
    }
}
