package me.owdding.skyblockpv.screens.tabs.rift

import com.mojang.authlib.GameProfile
import me.owdding.lib.builder.LayoutBuild
import me.owdding.lib.displays.DisplayWidget
import me.owdding.lib.displays.Displays
import me.owdding.skyblockpv.api.data.SkyBlockProfile
import me.owdding.skyblockpv.utils.LayoutUtils.centerHorizontally
import me.owdding.skyblockpv.utils.components.CarouselWidget
import me.owdding.skyblockpv.utils.components.PvWidgets
import net.minecraft.world.item.Items

class RiftEnderChestScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseRiftScreen(gameProfile, profile) {

    private var carousel: CarouselWidget? = null

    override fun getLayout(bg: DisplayWidget) = LayoutBuild.vertical {
        val inventories = profile?.rift?.inventory?.enderChest?.map { PvWidgets.createInventory(it) } ?: return@vertical
        val icons = List(inventories.size) { Items.ENDER_CHEST.defaultInstance }

        carousel = CarouselWidget(
            inventories,
            carousel?.index ?: 0,
            246,
        )

        val buttonContainer = carousel!!.getIcons {
            List(inventories.size) { index ->
                val icon = icons[index]
                icon.count = index + 1
                Displays.item(icon, showStackSize = true)
            }
        }

        widget(buttonContainer.centerHorizontally(uiWidth))
        spacer(height = 10)
        widget(carousel!!.centerHorizontally(uiWidth))
    }
}
