package tech.thatgravyboat.skyblockpv.screens.tabs.inventory

import com.mojang.authlib.GameProfile
import earth.terrarium.olympus.client.components.buttons.Button
import earth.terrarium.olympus.client.components.renderers.WidgetRenderers
import net.minecraft.client.gui.layouts.LinearLayout
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.screens.elements.ExtraConstants
import tech.thatgravyboat.skyblockpv.utils.LayoutBuild
import tech.thatgravyboat.skyblockpv.utils.Utils.centerHorizontally
import tech.thatgravyboat.skyblockpv.utils.components.CarouselWidget
import tech.thatgravyboat.skyblockpv.utils.displays.Display
import tech.thatgravyboat.skyblockpv.utils.displays.Displays

abstract class BasePagedInventoryScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseInventoryScreen(gameProfile, profile) {
    protected var page = 0

    abstract fun getInventories(): List<Display>
    abstract fun getIcons(): List<ItemStack>


    override fun createInventoryWidget() = LayoutBuild.vertical {
        val inventories = getInventories()
        val buttonContainer = LinearLayout.horizontal().spacing(1)
        val icons = getIcons()

        var carousel = CarouselWidget(
            inventories,
            page,
            246,
        )

        repeat(inventories.size) { index ->
            val icon = icons[index]
            icon.count = index + 1
            val itemDisplay = Displays.item(icon, showStackSize = true)

            val button = Button()
                .withSize(20, 20)
                .withTexture(ExtraConstants.BUTTON_DARK)
                .withRenderer(
                    WidgetRenderers.center(16, 16) { gr, ctx, _ -> itemDisplay.render(gr, ctx.x, ctx.y) },
                )
                .withCallback {
                    page = index
                    carousel.index = index
                }
            buttonContainer.addChild(button)
        }

        widget(buttonContainer.centerHorizontally(uiWidth))
        spacer(height = 10)
        widget(carousel.centerHorizontally(uiWidth))
    }
}
