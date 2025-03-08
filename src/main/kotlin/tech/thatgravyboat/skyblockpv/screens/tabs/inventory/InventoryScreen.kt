package tech.thatgravyboat.skyblockpv.screens.tabs.inventory

import com.mojang.authlib.GameProfile
import earth.terrarium.olympus.client.components.buttons.Button
import earth.terrarium.olympus.client.components.renderers.WidgetRenderers
import net.minecraft.client.gui.layouts.LinearLayout
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockpv.SkyBlockPv
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.screens.BasePvScreen
import tech.thatgravyboat.skyblockpv.screens.elements.ExtraConstants
import tech.thatgravyboat.skyblockpv.utils.LayoutBuild
import tech.thatgravyboat.skyblockpv.utils.LayoutBuilder
import tech.thatgravyboat.skyblockpv.utils.LayoutBuilder.Companion.setPos
import tech.thatgravyboat.skyblockpv.utils.Utils.center
import tech.thatgravyboat.skyblockpv.utils.Utils.centerHorizontally
import tech.thatgravyboat.skyblockpv.utils.Utils.centerVertically
import tech.thatgravyboat.skyblockpv.utils.displays.*

class InventoryScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BasePvScreen("INVENTORY", gameProfile, profile) {
    override fun create(bg: DisplayWidget) {
        profile ?: return
        val rowHeight = (uiHeight - 10) / 2
        LayoutBuild.vertical(5) {
            createMainInventoryRow(rowHeight)
            display(Displays.background(0xFFAAAAAAu, Displays.placeholder(uiWidth - 10, 5)).centerIn(uiWidth, -1))
            createSecondaryInventory(rowHeight)
        }.setPos(bg.x, bg.y).visitWidgets(this::addRenderableWidget)

        addCategories(bg)
    }

    private fun LayoutBuilder.createMainInventoryRow(height: Int) = horizontal {
        val inventory = profile?.inventory ?: return@horizontal
        val armor = inventory.armorItems?.inventory.orEmpty(4)
        val equipment = inventory.equipmentItems?.inventory.orEmpty(4)

        spacer(10, height)
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

    var page = 0

    private fun LayoutBuilder.createSecondaryInventory(height: Int) = horizontal {
        val inventory = profile?.inventory ?: return@horizontal

        val pageButtons = List(inventory.enderChestPages!!.size) {
            Button()
                .withSize(20, 20)
                .withTexture(ExtraConstants.BUTTON_DARK)
                .withCallback {
                    page = it
                    this@InventoryScreen.rebuildWidgets()
                }
        }

        val buttonContainer = LinearLayout.vertical().spacing(1)
        val maxRowWidth = pageButtons.size.coerceAtMost(5) * 20 + (pageButtons.size.coerceAtMost(5) - 1)

        pageButtons.chunked(5).forEach { chunk ->
            val element = LinearLayout.horizontal().spacing(1)
            chunk.forEach { element.addChild(it) }
            buttonContainer.addChild(element.centerHorizontally(maxRowWidth))
        }

        widget(buttonContainer.centerVertically(height))

        spacer(10, height)

        widget(createPagedInventory(inventory.enderChestPages!!.map { it.items.inventory }).center(-1, height))
    }


    private fun createPagedInventory(items: List<List<ItemStack>>): DisplayWidget {
        val itemDisplays = items[page].chunked(9).map { chunk ->
            chunk.map { item ->
                Displays.padding(2, Displays.item(item, showTooltip = true, showStackSize = true))
            }
        }
        return Displays.background(
            SkyBlockPv.id("inventory/inventory-9x${itemDisplays.size}"),
            Displays.padding(2, itemDisplays.asTable()),
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
                button.withCallback {
                    //currentCategory = category
                    page = 0
                    this.rebuildWidgets()
                }
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
