package tech.thatgravyboat.skyblockpv.screens.tabs.rift

import com.mojang.authlib.GameProfile
import earth.terrarium.olympus.client.components.buttons.Button
import earth.terrarium.olympus.client.components.renderers.WidgetRenderers
import earth.terrarium.olympus.client.layouts.Layouts
import earth.terrarium.olympus.client.layouts.LinearViewLayout
import earth.terrarium.olympus.client.ui.UIConstants
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.utils.ExtraWidgetRenderers
import tech.thatgravyboat.skyblockpv.utils.LayoutBuild
import tech.thatgravyboat.skyblockpv.utils.LayoutUtils.centerHorizontally
import tech.thatgravyboat.skyblockpv.utils.components.CarouselWidget
import tech.thatgravyboat.skyblockpv.utils.components.PvWidgets
import tech.thatgravyboat.skyblockpv.utils.displays.Displays

class RiftEnderChestScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseRiftScreen(gameProfile, profile) {

    private var carousel: CarouselWidget? = null

    override fun getLayout() = LayoutBuild.vertical {
        val inventories = profile?.rift?.inventory?.enderChest?.map { PvWidgets.createInventory(it) } ?: return@vertical
        val icons = List(inventories.size) { Items.ENDER_CHEST.defaultInstance }

        carousel = CarouselWidget(
            inventories,
            carousel?.index ?: 0,
            246,
        )

        val buttons = List(inventories.size) { index ->
            val icon = icons[index]
            icon.count = index + 1
            val itemDisplay = Displays.item(icon, showStackSize = true)

            Button()
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
        }

        val buttonContainer = buttons.chunked(9)
            .map { it.fold(Layouts.row().withGap(1), LinearViewLayout::withChild) }
            .fold(Layouts.column().withGap(1), LinearViewLayout::withChild)

        widget(buttonContainer.centerHorizontally(uiWidth))
        spacer(height = 10)
        widget(carousel!!.centerHorizontally(uiWidth))
    }
}
