package me.owdding.skyblockpv.screens.windowed.tabs.combat

import com.mojang.authlib.GameProfile
import com.mojang.datafixers.util.Either
import me.owdding.lib.displays.Alignment
import me.owdding.lib.displays.Display
import me.owdding.lib.displays.DisplayWidget
import me.owdding.lib.displays.Displays
import me.owdding.lib.displays.asTable
import me.owdding.lib.displays.asWidget
import me.owdding.lib.displays.toColumn
import me.owdding.lib.displays.toRow
import me.owdding.lib.displays.withTooltip as withDisplayTooltip
import me.owdding.lib.extensions.ItemUtils.createSkull
import me.owdding.lib.extensions.rightPad
import me.owdding.lib.extensions.round
import me.owdding.lib.extensions.withTooltip
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.data.repo.*
import me.owdding.skyblockpv.utils.CarouselPage
import me.owdding.skyblockpv.utils.LayoutUtils.asScrollable
import me.owdding.skyblockpv.utils.LayoutUtils.centerHorizontally
import me.owdding.skyblockpv.utils.Utils
import me.owdding.skyblockpv.utils.Utils.append
import me.owdding.skyblockpv.utils.Utils.asTranslated
import me.owdding.skyblockpv.utils.Utils.fixBase64Padding
import me.owdding.skyblockpv.utils.components.CarouselWidget
import me.owdding.skyblockpv.utils.components.PvLayouts
import me.owdding.skyblockpv.utils.displays.ExtraDisplays
import me.owdding.skyblockpv.utils.theme.PvColors
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.utils.builders.TooltipBuilder
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.italic

class BestiaryScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseCombatScreen(gameProfile, profile), CarouselPage {
    private var carousel: CarouselWidget? = null
    private val MOBS_PER_ROW_SIMPLE = 5
    private val MOBS_PER_ROW_COMPLEX = 8

    override var carouselStart: Int = 0

    override fun getLayout(bg: DisplayWidget) = PvLayouts.vertical {
        val unknownBestiaryKills = profile.bestiaryData.map { it.mobId }
            .filter { mobId -> BestiaryCodecs.allMobs.none { it == mobId } }
            .distinct()

        val overview = getOverview()
        val categories = getCategories()
        val inventories = categories.values.toList()
        val icons = categories.keys.toList()

        carousel = CarouselWidget(
            inventories,
            carousel?.index ?: carouselStart,
            246,
        )

        val buttonContainer = carousel!!.getIcons(page = toTabState()) { icons.map { Displays.item(it, showTooltip = true) } }

        widget(
            PvLayouts.vertical(5) {
                widget(overview.asWidget().centerHorizontally(uiWidth))
                widget(buttonContainer.centerHorizontally(uiWidth))
                widget(carousel!!.centerHorizontally(uiWidth))
            }.asScrollable(uiWidth, uiHeight),
        )
        SkyBlockPv.ifDevMode {
            SkyBlockPv.info("Found unknown bestiary kills: $unknownBestiaryKills")
        }
    }

    private fun getAllMobs(): List<BestiaryMobEntry> = BestiaryCodecs.data.categories.values.flatMap {
        Either.unwrap(
            it.mapBoth(
                { it.mobs },
                { it.subcategories.values.flatMap { it.mobs } },
            ),
        )
    }

    private fun getOverview(): Display {
        val allMobs = getAllMobs()
        val tiers = allMobs.map { it.getTiers() }
        val unlocked = tiers.sumOf { it.first }
        val possible = tiers.sumOf { it.second }
        val maxed = tiers.count { it.first >= it.second && it.second > 0 }

        val major = unlocked / 10
        val minor = unlocked % 10
        val level = "$major.$minor"
        val isMaxed = unlocked >= possible && possible > 0
        val progress = if (isMaxed) 1.0f else (minor.toFloat() / 10f)
        val percent = if (possible == 0) 100.0 else (unlocked.toDouble() / possible.toDouble() * 100.0).round()

        val tooltip = TooltipBuilder().apply {
            add("screens.bestiary.level".asTranslated(level))
            add("screens.bestiary.tiers".asTranslated(unlocked, possible))
            add("screens.bestiary.progress".asTranslated(percent))
            add("screens.bestiary.mobs_maxed".asTranslated(maxed, allMobs.size))
            if (!isMaxed) {
                add("screens.bestiary.next_milestone".asTranslated(minor))
            }
        }.build()

        val bar = listOf(
            ExtraDisplays.progress(progress, isMaxed, 100),
            ExtraDisplays.text("screens.bestiary.milestone_progress".asTranslated(minor)),
        ).toRow(4, Alignment.CENTER)

        val content = listOf(
            ExtraDisplays.text("screens.bestiary.level".asTranslated(level)),
            bar,
        ).toColumn(2, Alignment.CENTER)

        return listOf(
            Displays.item(Items.WRITABLE_BOOK.defaultInstance),
            content,
        ).toRow(6, Alignment.CENTER).withDisplayTooltip(tooltip)
    }

