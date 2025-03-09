package tech.thatgravyboat.skyblockpv.screens.tabs.inventory

import com.mojang.authlib.GameProfile
import earth.terrarium.olympus.client.components.buttons.Button
import earth.terrarium.olympus.client.components.renderers.WidgetRenderers
import net.minecraft.client.gui.layouts.LinearLayout
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.screens.elements.ExtraConstants
import tech.thatgravyboat.skyblockpv.utils.LayoutBuild
import tech.thatgravyboat.skyblockpv.utils.Utils.centerHorizontally
import tech.thatgravyboat.skyblockpv.utils.components.CarouselWidget
import tech.thatgravyboat.skyblockpv.utils.displays.Displays

class EnderChestScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseInventoryScreen(gameProfile, profile) {
    var page = 0

    override fun createInventoryWidget() = LayoutBuild.vertical {
        val inventory = profile?.inventory ?: return@vertical
        val buttonContainer = LinearLayout.horizontal().spacing(1)

        repeat(inventory.enderChestPages!!.size) { index ->
            val enderChestItem = Items.ENDER_CHEST.defaultInstance
            enderChestItem.count = index + 1
            val itemDisplay = Displays.item(enderChestItem, showStackSize = true)

            val button = Button()
                .withSize(20, 20)
                .withTexture(ExtraConstants.BUTTON_DARK)
                .withRenderer(
                    WidgetRenderers.center(16, 16) { gr, ctx, _ -> itemDisplay.render(gr, ctx.x, ctx.y) },
                )
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
                inventory.enderChestPages.map { createInventory(it.items.inventory) },
                page,
                246,
            ).centerHorizontally(uiWidth),
        )
    }
}
