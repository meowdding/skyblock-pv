package me.owdding.skyblockpv.screens.tabs.inventory

import com.mojang.authlib.GameProfile
import me.owdding.lib.builder.LayoutFactory
import me.owdding.lib.displays.*
import me.owdding.skyblockpv.api.data.SkyBlockProfile
import me.owdding.skyblockpv.utils.LayoutUtils.center
import me.owdding.skyblockpv.utils.components.PvWidgets
import me.owdding.skyblockpv.utils.displays.ExtraDisplays

class InventoryScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseInventoryScreen(gameProfile, profile) {
    override fun getLayout(bg: DisplayWidget) = LayoutFactory.horizontal {
        val inventory = profile.inventory ?: return@horizontal
        val armor = inventory.armorItems?.inventory.orEmpty(4)
        val equipment = inventory.equipmentItems?.inventory.orEmpty(4)

        spacer(10)
        val armorAndEquipment = listOf(
            PvWidgets.orderedArmorDisplay(armor.reversed()),
            PvWidgets.orderedEquipmentDisplay(equipment),
        ).toRow()

        display(
            ExtraDisplays.inventoryBackground(
                2, 4,
                Displays.padding(2, armorAndEquipment),
            ).centerIn(-1, height),
        )

        spacer(width = 10)
        val inventoryItems = inventory.inventoryItems?.inventory.orEmpty(36).chunked(9)
        val reorderedItems = (inventoryItems.drop(1) + inventoryItems.take(1)).flatten()
        widget(PvWidgets.createInventory(reorderedItems).asWidget().center(-1, height))
    }
}
