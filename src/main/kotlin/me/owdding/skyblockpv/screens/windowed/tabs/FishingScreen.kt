package me.owdding.skyblockpv.screens.windowed.tabs

import com.mojang.authlib.GameProfile
import earth.terrarium.olympus.client.components.base.ListWidget
import earth.terrarium.olympus.client.components.renderers.WidgetRenderers
import earth.terrarium.olympus.client.utils.Orientation
import me.owdding.lib.builder.LayoutBuilder
import me.owdding.lib.displays.Display
import me.owdding.lib.displays.DisplayWidget
import me.owdding.lib.displays.Displays
import me.owdding.lib.displays.asTable
import me.owdding.lib.displays.asWidget
import me.owdding.lib.displays.centerIn
import me.owdding.lib.displays.toColumn
import me.owdding.lib.displays.toRow
import me.owdding.lib.displays.withTooltip
import me.owdding.lib.extensions.transpose
import me.owdding.lib.layouts.setPos
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.api.predicates.ItemPredicateHelper
import me.owdding.skyblockpv.api.predicates.ItemPredicates
import me.owdding.skyblockpv.data.api.skills.DolphinBracket
import me.owdding.skyblockpv.data.api.skills.FishingGear
import me.owdding.skyblockpv.data.api.skills.TrophyFish
import me.owdding.skyblockpv.data.api.skills.TrophyFishType
import me.owdding.skyblockpv.data.api.skills.TrophyFrog
import me.owdding.skyblockpv.data.api.skills.TrophyFrogType
import me.owdding.skyblockpv.data.repo.EssenceData.addFishingPerk
import me.owdding.skyblockpv.screens.PvTab
import me.owdding.skyblockpv.screens.windowed.BaseWindowedPvScreen
import me.owdding.skyblockpv.screens.windowed.elements.ExtraConstants
import me.owdding.skyblockpv.utils.LayoutUtils.asScrollable
import me.owdding.skyblockpv.utils.Utils.text
import me.owdding.skyblockpv.utils.Utils.whiteText
import me.owdding.skyblockpv.utils.components.PvLayouts
import me.owdding.skyblockpv.utils.components.PvWidgets
import me.owdding.skyblockpv.utils.displays.ExtraDisplays
import me.owdding.skyblockpv.utils.theme.PvColors
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.client.gui.layouts.LayoutElement
import net.minecraft.client.gui.layouts.LayoutSettings
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.api.datatype.DataType
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.defaults.trophy.TrophyRank
import tech.thatgravyboat.skyblockapi.api.datatype.defaults.trophy.TrophyTier
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedName
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.text.CommonText
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.strikethrough
import java.math.RoundingMode
import java.text.DecimalFormat

class FishingScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) :
    BaseWindowedPvScreen("Fishing", gameProfile, profile) {

    private val numberFormatInstance = DecimalFormat.getCompactNumberInstance().apply {
        this.roundingMode = RoundingMode.FLOOR
    }

    override val tab: PvTab = PvTab.FISHING

    private var activeTrophyTab = TrophyType.FISH
    private var listWidgetInstance: ListWidget? = null
    private var savedScrollState: Int = 0

    override fun create(bg: DisplayWidget) {
        val infoWidget = getInfoWidget(profile)
        val statWidget = getStatWidget(profile)
        val gearWidget = getGearWidget(profile)
        var trophyWidth = 0

        val trophyWidget by lazy {
            val innerWidth = trophyWidth - 10

            PvWidgets.label(
                "Trophies",
                PvLayouts.vertical(alignment = 0.5f) {
                    val useSmallTable = (trophyWidth < 480)

                    horizontal {
                        val buttonWidth = (innerWidth / 2 - 4).coerceAtMost(120)

                        spacer(width = 4)
                        TrophyType.entries.forEachIndexed { index, type ->
                            button {
                                withSize(buttonWidth, 16)
                                withTexture(ExtraConstants.BUTTON_DARK)
                                withRenderer(WidgetRenderers.text(Text.of("Trophy $type", PvColors.WHITE)))
                                withCallback {
                                    if (activeTrophyTab != type) {
                                        activeTrophyTab = type
                                        savedScrollState = listWidgetInstance?.scroll ?: 0
                                        this@FishingScreen.rebuildWidgets()
                                    }
                                }
                            }
                            if (index != TrophyType.entries.size) spacer(width = 4)
                        }
                    }

                    spacer(height = 4)

                    when (activeTrophyTab) {
                        FISH -> {
                            if (useSmallTable) {
                                widget(getSmallTrophyFishTable(profile, innerWidth))
                            } else {
                                widget(getTrophyFishTable(profile, innerWidth))
                            }
                        }

                        FROG -> widget(getSmallTrophyFrogTable(profile, innerWidth))
                    }

                    spacer(height = 2)
                },
                width = trophyWidth,
            )
        }

        fun LayoutBuilder.addBottomRow(first: LayoutElement, second: LayoutElement) {
            PvLayouts.vertical {
                spacer(height = 5)
                horizontal {
                    widget(first)
                    widget(second, LayoutSettings::alignVerticallyBottom)
                }
                spacer(height = 10)
            }.let {
                widget(it) {
                    alignVerticallyBottom()
                    alignHorizontallyLeft()
                }
            }
        }

        fun Layout.applyLayout() {
            this.setPos(bg.x, bg.y).visitWidgets(this@FishingScreen::addRenderableWidget)
        }

        if (infoWidget.width + statWidget.width + gearWidget.width < bg.width && gearWidget.height + 165 /* Height of trophy table */ < bg.height) {
            trophyWidth = bg.width
            PvLayouts.frame {
                spacer(bg.width, bg.height)
                PvLayouts.vertical {
                    spacer(height = 5)
                    horizontal {
                        widget(infoWidget)
                        widget(statWidget)
                        widget(gearWidget)
                    }
                }.let {
                    widget(it) {
                        alignVerticallyTop()
                        alignHorizontallyLeft()
                    }
                }
                PvLayouts.vertical {
                    widget(trophyWidget)
                    spacer(height = 5)
                }.let {
                    widget(it) {
                        alignVerticallyBottom()
                        alignHorizontallyLeft()
                    }
                }
            }.applyLayout()
        } else if (
            infoWidget.width + statWidget.width < bg.width &&
            gearWidget.height + 10 + infoWidget.height < bg.height
        ) {
            trophyWidth = bg.width - gearWidget.width
            PvLayouts.frame {
                spacer(bg.width, bg.height)
                PvLayouts.vertical {
                    spacer(height = 5)
                    horizontal {
                        widget(infoWidget)
                        widget(statWidget)
                    }
                }.let {
                    widget(it) {
                        alignVerticallyTop()
                        alignHorizontallyLeft()
                    }
                }
                addBottomRow(gearWidget, trophyWidget)
            }.applyLayout()
        } else if (
            gearWidget.width + statWidget.width < bg.width &&
            gearWidget.height + 10 + infoWidget.height < bg.height
        ) {
            trophyWidth = bg.width - infoWidget.width
            PvLayouts.frame {
                spacer(bg.width, bg.height)
                PvLayouts.vertical {
                    spacer(height = 5)
                    horizontal {
                        widget(infoWidget)
                        widget(trophyWidget) {
                            alignVerticallyTop()
                        }
                    }
                }.let {
                    widget(it) {
                        alignVerticallyTop()
                        alignHorizontallyLeft()
                    }
                }
                addBottomRow(gearWidget, statWidget)
            }.applyLayout()
        } else {
            trophyWidth = bg.width - 60

            PvLayouts.vertical {
                fun add(element: LayoutElement) {
                    spacer(height = 5, width = element.width + 20)
                    widget(element) {
                        alignHorizontallyCenter()
                    }
                    spacer(height = 5)
                }

                add(infoWidget)
                add(statWidget)
                add(gearWidget)
                add(trophyWidget)
            }.asScrollable(bg.width, bg.height) {
                listWidgetInstance = this
                this.mouseScrolled(0.0, 0.0, 0.0, -savedScrollState / 10.0)
            }.applyLayout()
        }
    }


    private fun getInfoWidget(profile: SkyBlockProfile) = PvWidgets.label(
        "Information",
        PvLayouts.vertical {
            val lastCatch = profile.trophyFish.lastCatch
            if (lastCatch == null) {
                string(Text.of("Never caught a trophy fish!") { this.color = PvColors.RED })
            } else {
                string(
                    Text.join(
                        Text.of("Last Catch: ") { this.color = PvColors.DARK_GRAY },
                        lastCatch.displayName,
                    ),
                )
            }

            val rank = TrophyRank.getById((profile.trophyFish.rewards.filter { it <= TrophyRank.entries.count() }.maxOrNull() ?: 0) - 1)

            string(
                Text.join(
                    Text.of("Trophy Rank: ") { this.color = PvColors.DARK_GRAY },
                    rank?.displayName ?: Text.of("None") { this.color = PvColors.RED },
                ),
            )

            if (!profile.onStranded) {
                addFishingPerk(profile, "drake_piper")
                addFishingPerk(profile, "midas_lure")
                addFishingPerk(profile, "radiant_fisher")
            }

            val seaCreatureKills = profile.petMilestones["sea_creatures_killed"] ?: 0
            val dolphin = DolphinBracket.getByKills(seaCreatureKills)

            display(
                ExtraDisplays.text(
                    Text.join(
                        Text.of("Dolphin Pet: ") { this.color = PvColors.DARK_GRAY },
                        dolphin?.rarity?.displayText ?: Text.of("None") { this.color = PvColors.RED },
                    ),
                    shadow = false,
                ).withTooltip(
                    Text.join(
                        Text.of("Sea Creatures Killed: ") { this.color = PvColors.WHITE },
                        Text.of(seaCreatureKills.toFormattedString()) { this.color = PvColors.AQUA },
                    ),
                    "",
                    DolphinBracket.entries.map {
                        whiteText {
                            val hasObtained = it.killsRequired <= seaCreatureKills
                            if (!hasObtained) {
                                this.strikethrough = true
                                this.color = PvColors.DARK_GRAY
                            }
                            append(
                                Text.of("${it.rarity.displayName} Dolphin") {
                                    this.color = PvColors.DARK_GRAY
                                    if (hasObtained) {
                                        withColor((it.rarity.color))
                                    }
                                },
                            )
                            append("!")
                        }
                    },
                ),
            )
        },
        padding = 10,
        icon = SkyBlockPv.id("icon/item/clipboard"),
    )

    private fun getStatWidget(profile: SkyBlockProfile) = PvWidgets.label(
        "Stats",
        PvLayouts.vertical {
            val sharksKilled = profile.miscFishData.festivalSharksKilled
            display(
                ExtraDisplays.text(
                    text("Festival sharks killed: ") {

                        append(
                            text(sharksKilled.coerceAtMost(5000).toFormattedString()) {
                                when (sharksKilled) {
                                    in 5000..Int.MAX_VALUE -> PvColors.GREEN
                                    in 2500..<5000 -> PvColors.YELLOW
                                    in 1..<2500 -> PvColors.RED
                                    else -> PvColors.DARK_RED
                                }.let { this.color = it }
                            },
                        )
                        append("/")
                        append(5000.toFormattedString())
                    },
                    shadow = false,
                ).withTooltip(
                    whiteText {
                        append(
                            text("+1 Sbxp ") {
                                this.color = PvColors.AQUA
                            },
                        )
                        append("per 50 sharks killed!")
                    },
                    "",
                    buildList {
                        add(
                            whiteText("Total sharks killed: ") {
                                append(sharksKilled.toFormattedString())
                            },
                        )
                        whiteText {
                            append("Total Progress: ")
                            val progress = (sharksKilled / 5000.toFloat())
                            append(
                                text("${(progress * 100).toFormattedString()}%") {
                                    this.color = PvColors.DARK_AQUA
                                },
                            )
                        }.also { if (sharksKilled < 5000) add(it) }
                    },
                ),
            )

            string(
                text("Sea creatures killed: ") {
                    append((profile.petMilestones["sea_creatures_killed"] ?: 0).toFormattedString())
                },
            )

            fun addStat(statName: String, amount: Int, config: Display.() -> Display = { this }) {
                display(
                    ExtraDisplays.text(
                        text {
                            append("$statName: ")
                            append(amount.toFormattedString())
                        },
                        shadow = false,
                    ).let(config),
                )
            }

            val itemsFished = profile.miscFishData.itemsFished

            addStat("Total Catches", itemsFished.total)
            addStat("Normal Catches", itemsFished.normal)
            addStat("Treasures Found", itemsFished.treasure + itemsFished.largeTreasure)
            addStat("Trophy Fishes Caught", profile.trophyFish.totalCatches) {
                profile.trophyFish.obtainedTypes.asSequence().mapNotNull {
                    val fishTiers = TrophyTier.entries.firstOrNull { tier ->
                        it.key.endsWith(tier.name.lowercase())
                    } ?: return@mapNotNull null
                    return@mapNotNull fishTiers to it.value
                }.groupBy { it.first }.map { it.key to it.value.sumOf { it.second } }.sortedBy { it.first.ordinal }.map {
                    whiteText("Total ") {
                        append(text(it.first.displayName))
                        append(" Caught: ")
                        append("${it.second}")
                    }
                }.toList().takeUnless { it.isEmpty() }?.let { withTooltip(it) } ?: this
            }
            addStat("Trophy Frogs Caught", profile.miscFishData.trophyFrogs.totalCatches) {
                val completed = profile.miscFishData.trophyFrogs.completedTasks.count { task ->
                    TrophyFrogType.entries.any { task.startsWith("TROPHY_${it.internalName}_") }
                }
                withTooltip(
                    whiteText("Completed Trophy Frog Tiers: ") {
                        append("$completed/${TrophyFrogType.entries.size * trophyFrogTiers.size}")
                    },
                )
            }
        },
        padding = 10,
    )

    private fun getGearWidget(profile: SkyBlockProfile) = PvWidgets.label(
        "Gear",
        PvLayouts.horizontal {
            widget(getTrophyArmor(profile))
            spacer(width = 5)
            widget(
                PvWidgets.armorAndEquipment(
                    profile,
                    ::calculateItemScore,
                    FishingGear.necklaces,
                    FishingGear.cloaks,
                    FishingGear.belts,
                    FishingGear.gloves,
                    FishingGear.armor,
                ),
            )
            spacer(width = 5)

            PvWidgets.tools(
                profile,
                ::calculateItemScore,
                FishingGear.rods,
                "icon/slot/rod",
            ).let { widget(it) }
        },
    )

    private fun getTrophyArmor(profile: SkyBlockProfile): LayoutElement {
        val trophyArmor = ItemPredicateHelper.getItemsMatching(
            profile,
            ItemPredicates.AnySkyblockID(FishingGear.trophyArmor),
        ) ?: emptyList()

        return ExtraDisplays.inventoryBackground(
            4,
            Orientation.VERTICAL,
            Displays.padding(2, PvWidgets.armorDisplay(trophyArmor)),
        ).asWidget()
    }

    private fun getSmallTrophyFishTable(profile: SkyBlockProfile, width: Int): LayoutElement {
        val trophyFishItems = TrophyFishType.entries.map { type ->
            val fishies = TrophyTier.entries.map { tier -> TrophyFish(type, tier) }.sortedBy { it.tier.ordinal }.reversed()
            val highestObtainedType = fishies.firstOrNull { profile.trophyFish.obtainedTypes.containsKey(it.apiName) || it.tier == TrophyTier.NONE }
            val caught = getCaughtInformation(fishies, profile)
            val tooltip = getCaughtInformationTooltip(fishies, profile, caught)

            val item = highestObtainedType?.takeIf { it.tier != TrophyTier.NONE }?.item ?: Items.DYE.gray().defaultInstance
            val stackText = caught[TrophyTier.NONE]?.takeIf { i -> i != 0 }?.let(numberFormatInstance::format) ?: ""

            Displays.item(item, customStackText = stackText)
                .withTooltip(highestObtainedType?.displayName, tooltip as List<*>)
        }

        val chunked = trophyFishItems.chunked(6)

        return ExtraDisplays.inventoryBackground(
            6, 3,
            Displays.padding(2, chunked.map { row -> row.map { Displays.padding(2, it) }.toRow() }.toColumn()),
        ).centerIn(width, -1).asWidget()
    }

    private fun getTrophyFishTable(profile: SkyBlockProfile, width: Int): LayoutElement {
        return TrophyFishType.entries.map { type -> getTrophyFishTableColumn(type, profile) }
            .transpose().asTable(4).centerIn(width, -1).asWidget()
    }

    private fun getTrophyFishTableColumn(types: TrophyFishType, profile: SkyBlockProfile): List<Display> {
        val fishies = TrophyTier.entries.reversed().map { tiers -> TrophyFish(types, tiers) }
        val caught = getCaughtInformation(fishies, profile)
        val caughtTooltip = getCaughtInformationTooltip(fishies, profile, caught)

        return fishies.map {
            getTrophyFishTableEntry(it, profile, caught[it.tier] ?: 0).withTooltip(
                it.displayName,
                caughtTooltip,
            )
        }
    }

    private fun getTrophyFishTableEntry(trophyFish: TrophyFish, profile: SkyBlockProfile, amountCaught: Int): Display {
        val item = if (!profile.trophyFish.obtainedTypes.containsKey(trophyFish.apiName)) {
            Displays.item(Items.DYE.gray().defaultInstance)
        } else {
            val formatAmount = numberFormatInstance.format(amountCaught)
            Displays.item(
                trophyFish.item,
                customStackText = if (formatAmount == "0") "" else formatAmount,
            )
        }

        return ExtraDisplays.inventorySlot(Displays.padding(4, item)).let {
            if (trophyFish.tier == TrophyTier.NONE) {
                return@let Displays.padding(0, 0, 0, 0, it)
            }
            return it
        }
    }

    private fun getSmallTrophyFrogTable(profile: SkyBlockProfile, width: Int): LayoutElement {
        val rows = TrophyFrogType.entries.map { type -> getSmallTrophyFrogTableEntry(type, profile) }
            .chunked(4)
            .map { row -> row.map { Displays.padding(2, it) }.toRow() }
            .toColumn()

        return ExtraDisplays.inventoryBackground(
            4, 3,
            Displays.padding(2, rows),
        ).centerIn(width, -1).asWidget()
    }

    private fun getSmallTrophyFrogTableEntry(type: TrophyFrogType, profile: SkyBlockProfile): Display {
        val highestTier = trophyFrogTiers.reversed().firstOrNull { tier ->
            profile.miscFishData.trophyFrogs.hasCompleted(TrophyFrog(type, tier))
        }
        val highestFrog = highestTier?.let { TrophyFrog(type, it) }
        val item = highestFrog?.item ?: Items.DYE.gray().defaultInstance
        val display = if (highestTier == null) {
            Displays.item(item)
        } else {
            Displays.item(item, customStackText = Text.of("✔", highestTier.displayColor))
        }

        return display.withTooltip(
            highestFrog?.displayName ?: type.displayName,
            getTrophyFrogTooltip(type, profile),
        )
    }

    private fun getCaughtInformation(fishies: List<TrophyFish>, profile: SkyBlockProfile): Map<TrophyTier, Int> {
        return fishies.associate { it.tier to profile.trophyFish.obtainedTypes.getOrDefault(it.apiName, 0) }
    }

    private fun getCaughtInformationTooltip(
        fishies: List<TrophyFish>,
        profile: SkyBlockProfile,
        caught: Map<TrophyTier, Int> = getCaughtInformation(fishies, profile),
    ) = buildList {
        add(fishies.firstOrNull()?.type?.obtaining)
        add(CommonText.EMPTY)
        TrophyTier.entries.reversed().forEach { tiers ->
            add(Text.of(tiers.displayName).append(": ").append("${caught[tiers] ?: 0}"))
        }
    }

    private fun getTrophyFrogTooltip(type: TrophyFrogType, profile: SkyBlockProfile) = buildList {
        add(type.obtaining)
        add(CommonText.EMPTY)
        trophyFrogTiers.reversed().forEach { tier ->
            val completed = profile.miscFishData.trophyFrogs.hasCompleted(TrophyFrog(type, tier))
            add(
                Text.of(tier.displayName).append(": ").append(
                    Text.of(if (completed) "✔" else "❌") {
                        color = if (completed) PvColors.GREEN else PvColors.RED
                    },
                ),
            )
        }
        add(CommonText.EMPTY)
        add(Text.of("Note: Catch counts per tier are currently not provided by Hypixel.", PvColors.GRAY))
    }

    /**
     * Creates a score for a rod to determine which ones to display
     */
    private fun calculateItemScore(itemStack: ItemStack): Int {
        fun <T> getData(type: DataType<T>): T? = itemStack.getData(type)

        var score = 0

        score += 1.takeIf { getData(DataTypes.RECOMBOBULATOR) ?: false } ?: 0

        // take the actual level of ultimate enchants since those are worth smth
        getData(DataTypes.ENCHANTMENTS)?.let {
            score += it.keys.firstOrNull { key -> key.startsWith("ultimate") }?.let { key -> it[key] } ?: 0
        }

        // only counting t5 and t6 enchants as everything else is kinda useless
        score += getData(DataTypes.ENCHANTMENTS)?.map { it.value - 4 }?.filter { it > 0 }?.sum() ?: 0

        score += getData(DataTypes.MODIFIER)?.let { 1 } ?: 0

        score += ((getData(DataTypes.RARITY)?.ordinal ?: 0) - 2).coerceIn(0, 3)

        score += listOf(getData(DataTypes.HOOK), getData(DataTypes.LINE), getData(DataTypes.SINKER)).count { it != null }


        return score
    }

    companion object {
        private val trophyFrogTiers = TrophyTier.entries.filter { it != TrophyTier.NONE }
        private val TrophyTier.displayColor
            get() = when (this) {
                TrophyTier.NONE -> PvColors.RED
                TrophyTier.BRONZE -> PvColors.DARK_GRAY
                TrophyTier.SILVER -> PvColors.GRAY
                TrophyTier.GOLD -> PvColors.GOLD
                TrophyTier.DIAMOND -> PvColors.AQUA
            }

        enum class TrophyType {
            FISH,
            FROG,
            ;

            val formattedName = toFormattedName()
            override fun toString(): String = formattedName
        }
    }
}
