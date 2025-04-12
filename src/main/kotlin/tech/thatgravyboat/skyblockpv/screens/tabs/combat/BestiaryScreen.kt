package tech.thatgravyboat.skyblockpv.screens.tabs.combat

import com.mojang.authlib.GameProfile
import com.mojang.datafixers.util.Either
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.italic
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.data.repo.*
import tech.thatgravyboat.skyblockpv.utils.LayoutBuild
import tech.thatgravyboat.skyblockpv.utils.LayoutUtils.asScrollable
import tech.thatgravyboat.skyblockpv.utils.LayoutUtils.centerHorizontally
import tech.thatgravyboat.skyblockpv.utils.Utils
import tech.thatgravyboat.skyblockpv.utils.Utils.append
import tech.thatgravyboat.skyblockpv.utils.Utils.fixBase64Padding
import tech.thatgravyboat.skyblockpv.utils.Utils.rightPad
import tech.thatgravyboat.skyblockpv.utils.Utils.round
import tech.thatgravyboat.skyblockpv.utils.components.CarouselWidget
import tech.thatgravyboat.skyblockpv.utils.createSkull
import tech.thatgravyboat.skyblockpv.utils.displays.Display
import tech.thatgravyboat.skyblockpv.utils.displays.DisplayWidget
import tech.thatgravyboat.skyblockpv.utils.displays.Displays
import tech.thatgravyboat.skyblockpv.utils.displays.asTable
import tech.thatgravyboat.skyblockpv.utils.withTooltip

class BestiaryScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseCombatScreen(gameProfile, profile) {
    private var carousel: CarouselWidget? = null
    private val MOBS_PER_ROW_SIMPLE = 5
    private val MOBS_PER_ROW_COMPLEX = 8

    override fun getLayout(bg: DisplayWidget) = LayoutBuild.vertical {
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
            LayoutBuild.vertical(5) {
                widget(buttonContainer.centerHorizontally(uiWidth))
                widget(carousel!!.centerHorizontally(uiWidth))
            }.asScrollable(uiWidth, uiHeight),
        )
    }

    private fun getCategories(): Map<ItemStack, Display> = BestiaryCodecs.data?.categories?.map { (_, v) ->
        Either.unwrap(
            v.mapBoth(
                { it.icon.getItem(it.name) to it.getCategory() }, // Simple
                { it.icon.getItem(it.name) to it.getCategory() }, // Complex
            ),
        )
    }?.toMap() ?: emptyMap()

    private fun BestiaryCategoryEntry.getCategory() = mobs.map { it.getItem() }.format(MOBS_PER_ROW_SIMPLE)

    private fun ComplexBestiaryCategoryEntry.getCategory() = subcategories.flatMap { it.value.mobs.map { it.getItem() } }.format(MOBS_PER_ROW_COMPLEX)

    private fun List<Display>.format(mobsPerRow: Int) = toMutableList()
        .rightPad(mobsPerRow * 2, Displays.empty(16, 16))
        .map { Displays.padding(2, it) }
        .chunked(mobsPerRow)
        .let {
            Displays.inventoryBackground(
                mobsPerRow, it.size,
                Displays.padding(2, it.asTable()),
            )
        }

    private fun BestiaryMobEntry.getItem(): Display {
        val kills = profile?.bestiaryData?.filter { mobs.contains(it.mobId) }?.sumOf { it.kills } ?: 0
        val fullBracket = BestiaryCodecs.data?.brackets?.get(bracket) ?: emptyList()
        val maxLevel = fullBracket.indexOf(cap) + 1
        val bracket = fullBracket.take(maxLevel)
        val requiredKills = bracket.lastOrNull() ?: 0
        val currentLevel = bracket.indexOfLast { kills >= it } + 1

        val item = if (kills == 0L) Items.GRAY_DYE.defaultInstance
        else icon.getItem()

        item.withTooltip {
            add(name)

            add("Kills: ") {
                color = TextColor.GRAY
                append(kills.toFormattedString()) { color = TextColor.YELLOW }
                append("/") { color = TextColor.GOLD }
                append(requiredKills.toFormattedString()) { color = TextColor.YELLOW }
                val percentage = kills / requiredKills.toDouble() * 100

                if (percentage >= 100) {
                    append(" Maxed!") {
                        color = TextColor.RED
                    }
                } else {
                    append(" (")
                    append(percentage.round()) {
                        color = TextColor.GREEN
                        append("%")
                    }
                    append(")")
                }
            }

            add("Level: ") {
                color = TextColor.GRAY
                append("$currentLevel") { color = TextColor.YELLOW }
                append("/") { color = TextColor.GOLD }
                append("$maxLevel") { color = TextColor.YELLOW }
            }

            if (currentLevel != maxLevel) {
                add("Next Level: ") {
                    color = TextColor.GRAY
                    append(kills.toFormattedString()) { color = TextColor.YELLOW }
                    append("/") { color = TextColor.GOLD }
                    append(bracket[currentLevel].toFormattedString()) { color = TextColor.YELLOW }

                    append(" (")
                    append((kills / bracket[currentLevel].toDouble() * 100).round()) {
                        color = TextColor.GREEN
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
        set(DataComponents.CUSTOM_NAME, Text.join(name) { italic = false; color = TextColor.WHITE })
    }
}

