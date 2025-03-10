package tech.thatgravyboat.skyblockpv.screens.tabs.inventory

import com.mojang.authlib.GameProfile
import earth.terrarium.olympus.client.components.buttons.Button
import earth.terrarium.olympus.client.components.renderers.WidgetRenderers
import earth.terrarium.olympus.client.ui.UIConstants
import net.minecraft.client.gui.layouts.LinearLayout
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.utils.ExtraWidgetRenderers
import tech.thatgravyboat.skyblockpv.utils.LayoutBuild
import tech.thatgravyboat.skyblockpv.utils.Utils.centerHorizontally
import tech.thatgravyboat.skyblockpv.utils.components.CarouselWidget
import tech.thatgravyboat.skyblockpv.utils.displays.Display
import tech.thatgravyboat.skyblockpv.utils.displays.Displays

abstract class BasePagedInventoryScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseInventoryScreen(gameProfile, profile) {

    protected var carousel: CarouselWidget? = null

    abstract fun getInventories(): List<Display>
    abstract fun getIcons(): List<ItemStack>


    override fun createInventoryWidget() = LayoutBuild.vertical {
        val inventories = getInventories()
        val buttonContainer = LinearLayout.horizontal().spacing(1)
        val icons = getIcons()

        carousel = CarouselWidget(
            inventories,
            carousel?.index ?: 0,
            246,
        )

        repeat(inventories.size) { index ->
            val icon = icons[index]
            icon.count = index + 1
            val itemDisplay = Displays.item(icon, showStackSize = true)

            val button = Button()
                .withSize(20, 20)
                .withRenderer(
                    WidgetRenderers.layered(
                        ExtraWidgetRenderers.conditional(
                            WidgetRenderers.sprite(UIConstants.PRIMARY_BUTTON),
                            WidgetRenderers.sprite(UIConstants.DARK_BUTTON),
                        ) { index == carousel?.index },
                        WidgetRenderers.center(16, 20, ExtraWidgetRenderers.display(itemDisplay)),
                    ),
                )
                .withCallback {
                    carousel?.index = index
                }
            buttonContainer.addChild(button)
        }

        widget(buttonContainer.centerHorizontally(uiWidth))
        spacer(height = 10)
        widget(carousel!!.centerHorizontally(uiWidth))
    }
}
