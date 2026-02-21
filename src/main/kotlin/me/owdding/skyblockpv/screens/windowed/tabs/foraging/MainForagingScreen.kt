package me.owdding.skyblockpv.screens.windowed.tabs.foraging

import com.mojang.authlib.GameProfile
import me.owdding.lib.displays.DisplayWidget
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.data.repo.StaticForagingData
import me.owdding.skyblockpv.utils.components.PvLayouts
import me.owdding.skyblockpv.utils.components.PvWidgets
import me.owdding.skyblockpv.utils.theme.PvColors
import net.minecraft.client.gui.layouts.Layout
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

class MainForagingScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseForagingScreen(gameProfile, profile) {
    override val type: ForagingCategory = ForagingCategory.MAIN

    override fun getLayout(bg: DisplayWidget): Layout = PvLayouts.frame {
        val information = getInformation(profile)

        widget(information)
    }

    private fun getInformation(profile: SkyBlockProfile) = PvWidgets.label(
        "Information",
        PvLayouts.vertical(3) {

            val foraging = profile.foraging
            val core = profile.foragingCore

            fun addGifts(name: String, level: Int, max: Int) {
                textDisplay("$name Gifts: ") {
                    append(level) {
                        color = if (level >= max) PvColors.GREEN else PvColors.RED
                    }
                    append("/")
                    append(max) { color = PvColors.DARK_GREEN }
                }
            }
            addGifts("Mangrove", foraging?.treeGifts?.mangroveTierClaimed ?: 0, StaticForagingData.treeGifts.mangrove.size)
            addGifts("Fig", foraging?.treeGifts?.figTierClaimed ?: 0, StaticForagingData.treeGifts.fig.size)
        },
        padding = 10,
        icon = SkyBlockPv.id("icon/item/clipboard"),
    )
}
