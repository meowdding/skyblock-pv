package me.owdding.skyblockpv.screens.windowed.tabs.rift

import com.mojang.authlib.GameProfile
import me.owdding.lib.displays.DisplayWidget
import me.owdding.lib.displays.Displays
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.screens.PvTab
import me.owdding.skyblockpv.utils.CarouselPage
import me.owdding.skyblockpv.utils.LayoutUtils.centerHorizontally
import me.owdding.skyblockpv.utils.Utils
import me.owdding.skyblockpv.utils.components.CarouselWidget
import me.owdding.skyblockpv.utils.components.PvLayouts
import me.owdding.skyblockpv.utils.components.PvWidgets
import net.minecraft.world.item.Items

class RiftEnderChestScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseRiftScreen(gameProfile, profile), CarouselPage {

    override var carouselStart: Int = 0
    private var carousel: CarouselWidget? = null

    override fun getLayout(bg: DisplayWidget) = PvLayouts.vertical {
        val inventories = profile.rift?.inventory?.enderChest?.map { PvWidgets.createInventory(it) } ?: run {
            Utils.openTab(PvTab.COLLECTION, gameProfile, profile)
            return@vertical
        }
        val icons = List(inventories.size) { Items.ENDER_CHEST.defaultInstance }

        carousel = CarouselWidget(
            inventories,
            carousel?.index ?: carouselStart,
            246,
        )

        val buttonContainer = carousel!!.getIcons(page = toTabState()) {
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
