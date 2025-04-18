package tech.thatgravyboat.skyblockpv.screens.tabs.rift

import com.mojang.authlib.GameProfile
import tech.thatgravyboat.lib.builder.LayoutBuild
import tech.thatgravyboat.lib.displays.DisplayWidget
import tech.thatgravyboat.lib.displays.Displays
import tech.thatgravyboat.lib.displays.asWidget
import tech.thatgravyboat.lib.displays.centerIn
import tech.thatgravyboat.lib.displays.toRow
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.utils.LayoutUtils.center
import tech.thatgravyboat.skyblockpv.utils.components.PvWidgets
import tech.thatgravyboat.skyblockpv.utils.displays.*

class RiftInventoryScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseRiftScreen(gameProfile, profile) {
    override fun getLayout(bg: DisplayWidget) = LayoutBuild.horizontal {
        val inventory = profile?.rift?.inventory ?: return@horizontal
        val armor = inventory.armor
        val equipment = inventory.equipment

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
        val inventoryItems = inventory.inventory.chunked(9)
        val reorderedItems = (inventoryItems.drop(1) + inventoryItems.take(1)).flatten()
        widget(PvWidgets.createInventory(reorderedItems).asWidget().center(-1, height))
    }
}
