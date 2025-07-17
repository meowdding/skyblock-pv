package me.owdding.skyblockpv.screens.tabs.combat

import com.mojang.authlib.GameProfile
import com.mojang.datafixers.util.Either
import me.owdding.lib.displays.Display
import me.owdding.lib.displays.DisplayWidget
import me.owdding.lib.displays.Displays
import me.owdding.lib.displays.asTable
import me.owdding.lib.extensions.ItemUtils.createSkull
import me.owdding.lib.extensions.rightPad
import me.owdding.lib.extensions.round
import me.owdding.lib.extensions.withTooltip
import me.owdding.skyblockpv.api.data.SkyBlockProfile
import me.owdding.skyblockpv.data.repo.*
import me.owdding.skyblockpv.utils.LayoutUtils.asScrollable
import me.owdding.skyblockpv.utils.LayoutUtils.centerHorizontally
import me.owdding.skyblockpv.utils.Utils
import me.owdding.skyblockpv.utils.Utils.append
import me.owdding.skyblockpv.utils.Utils.fixBase64Padding
import me.owdding.skyblockpv.utils.components.CarouselWidget
import me.owdding.skyblockpv.utils.components.PvLayouts
import me.owdding.skyblockpv.utils.displays.ExtraDisplays
import me.owdding.skyblockpv.utils.theme.PvColors
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.italic

class BestiaryScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseCombatScreen(gameProfile, profile) {
    private var carousel: CarouselWidget? = null
    private val MOBS_PER_ROW_SIMPLE = 5
    private val MOBS_PER_ROW_COMPLEX = 8

    override fun getLayout(bg: DisplayWidget) = PvLayouts.vertical {
        val categories = getCategories()
        val inventories = categories.values.toList()
        val icons = categories.keys.toList()

        carousel = CarouselWidget(
            inventories,
            carousel?.index ?: 0,
            246,
        )

        val buttonContainer = carousel!!.getIcons { icons.map { Displays.item(it, showTooltip = true) } }

        widget(
            PvLayouts.vertical(5) {
                widget(buttonContainer.centerHorizontally(uiWidth))
                widget(carousel!!.centerHorizontally(uiWidth))
            }.asScrollable(uiWidth, uiHeight),
        )
    }

    private fun getCategories(): Map<ItemStack, Display> = BestiaryCodecs.data.categories.map { (_, v) ->
        Either.unwrap(
            v.mapBoth(
                { it.icon.getItem(it.name) to it.getCategory() }, // Simple
                { it.icon.getItem(it.name) to it.getCategory() }, // Complex
            ),
        )
    }.toMap()

    private fun BestiaryCategoryEntry.getCategory() = mobs.map { it.getItem() }.format(MOBS_PER_ROW_SIMPLE)

    private fun ComplexBestiaryCategoryEntry.getCategory() = subcategories.flatMap { it.value.mobs.map { it.getItem() } }.format(MOBS_PER_ROW_COMPLEX)

    private fun List<Display>.format(mobsPerRow: Int) = toMutableList()
        .rightPad(mobsPerRow * 2, Displays.empty(16, 16))
        .map { Displays.padding(2, it) }
        .chunked(mobsPerRow)
        .let {
            ExtraDisplays.inventoryBackground(
                mobsPerRow, it.size,
                Displays.padding(2, it.asTable()),
            )
        }

    private fun BestiaryMobEntry.getItem(): Display {
        val kills = profile.bestiaryData.filter { mobs.contains(it.mobId) }.sumOf { it.kills }
        val fullBracket = BestiaryCodecs.data.brackets[bracket] ?: emptyList()
        val maxLevel = fullBracket.indexOf(cap) + 1
        val bracket = fullBracket.take(maxLevel)
        val requiredKills = bracket.lastOrNull() ?: 0
        val currentLevel = bracket.indexOfLast { kills >= it } + 1

        val item = if (kills == 0L) Items.GRAY_DYE.defaultInstance else icon.getItem()

        item.withTooltip {
            add(name)

            add("Level: ") {
                color = PvColors.GRAY
                append("$currentLevel") { color = PvColors.YELLOW }
                append("/") { color = PvColors.GOLD }
                append("$maxLevel") { color = PvColors.YELLOW }
            }

            add("Kills: ") {
                color = PvColors.GRAY
                append(kills.toFormattedString()) { color = PvColors.YELLOW }

                val percentage = kills / requiredKills.toDouble() * 100
                if (percentage >= 100) {
                    append(" Maxed!") {
                        color = PvColors.RED
                    }
                } else {
                    append("/") { color = PvColors.GOLD }
                    append(requiredKills.toFormattedString()) { color = PvColors.YELLOW }

                    append(" (")
                    append(percentage.round()) {
                        color = PvColors.GREEN
                        append("%")
                    }
                    append(")")
                }
            }

            if (currentLevel != maxLevel) {
                add("Next Level: ") {
                    color = PvColors.GRAY
                    append(kills.toFormattedString()) { color = PvColors.YELLOW }
                    append("/") { color = PvColors.GOLD }
                    append(bracket[currentLevel].toFormattedString()) { color = PvColors.YELLOW }

                    append(" (")
                    append((kills / bracket[currentLevel].toDouble() * 100).round()) {
                        color = PvColors.GREEN
                        append("%")
                    }
                    append(")")
                }
            }
        }
        return Displays.item(item, customStackText = currentLevel, showTooltip = true)
    }

    private fun BestiaryIcon.getItem(name: String = ""): ItemStack = Either.unwrap(
        this.mapBoth(
            { Utils.getMinecraftItem(it) },
            // NEU Repo can be weird, sometimes including that weird split or wrong length of base64
            { createSkull(it.second.split("\", \"").first().fixBase64Padding()) },
        ),
    ).apply {
        set(DataComponents.CUSTOM_NAME, Text.join(name) { italic = false; color = PvColors.WHITE })
    }
}

