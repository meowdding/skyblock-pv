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
import tech.thatgravyboat.skyblockpv.utils.LayoutBuilder.Companion.setPos
import tech.thatgravyboat.skyblockpv.utils.displays.DisplayWidget
import tech.thatgravyboat.skyblockpv.utils.displays.Displays
import tech.thatgravyboat.skyblockpv.utils.displays.asTable
import tech.thatgravyboat.skyblockpv.utils.displays.asWidget

class InventoryScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BasePvScreen("INVENTORY", gameProfile, profile) {
    override fun create(bg: DisplayWidget) {
        LayoutBuild.vertical {
            widget(createInventory(profile!!.inventory!!.inventoryItems!!.inventory))
        }.setPos(bg.x, bg.y).visitWidgets(this::addRenderableWidget)

        addCategories(bg)
    }

    private fun createInventory(items: List<ItemStack>): DisplayWidget {
        val itemDisplays = items.chunked(9).map { chunk ->
            chunk.map { item ->
                Displays.padding(2, Displays.item(item, showTooltip = true, showStackSize = true))
            }
        }.reversed()
        return Displays.background(
            SkyBlockPv.id("inventory/inventory-${itemDisplays.size}"),
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
}
