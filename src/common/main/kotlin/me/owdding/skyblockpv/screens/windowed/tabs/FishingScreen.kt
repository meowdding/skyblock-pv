package me.owdding.skyblockpv.screens.windowed.tabs

import com.mojang.authlib.GameProfile
import earth.terrarium.olympus.client.utils.Orientation
import me.owdding.lib.builder.LayoutBuilder
import me.owdding.lib.displays.*
import me.owdding.lib.extensions.transpose
import me.owdding.lib.layouts.setPos
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.api.data.SkyBlockProfile
import me.owdding.skyblockpv.api.predicates.ItemPredicateHelper
import me.owdding.skyblockpv.api.predicates.ItemPredicates
import me.owdding.skyblockpv.data.api.skills.*
import me.owdding.skyblockpv.data.repo.EssenceData.addFishingPerk
import me.owdding.skyblockpv.screens.windowed.BasePvWindowedScreen
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
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.text.CommonText
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.strikethrough
import java.math.RoundingMode
import java.text.DecimalFormat

class FishingScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) :
    BasePvWindowedScreen("Fishing", gameProfile, profile) {

    private val numberFormatInstance = DecimalFormat.getCompactNumberInstance().apply {
        this.roundingMode = RoundingMode.FLOOR
    }

    override fun create(bg: DisplayWidget) {
        val infoWidget = getInfoWidget(profile)
        val statWidget = getStatWidget(profile)
        val gearWidget = getGearWidget(profile)
        var trophyWidth = 0
        val trophyWidget by lazy {
            PvLayouts.vertical {
                val useSmallTable = (trophyWidth < 480)
                widget(PvWidgets.getTitleWidget("Trophy Fish", trophyWidth))
                if (useSmallTable) {
                    widget(PvWidgets.getMainContentWidget(getSmallTrophyTable(profile), trophyWidth))
                } else {
                    widget(PvWidgets.getMainContentWidget(getTrophyTable(profile), trophyWidth))
                }
                spacer(height = 5)
            }
        }

        fun LayoutBuilder.addBottomRow(first: LayoutElement, second: LayoutElement) {
            PvLayouts.vertical {
                spacer(height = 5)
                horizontal {
                    widget(first)
                    widget(second, LayoutSettings::alignVerticallyBottom)
                }
                spacer(height = 5)
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
                widget(trophyWidget) {
                    alignVerticallyBottom()
                    alignHorizontallyLeft()
                }
            }.applyLayout()
        } else if (infoWidget.width + statWidget.width < bg.width && gearWidget.height + 10 + infoWidget.height < bg.height) {
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
        } else if (gearWidget.width + statWidget.width < bg.width && gearWidget.height + 10 + infoWidget.height < bg.height) {
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
            }.asScrollable(bg.width, bg.height).applyLayout()
        }
    }


    private fun getInfoWidget(profile: SkyBlockProfile) = PvWidgets.label(
        "Information",
        PvLayouts.vertical {
            if (profile.trophyFish.lastCatch == null) {
                string(Text.of("Never caught a trophy fish!") { this.color = PvColors.RED })
            } else {
                string(
                    Text.join(
                        Text.of("Last Catch: ") { this.color = PvColors.DARK_GRAY },
                        profile.trophyFish.lastCatch.displayName,
                    ),
                )
            }

            val rank = TrophyFishRank.getById((profile.trophyFish.rewards.filter { it <= TrophyFishRank.entries.count() }.maxOrNull() ?: 0) - 1)

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
                    val fishTiers = TrophyFishTier.entries.firstOrNull { tier ->
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

    private fun getSmallTrophyTable(profile: SkyBlockProfile): LayoutElement {
        val trophyFishItems = TrophyFishType.entries.map { type ->
            val fishies = TrophyFishTier.entries.map { tier -> TrophyFish(type, tier) }.sortedBy { it.tier.ordinal }.reversed()
            val highestObtainedType = fishies.firstOrNull { profile.trophyFish.obtainedTypes.containsKey(it.apiName) || it.tier == TrophyFishTier.NONE }
            val caught = getCaughtInformation(fishies, profile)
            val tooltip = getCaughtInformationTooltip(fishies, profile, caught)

            val item = highestObtainedType?.takeIf { it.tier != TrophyFishTier.NONE }?.item ?: Items.GRAY_DYE.defaultInstance
            val stackText = caught[TrophyFishTier.NONE]?.takeIf { i -> i != 0 }?.let(numberFormatInstance::format) ?: ""

            Displays.item(item, customStackText = stackText)
                .withTooltip(highestObtainedType?.displayName, tooltip as List<*>)
        }

        val chunked = trophyFishItems.chunked(6)

        return ExtraDisplays.inventoryBackground(
            6, 3,
            Displays.padding(2, chunked.map { row -> row.map { Displays.padding(2, it) }.toRow() }.toColumn()),
        ).asWidget()
    }

    private fun getTrophyTable(profile: SkyBlockProfile): LayoutElement {
        return TrophyFishType.entries.map { type -> getTrophyTableColumn(type, profile) }.transpose().asTable(4).centerIn(uiWidth, -1).asWidget()
    }

    private fun getCaughtInformation(fishies: List<TrophyFish>, profile: SkyBlockProfile): Map<TrophyFishTier, Int> {
        return fishies.associate { it.tier to profile.trophyFish.obtainedTypes.getOrDefault(it.apiName, 0) }
    }

    private fun getCaughtInformationTooltip(
        fishies: List<TrophyFish>,
        profile: SkyBlockProfile,
        caught: Map<TrophyFishTier, Int> = getCaughtInformation(fishies, profile),
    ) = buildList {
        add(fishies.firstOrNull()?.type?.obtaining)
        add(CommonText.EMPTY)
        TrophyFishTier.entries.reversed().forEach { tiers ->
            add(Text.of(tiers.displayName).append(": ").append("${caught[tiers] ?: 0}"))
        }
    }

    private fun getTrophyTableColumn(types: TrophyFishType, profile: SkyBlockProfile): List<Display> {
        val fishies = TrophyFishTier.entries.reversed().map { tiers -> TrophyFish(types, tiers) }
        val caught = getCaughtInformation(fishies, profile)
        val caughtTooltip = getCaughtInformationTooltip(fishies, profile, caught)

        return fishies.map {
            getTrophyTableEntry(it, profile, caught[it.tier] ?: 0).withTooltip(
                it.displayName,
                caughtTooltip,
            )
        }
    }

    private fun getTrophyTableEntry(trophyFish: TrophyFish, profile: SkyBlockProfile, amountCaught: Int): Display {
        val item = if (!profile.trophyFish.obtainedTypes.containsKey(trophyFish.apiName)) {
            Displays.item(Items.GRAY_DYE.defaultInstance)
        } else {
            Displays.item(
                trophyFish.item,
                customStackText = numberFormatInstance.format(amountCaught),
            )
        }

        return ExtraDisplays.inventorySlot(Displays.padding(3, item)).let {
            if (trophyFish.tier == TrophyFishTier.NONE) {
                return@let Displays.padding(0, 0, 0, 0, it)
            }
            return it
        }
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

        // only counting t8 and above, since t7 are just 64 t1s, maybe this still has to be tweaked
        score += getData(DataTypes.ATTRIBUTES)?.map { it.value - 7 }?.filter { it > 0 }?.sum() ?: 0

        // only counting t5 and t6 enchants as everything else is kinda useless
        score += getData(DataTypes.ENCHANTMENTS)?.map { it.value - 4 }?.filter { it > 0 }?.sum() ?: 0

        score += getData(DataTypes.MODIFIER)?.let { 1 } ?: 0

        score += ((getData(DataTypes.RARITY)?.ordinal ?: 0) - 2).coerceIn(0, 3)

        score += listOf(getData(DataTypes.HOOK), getData(DataTypes.LINE), getData(DataTypes.SINKER)).count { it != null }


        return score
    }
}
