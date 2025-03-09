package tech.thatgravyboat.skyblockpv.screens.tabs.inventory

import com.mojang.authlib.GameProfile
import earth.terrarium.olympus.client.components.buttons.Button
import net.minecraft.client.gui.layouts.LinearLayout
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.screens.elements.ExtraConstants
import tech.thatgravyboat.skyblockpv.utils.LayoutBuild
import tech.thatgravyboat.skyblockpv.utils.Utils.centerHorizontally
import tech.thatgravyboat.skyblockpv.utils.components.CarouselWidget

class EnderChestScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseInventoryScreen(gameProfile, profile) {
    var page = 0

    override fun createInventoryWidget() = LayoutBuild.vertical {
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
                inventory.enderChestPages.map { createInventory(it.items.inventory) },
                page,
                246,
            ).centerHorizontally(uiWidth),
        )
    }
}
