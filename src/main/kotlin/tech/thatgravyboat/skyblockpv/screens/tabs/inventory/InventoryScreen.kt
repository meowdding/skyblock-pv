package tech.thatgravyboat.skyblockpv.screens.tabs.inventory

import com.mojang.authlib.GameProfile
import earth.terrarium.olympus.client.components.buttons.Button
import earth.terrarium.olympus.client.components.renderers.WidgetRenderers
import net.minecraft.client.gui.layouts.FrameLayout
import net.minecraft.client.gui.layouts.LinearLayout
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockpv.SkyBlockPv
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.screens.BasePvScreen
import tech.thatgravyboat.skyblockpv.screens.elements.ExtraConstants
import tech.thatgravyboat.skyblockpv.utils.LayoutBuild
import tech.thatgravyboat.skyblockpv.utils.Utils.center
import tech.thatgravyboat.skyblockpv.utils.displays.*

class InventoryScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BasePvScreen("INVENTORY", gameProfile, profile) {
    override fun create(bg: DisplayWidget) {
        profile ?: return
        val inv = createMainInventoryRow()
        FrameLayout.centerInRectangle(inv, bg.x, bg.y, uiWidth, uiHeight)
        inv.visitWidgets(this::addRenderableWidget)

        addCategories(bg)
    }

    private fun createMainInventoryRow() = LayoutBuild.horizontal {
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
        widget(createInventory(inventory.inventoryItems?.inventory.orEmpty(36)).center(-1, height))
    }

    private fun createInventory(items: List<ItemStack>): DisplayWidget {
        val itemDisplays = items.chunked(9).map { chunk ->
            chunk.map { item ->
                Displays.padding(2, Displays.item(item, showTooltip = true, showStackSize = true))
            }
        }
        val sortedItemDisplays = itemDisplays.drop(1) + itemDisplays.take(1)
        return Displays.background(
            SkyBlockPv.id("inventory/inventory-9x${sortedItemDisplays.size}"),
            Displays.padding(2, sortedItemDisplays.asTable()),
        ).asWidget()
    }

    private fun addCategories(bg: DisplayWidget) {
        val categories = InventoryCategory.entries
        val buttonRow = LinearLayout.horizontal().spacing(2)
        categories.forEach { category ->
            val button = Button()
            button.setSize(20, 22)
            if (category.isSelected()) {
                button.withTexture(ExtraConstants.TAB_TOP_SELECTED)
            } else {
                button.withCallback { McClient.tell { McClient.setScreen(category.create(gameProfile, profile)) } }
                button.withTexture(ExtraConstants.TAB_TOP)
            }
            button.withRenderer(
                WidgetRenderers.padded(
                    4, 0, 0, 0,
                    WidgetRenderers.center(16, 16) { gr, ctx, _ -> gr.renderItem(category.icon, ctx.x, ctx.y) },
                ),
            )
            buttonRow.addChild(button)
        }
        buttonRow.arrangeElements()
        buttonRow.setPosition(bg.x + 20, bg.y - buttonRow.height)
        buttonRow.visitWidgets(this::addRenderableWidget)
    }

    private fun List<ItemStack>?.orEmpty(size: Int) = this ?: List(size) { ItemStack.EMPTY }
}
