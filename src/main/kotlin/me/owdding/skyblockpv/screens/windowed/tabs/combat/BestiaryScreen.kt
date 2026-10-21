package me.owdding.skyblockpv.screens.windowed.tabs.combat

import com.mojang.authlib.GameProfile
import com.mojang.datafixers.util.Either
import earth.terrarium.olympus.client.components.base.BaseWidget
import earth.terrarium.olympus.client.components.buttons.Button
import me.owdding.lib.displays.Alignment
import me.owdding.lib.displays.Display
import me.owdding.lib.displays.DisplayWidget
import me.owdding.lib.displays.Displays
import me.owdding.lib.displays.asTable
import me.owdding.lib.displays.asWidget
import me.owdding.lib.displays.toColumn
import me.owdding.lib.displays.toRow
import me.owdding.lib.extensions.ItemUtils.createSkull
import me.owdding.lib.extensions.rightPad
import me.owdding.lib.extensions.round
import me.owdding.lib.extensions.withTooltip
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.data.repo.BestiaryCategoryEntry
import me.owdding.skyblockpv.data.repo.BestiaryCodecs
import me.owdding.skyblockpv.data.repo.BestiaryIcon
import me.owdding.skyblockpv.data.repo.BestiaryMobEntry
import me.owdding.skyblockpv.data.repo.ComplexBestiaryCategoryEntry
import me.owdding.skyblockpv.utils.CarouselPage
import me.owdding.skyblockpv.utils.LayoutUtils.asScrollable
import me.owdding.skyblockpv.utils.LayoutUtils.centerHorizontally
import me.owdding.skyblockpv.utils.Utils
import me.owdding.skyblockpv.utils.Utils.asTranslated
import me.owdding.skyblockpv.utils.Utils.fixBase64Padding
import me.owdding.skyblockpv.utils.components.CarouselWidget
import me.owdding.skyblockpv.utils.components.PvLayouts
import me.owdding.skyblockpv.utils.displays.ExtraDisplays
import me.owdding.skyblockpv.utils.theme.PvColors
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.utils.builders.TooltipBuilder
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.italic
import me.owdding.lib.displays.withTooltip as withDisplayTooltip

class BestiaryScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseCombatScreen(gameProfile, profile), CarouselPage {
    private var carousel: CarouselWidget? = null
    private val selectedSubcategories = mutableMapOf<String, Int>()

    override var carouselStart: Int = 0

