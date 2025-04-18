package tech.thatgravyboat.skyblockpv.screens.tabs.inventory

import com.mojang.authlib.GameProfile
import tech.thatgravyboat.lib.builder.LayoutBuild
import tech.thatgravyboat.lib.displays.*
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.utils.LayoutUtils.center
import tech.thatgravyboat.skyblockpv.utils.components.PvWidgets
import tech.thatgravyboat.skyblockpv.utils.displays.ExtraDisplays

class InventoryScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseInventoryScreen(gameProfile, profile) {
    override fun getLayout(bg: DisplayWidget) = LayoutBuild.horizontal {
        val inventory = profile?.inventory ?: return@horizontal
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
