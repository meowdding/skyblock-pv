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
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

class MainForagingScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseForagingScreen(gameProfile, profile) {
    override val type: ForagingCategory = ForagingCategory.MAIN

    override fun getLayout(bg: DisplayWidget): Layout = PvLayouts.frame {
        val information = getInformation(profile)
        val personalBests = getPersonalBests(profile)

        horizontal(5, 0.5f) {
            widget(information)
            widget(personalBests)
        }
    }

    private fun getInformation(profile: SkyBlockProfile) = PvWidgets.label(
        "Information",
        PvLayouts.vertical(3) {
            val foraging = profile.foraging

            fun addGifts(name: String, level: Int, max: Int, amount: Int) {
                string("$name Gifts: ") {
                    append(level) {
                        color = if (level >= max) PvColors.GREEN else PvColors.RED
                    }
                    append("/")
                    append(max) { color = PvColors.GREEN }
                }
                string("Total $name Gifts: ") {
                    append(amount)
                }
            }
            addGifts(
                "Mangrove",
                foraging?.treeGifts?.mangroveTierClaimed ?: 0,
                StaticForagingData.treeGifts.mangrove.size,
                foraging?.treeGifts?.mangrove ?: 0,
            )
            addGifts(
                "Fig",
                foraging?.treeGifts?.figTierClaimed ?: 0,
                StaticForagingData.treeGifts.fig.size,
                foraging?.treeGifts?.fig ?: 0,
            )

            spacer(height = 1)
            string("Forest Whispers (Total/Spent)")
            string("") {
                append((profile.foragingCore?.forestsWhispers ?: 0).toFormattedString(), TextColor.DARK_AQUA)
                append("/")
                append((profile.foragingCore?.forestsSpentWhispers ?: 0).toFormattedString(), TextColor.DARK_AQUA)
            }
            profile.foragingCore
        },
        padding = 10,
        icon = SkyBlockPv.id("icon/item/clipboard"),
    )

    private fun getPersonalBests(profile: SkyBlockProfile) = PvLayouts.vertical(3, 0.5f) {

        fun hasPerk(name: String): Boolean = profile.essenceUpgrades[name] != null
        fun getPerkLevel(name: String): Int = profile.essenceUpgrades[name] ?: 0


        fun addType(name: String, id: String, maxFortune: Int, personalBest: Int, personalBestMax: Int, formula: String) = PvWidgets.label(
            name,
            PvLayouts.vertical(3) {
                val level = getPerkLevel("agatha_${id}_fortune")
                val hasPersonalBestUnlocked = hasPerk("agatha_${id}_personal_best")


                string("$name Personal Bests: ") {
                    append(if (hasPersonalBestUnlocked) "Yes" else "No") {
                        color = if (hasPersonalBestUnlocked) TextColor.GREEN else TextColor.RED
                    }
                }
                if (hasPersonalBestUnlocked) {
                    string("$name Best: ") {
                        append(personalBest.coerceAtMost(personalBestMax).toFormattedString()) {
                            color = if (personalBest >= personalBestMax) TextColor.GREEN else TextColor.RED
                        }
                        append("/")
                        append(personalBestMax.toFormattedString()) {
                            color = TextColor.GREEN
                        }
                    }
                }
                string("$name Fortune Level: ") {
                    append(level) {
                        color = if (level >= maxFortune) TextColor.GREEN else TextColor.RED
                    }
                    append("/")
                    append(maxFortune) {
                        color = TextColor.GREEN
                    }
                }
            },
            padding = 10,
        ).add()

        addType(
            "Fig",
            "fig",
            StaticForagingData.misc.figFortune,
            profile.foraging?.personalBests?.fig ?: 0,
            StaticForagingData.misc.figPersonalBest,
            StaticForagingData.misc.figRewardFormula,
        )
        addType(
            "Mangrove",
            "mangrove",
            StaticForagingData.misc.mangroveFortune,
            profile.foraging?.personalBests?.mangrove ?: 0,
            StaticForagingData.misc.mangrovePersonalBest,
            StaticForagingData.misc.mangroveRewardFormula,
        )
    }
}
