package tech.thatgravyboat.skyblockpv.screens.tabs.rift

import com.mojang.authlib.GameProfile
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.utils.LayoutBuild
import tech.thatgravyboat.skyblockpv.utils.LayoutUtils.center
import tech.thatgravyboat.skyblockpv.utils.components.PvWidgets
import tech.thatgravyboat.skyblockpv.utils.displays.*

class RiftInventoryScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseRiftScreen(gameProfile, profile) {
    override fun getLayout() = LayoutBuild.horizontal {
        val inventory = profile?.rift?.inventory ?: return@horizontal
        val armor = inventory.armor
        val equipment = inventory.equipment

        spacer(10)
        val armorAndEquipment = listOf(
            armor.reversed().map { Displays.padding(2, Displays.item(it, showTooltip = true, showStackSize = true)) }.toColumn(),
            equipment.map { Displays.padding(2, Displays.item(it, showTooltip = true, showStackSize = true)) }.toColumn(),
        ).toRow()

        display(
            Displays.inventoryBackground(
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