    override fun getLayout(bg: DisplayWidget) = PvLayouts.vertical {
        val unknownBestiaryKills = profile.bestiaryData.map { it.mobId }
            .filter { mobId -> BestiaryCodecs.allMobs.none { it == mobId } }
            .distinct()

        val overview = getOverview()
        val pages = getPages()
        val inventories = pages.map { it.display }
        val icons = pages.map { it.icon }

        carousel = CarouselWidget(
            inventories,
            carousel?.index ?: carouselStart,
            246,
            minHeight = pages.maxOfOrNull { it.maxHeight } ?: 0,
            preview = { index, direction -> previewDisplay(pages, index, direction) },
            onStep = { index, direction -> stepSubcategory(pages, index, direction) },
        )

        val buttonContainer = carousel!!.getIcons(page = toTabState()) { icons.map { Displays.item(it, showTooltip = true) } }

        widget(
            PvLayouts.vertical(5) {
                widget(overview.asWidget().centerHorizontally(uiWidth))
                widget(buttonContainer.centerHorizontally(uiWidth))
                if (pages.any { it.tabs.isNotEmpty() }) {
                    widget(SubcategoryTabs(pages, 246) { carousel?.index ?: 0 }.centerHorizontally(uiWidth))
                }
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

    private fun getPages(): List<CategoryPage> = BestiaryCodecs.data.categories.map { (key, v) ->
        Either.unwrap(
            v.mapBoth(
                { CategoryPage(key, it.icon.getItem(it.name).withCategoryTooltip(it.name, it.mobs), it.getCategory()) }, // Simple
                { it.getComplexCategory(key) }, // Complex
            ),
        )
    }

    private fun BestiaryCategoryEntry.getCategory() = mobs.map { it.getItem() }.format(MOBS_PER_ROW)

    private fun ComplexBestiaryCategoryEntry.getComplexCategory(key: String): CategoryPage {
        val subs = subcategories.values.toList()
        val grids = subs.map { sub -> sub.mobs.map { it.getItem() }.format(MOBS_PER_ROW) }
        val selected = { (selectedSubcategories[key] ?: 0).coerceIn(0, maxOf(grids.lastIndex, 0)) }

        val tabs = subs.mapIndexed { index, sub ->
            val item = Displays.item(sub.icon.getItem(sub.name).withCategoryTooltip(sub.name, sub.mobs), showTooltip = true)
            CarouselWidget.carouselIconButton(item, { selected() == index }) {
                selectedSubcategories[key] = index
            }
        }

        val display = object : Display {
            override fun getWidth() = grids.getOrNull(selected())?.getWidth() ?: 0
            override fun getHeight() = grids.getOrNull(selected())?.getHeight() ?: 0

            override fun extract(graphics: GuiGraphicsExtractor) {
                grids.getOrNull(selected())?.extract(graphics)
            }
        }

        return CategoryPage(key, icon.getItem(name).withCategoryTooltip(name, subs.flatMap { it.mobs }), display, tabs, grids)
    }

    private fun stepTarget(pages: List<CategoryPage>, index: Int, direction: Int): Pair<Int, Int> {
        val page = pages.getOrNull(index) ?: return index to 0
        val next = (selectedSubcategories[page.key] ?: 0) + direction
        if (next in page.tabs.indices) return index to next

        val targetIndex = (index + direction + pages.size) % pages.size
        val target = pages[targetIndex]
        val sub = if (target.tabs.isEmpty() || direction > 0) 0 else target.tabs.lastIndex
        return targetIndex to sub
    }

    private fun previewDisplay(pages: List<CategoryPage>, index: Int, direction: Int): Display? {
        val (targetIndex, sub) = stepTarget(pages, index, direction)
        val page = pages.getOrNull(targetIndex) ?: return null
        return page.grids.getOrNull(sub) ?: page.display
    }

    private fun stepSubcategory(pages: List<CategoryPage>, index: Int, direction: Int): Boolean {
        val (targetIndex, sub) = stepTarget(pages, index, direction)
        val target = pages.getOrNull(targetIndex) ?: return false
        if (target.tabs.isNotEmpty()) {
            selectedSubcategories[target.key] = sub
        }
        return targetIndex == index
    }

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
                append("$currentLevel", PvColors.YELLOW)
                append("/", PvColors.GOLD)
                append("$maxLevel", PvColors.YELLOW)
            }

            add("Kills: ") {
                color = PvColors.GRAY
                append(kills.toFormattedString(), PvColors.YELLOW)

                val percentage = kills / requiredKills.toDouble() * 100
                if (percentage >= 100) {
                    append(" Maxed!", PvColors.RED)
                } else {
                    append("/", PvColors.GOLD)
                    append(requiredKills.toFormattedString(), PvColors.YELLOW)

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
                    append(kills.toFormattedString(), PvColors.YELLOW)
                    append("/", PvColors.GOLD)
                    append(tiers[currentLevel].toFormattedString(), PvColors.YELLOW)

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
            customStackText = Text.of(currentLevel.toString(), if (currentLevel == maxLevel) PvColors.GOLD else PvColors.WHITE),
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

        private const val MOBS_PER_ROW = 8
    }
}

private class CategoryPage(
    val key: String,
    val icon: ItemStack,
    val display: Display,
    val tabs: List<Button> = emptyList(),
    val grids: List<Display> = emptyList(),
) {
    val maxHeight: Int get() = maxOf(display.getHeight(), grids.maxOfOrNull { it.getHeight() } ?: 0)
}

private class SubcategoryTabs(
    private val pages: List<CategoryPage>,
    width: Int,
    private val currentIndex: () -> Int,
) : BaseWidget() {

    init {
        this.width = width
        this.height = 20
    }

    private fun placeTabs(): List<Button> {
        val tabs = pages.getOrNull(currentIndex())?.tabs ?: return emptyList()
        val startX = x + (width - (tabs.size * 21 - 1)) / 2
        tabs.forEachIndexed { index, button ->
            button.x = startX + index * 21
            button.y = y
        }
        return tabs
    }

    override fun extractWidgetRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partialTicks: Float) {
        placeTabs().forEach { it.extractRenderState(graphics, mouseX, mouseY, partialTicks) }
    }

    override fun onClick(event: MouseButtonEvent, doubleClick: Boolean) {
        placeTabs().forEach { it.mouseClicked(event, doubleClick) }
    }
}