    private fun ItemStack.withCategoryTooltip(name: String, mobs: List<BestiaryMobEntry>): ItemStack {
        val tiers = mobs.map { it.getTiers() }
        val current = tiers.sumOf { it.first }
        val max = tiers.sumOf { it.second }
        val maxed = tiers.count { it.first >= it.second && it.second > 0 }

        val percent = if (max == 0) 100.0 else (current.toDouble() / max.toDouble() * 100.0).round()

        return withTooltip {
            add(name)
            add("screens.bestiary.tiers".asTranslated(current, max))
            add("screens.bestiary.progress".asTranslated(percent))
            add("screens.bestiary.mobs_maxed".asTranslated(maxed, mobs.size))
        }
    }

    private fun getCategories(): Map<ItemStack, Display> = BestiaryCodecs.data.categories.map { (_, v) ->
        Either.unwrap(
            v.mapBoth(
                { it.icon.getItem(it.name).withCategoryTooltip(it.name, it.mobs) to it.getCategory() }, // Simple
                { it.icon.getItem(it.name).withCategoryTooltip(it.name, it.subcategories.values.flatMap { it.mobs }) to it.getCategory() }, // Complex
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

    private fun BestiaryMobEntry.resolveBracket(): Pair<Long, List<Int>> {
        val kills = profile.bestiaryData.filter { mobs.contains(it.mobId) }.sumOf { it.kills }

        val fullBracket = if (bracketType != null) {
            val upperType = bracketType.uppercase()
            val bracketSets = BestiaryCodecs.data.bracketSets ?: emptyMap()

            val typeBrackets = bracketSets[upperType] ?: bracketSets[BRACKET_TYPE_ALIASES[upperType]]

            typeBrackets?.get(bracket) ?: emptyList()
        } else {
            BestiaryCodecs.data.brackets[bracket] ?: emptyList()
        }

        val tiers = if (fullBracket.isEmpty()) emptyList() else fullBracket.takeWhile { it < cap } + cap
        return kills to tiers
    }

    private fun BestiaryMobEntry.getTiers(): Pair<Int, Int> {
        val (kills, tiers) = resolveBracket()
        return tiers.count { kills >= it } to tiers.size
    }

    private fun BestiaryMobEntry.getItem(): Display {
        val (kills, tiers) = resolveBracket()
        val maxLevel = tiers.size
        val requiredKills = tiers.lastOrNull() ?: 0
        val currentLevel = tiers.indexOfLast { kills >= it } + 1

        val item = if (kills == 0L) Items.DYE.gray().defaultInstance else icon.getItem()

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
                    append(tiers[currentLevel].toFormattedString()) { color = PvColors.YELLOW }

                    append(" (")
                    append((kills / tiers[currentLevel].toDouble() * 100).round()) {
                        color = PvColors.GREEN
                        append("%")
                    }
                    append(")")
                }
            }
        }
        return Displays.item(
            item,
            customStackText = Text.of(currentLevel.toString()) {
                color = if (currentLevel == maxLevel) PvColors.GOLD else PvColors.WHITE
            },
            showTooltip = true,
        )
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

    companion object {
        // The bracketSet is CRITTERS while its using CRITTER inside each entry
        private val BRACKET_TYPE_ALIASES = mapOf(
            "CRITTER" to "CRITTERS",
        )
    }
}

