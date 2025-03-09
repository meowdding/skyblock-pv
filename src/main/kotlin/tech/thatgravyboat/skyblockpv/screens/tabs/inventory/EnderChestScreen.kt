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
import tech.thatgravyboat.skyblockpv.utils.Utils.centerHorizontally
import tech.thatgravyboat.skyblockpv.utils.components.CarouselWidget
import tech.thatgravyboat.skyblockpv.utils.displays.Display
import tech.thatgravyboat.skyblockpv.utils.displays.DisplayWidget
import tech.thatgravyboat.skyblockpv.utils.displays.Displays
import tech.thatgravyboat.skyblockpv.utils.displays.asTable

class EnderChestScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BasePvScreen("INVENTORY", gameProfile, profile) {
    override fun create(bg: DisplayWidget) {
        profile ?: return
        val inv = createSecondaryInventory()
        FrameLayout.centerInRectangle(inv, bg.x, bg.y, uiWidth, uiHeight)
        inv.visitWidgets(this::addRenderableWidget)

        addCategories(bg)
    }

    var page = 0

    private fun createSecondaryInventory() = LayoutBuild.vertical {
        val inventory = profile?.inventory ?: return@vertical
        val buttonContainer = LinearLayout.horizontal().spacing(1)

        repeat(inventory.enderChestPages!!.size) { index ->
            val button = Button()
                .withSize(20, 20)
                .withTexture(ExtraConstants.BUTTON_DARK)
                .withCallback {
                    page = index
                    this@EnderChestScreen.rebuildWidgets()
                }
            buttonContainer.addChild(button)
        }

        widget(buttonContainer.centerHorizontally(uiWidth))

        spacer(height = 10)

        widget(
            CarouselWidget(
                inventory.enderChestPages.map { createPagedInventory(it.items.inventory) },
                page,
                246,
            ).centerHorizontally(uiWidth),
        )

    }


    private fun createPagedInventory(items: List<ItemStack>): Display {
        val itemDisplays = items.chunked(9).map { chunk ->
            chunk.map { item ->
                Displays.padding(2, Displays.item(item, showTooltip = true, showStackSize = true))
            }
        }
        return Displays.background(
            SkyBlockPv.id("inventory/inventory-9x${itemDisplays.size}"),
            Displays.padding(2, itemDisplays.asTable()),
        )
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
