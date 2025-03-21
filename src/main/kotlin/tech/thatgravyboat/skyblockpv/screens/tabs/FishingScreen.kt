package tech.thatgravyboat.skyblockpv.screens.tabs

import com.mojang.authlib.GameProfile
import net.minecraft.ChatFormatting
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.client.gui.layouts.LayoutElement
import net.minecraft.client.gui.layouts.LayoutSettings
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockpv.SkyBlockPv
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.api.predicates.ItemPredicateHelper
import tech.thatgravyboat.skyblockpv.api.predicates.ItemPredicates
import tech.thatgravyboat.skyblockpv.data.*
import tech.thatgravyboat.skyblockpv.data.EssenceData.addPerk
import tech.thatgravyboat.skyblockpv.screens.BasePvScreen
import tech.thatgravyboat.skyblockpv.utils.LayoutBuild
import tech.thatgravyboat.skyblockpv.utils.LayoutBuilder
import tech.thatgravyboat.skyblockpv.utils.LayoutBuilder.Companion.setPos
import tech.thatgravyboat.skyblockpv.utils.Utils
import tech.thatgravyboat.skyblockpv.utils.Utils.asScrollable
import tech.thatgravyboat.skyblockpv.utils.Utils.text
import tech.thatgravyboat.skyblockpv.utils.Utils.transpose
import tech.thatgravyboat.skyblockpv.utils.Utils.whiteText
import tech.thatgravyboat.skyblockpv.utils.displays.*
import java.math.RoundingMode
import java.text.DecimalFormat

class FishingScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) :
    BasePvScreen("Fishing", gameProfile, profile) {

    private val numberFormatInstance = DecimalFormat.getCompactNumberInstance().apply {
        this.roundingMode = RoundingMode.FLOOR
    }

    override fun create(bg: DisplayWidget) {
        val profile = profile ?: return

        val infoWidget = getInfoWidget(profile)
        val statWidget = getStatWidget(profile)
        val gearWidget = getGearWidget(profile)
        var trophyWidth = 0
        val trophyWidget by lazy {
            LayoutBuild.vertical {
                val useSmallTable = (trophyWidth < 480)
                widget(Utils.getTitleWidget("Trophy Fish", trophyWidth))
                if (useSmallTable) {
                    widget(Utils.getMainContentWidget(getSmallTrophyTable(profile), trophyWidth))
                } else {
                    widget(Utils.getMainContentWidget(getTrophyTable(profile), trophyWidth))
                }
                spacer(height = 5)
            }
        }

        fun LayoutBuilder.addBottomRow(first: LayoutElement, second: LayoutElement) {
            LayoutBuild.vertical {
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
            LayoutBuild.frame {
                spacer(bg.width, bg.height)
                LayoutBuild.vertical {
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
            LayoutBuild.frame {
                spacer(bg.width, bg.height)
                LayoutBuild.vertical {
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
            LayoutBuild.frame {
                spacer(bg.width, bg.height)
                LayoutBuild.vertical {
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

            LayoutBuild.vertical {
                fun add(element: LayoutElement)  {
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


    private fun getInfoWidget(profile: SkyBlockProfile) = createWidget(
        "Information",
        LayoutBuild.vertical {
            if (profile.trophyFish.lastCatch == null) {
                string(Text.of("Never caught a trophy fish!") { this.color = TextColor.RED })
            } else {
                string(Text.join(
                    Text.of("Last Catch: ") { this.color = TextColor.DARK_GRAY },
                    profile.trophyFish.lastCatch.displayName
                ))
            }

            val rank = TrophyFishRanks.getById((profile.trophyFish.rewards.maxOrNull() ?: 0) - 1)

            string(
                Text.join(
                    Text.of("Trophy Rank: ") { this.color = TextColor.DARK_GRAY },
                    rank?.displayName ?: Text.of("None") { this.color = TextColor.RED }
                ),
            )

            addPerk(profile, "drake_piper")
            addPerk(profile, "midas_lure")
            addPerk(profile, "radiant_fisher")

            val seaCreatureKills = profile.petMilestones["sea_creatures_killed"] ?: 0
            val dolphin = DolphinBrackets.getByKills(seaCreatureKills)

            display(
                Displays.text(
                    Text.join(
                        Text.of("Dolphin Pet: ") { this.color = TextColor.DARK_GRAY },
                        dolphin?.rarity?.displayText ?: Text.of("None") { this.color = TextColor.RED }
                    ),
                    shadow = false,
                ).withTooltip(
                    Text.join(
                        Text.of("Sea Creatures Killed: ") { this.color = TextColor.WHITE },
                        Text.of(seaCreatureKills.toFormattedString()) { this.color = TextColor.AQUA }
                    ),
                    "",
                    DolphinBrackets.entries.map {
                        whiteText {
                            val hasObtained = it.killsRequired <= seaCreatureKills
                            if (!hasObtained) {
                                withStyle(ChatFormatting.STRIKETHROUGH)
                                withStyle(ChatFormatting.DARK_GRAY)
                            }
                            append(
                                text("${it.rarity.displayName} Dolphin") {
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
        padding = 30,
    )

    private fun getStatWidget(profile: SkyBlockProfile) = createWidget(
        "Stats",
        LayoutBuild.vertical {
            val sharksKilled = profile.miscFishData.festivalSharksKilled
            display(
                Displays.text(
                    text("Festival sharks killed: ") {

                        append(
                            text(sharksKilled.coerceAtMost(5000).toFormattedString()) {
                                when (sharksKilled) {
                                    in 5000..Int.MAX_VALUE -> ChatFormatting.GREEN
                                    in 2500..<5000 -> ChatFormatting.YELLOW
                                    in 1..<2500 -> ChatFormatting.RED
                                    else -> ChatFormatting.DARK_RED
                                }.let { withStyle(it) }
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
                                withStyle(ChatFormatting.AQUA)
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
                                    withStyle(ChatFormatting.DARK_AQUA)
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
                    Displays.text(
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
                    val fishTiers = TrophyFishTiers.entries.firstOrNull { tier ->
                        it.key.endsWith(tier.name.lowercase())
                    } ?: return@mapNotNull null
                    return@mapNotNull fishTiers to it.value
                }.groupBy { it.first }.map { it.key to it.value.sumOf { it.second } }.sortedBy { it.first.ordinal }.map {
                    whiteText("Total ") {
                        append(text(it.first.displayName))
                        append(" Caught: ")
                        append("${it.second}")
                    }
                }.toList().takeUnless { it.isEmpty() }?.let { withTooltip(it) }?: this
            }
        },
        padding = 30,
    )

    private fun getGearWidget(profile: SkyBlockProfile) = createWidget("Gear", LayoutBuild.horizontal {
        widget(getTrophyArmor(profile))
        spacer(width = 5)
        widget(getArmorAndEquipment(profile))
        spacer(width = 5)
        widget(getRods(profile))
    }, padding = 20)

    private fun getDisplayArmor(list: List<ItemStack>) = buildList {
        fun addArmor(type: String) {
            val itemStack = list.firstOrNull { it.getData(DataTypes.ID)?.contains(type) != false } ?: ItemStack.EMPTY
            add(
                Displays.padding(
                    2,
                    Displays.item(itemStack, showTooltip = true)
                        .let {
                            return@let if (itemStack.isEmpty) {
                                Displays.background(
                                    ResourceLocation.parse("container/slot/${type.lowercase()}"),
                                    it
                                ).centerIn(-1, -1)
                            } else {
                                it
                            }
                        },
                ),
            )
        }

        addArmor("HELMET")
        addArmor("CHESTPLATE")
        addArmor("LEGGINGS")
        addArmor("BOOTS")
    }

    private fun getTrophyArmor(profile: SkyBlockProfile): LayoutElement {
        val trophyArmor = ItemPredicateHelper.getItemsMatching(
            profile,
            ItemPredicates.AnySkyblockID(FishingEquipment.trophyArmor)
        ) ?: emptyList()

        val displayArmor = getDisplayArmor(trophyArmor).toColumn()
        return Displays.background(
            SkyBlockPv.id("inventory/inventory-1x4"),
            Displays.padding(2, displayArmor),
        ).asWidget()
    }

    private fun getArmorAndEquipment(profile: SkyBlockProfile): LayoutElement {
        val armorAndEquipment = ItemPredicateHelper.getItemsMatching(
            profile,
            ItemPredicates.AnySkyblockID(FishingEquipment.armor).or(ItemPredicates.AnySkyblockID(FishingEquipment.equipment)),
        )?.sortedBy(::calculateItemScore)?.reversed() ?: emptyList()

        val displayArmor = getDisplayArmor(armorAndEquipment)

        val displayEquipment = buildList {
            fun addEquipment(type: FishingEquipment) {
                val item = armorAndEquipment.firstOrNull { it.getData(DataTypes.ID)?.let { id -> type.list.contains(id) } == true } ?: ItemStack.EMPTY
                val display = if (item.isEmpty) {
                    Displays.background(SkyBlockPv.id("icon/slot/${type.name.lowercase().dropLast(1)}"), Displays.empty(16, 16))
                } else {
                    Displays.item(item, showTooltip = true)
                }

                add(Displays.padding(2, display))
            }

            addEquipment(FishingEquipment.NECKLACES)
            addEquipment(FishingEquipment.CLOAKS)
            addEquipment(FishingEquipment.BELTS)
            addEquipment(FishingEquipment.GLOVES)
        }

        val armorEquipment = listOf(
            displayArmor.toColumn(),
            displayEquipment.toColumn(),
        ).toRow()
        return Displays.background(
            SkyBlockPv.id("inventory/inventory-2x4"),
            Displays.padding(2, armorEquipment),
        ).centerIn(-1, -1).asWidget()
    }

    private fun getRods(profile: SkyBlockProfile): LayoutElement {
        val fishingRods = ItemPredicateHelper.getItemsMatching(
            profile = profile,
            predicate = ItemPredicates.AnySkyblockID(FishingEquipment.rods),
        )?.sortedBy(::calculateItemScore)?.reversed()?.take(4)?.toMutableList() ?: mutableListOf()

        while (fishingRods.size < 4) {
            fishingRods.add(Items.AIR.defaultInstance)
        }

        val column = fishingRods.map {
            Displays.item(it, showTooltip = true)
                .let { display ->
                    if (it.isEmpty) {
                        return@let Displays.background(SkyBlockPv.id("icon/slot/rod"), display)
                    }

                    display
                }.let { Displays.padding(2, it) }
        }.toColumn()

        return Displays.background(
            SkyBlockPv.id("inventory/inventory-1x4"),
            Displays.padding(2, column),
        ).centerIn(-1, -1).asWidget()
    }


    private fun getSmallTrophyTable(profile: SkyBlockProfile): LayoutElement {
        val trophyFishItems = TrophyFishTypes.entries.map { type ->
            val fishies = TrophyFishTiers.entries.map { tier -> TrophyFish(type, tier) }.sortedBy { it.tier.ordinal }.reversed()
            val highestObtainedType = fishies.firstOrNull { profile.trophyFish.obtainedTypes.containsKey(it.apiName) || it.tier == TrophyFishTiers.NONE }
            val caught = getCaughtInformation(fishies, profile)
            val tooltip = getCaughtInformationTooltip(fishies, profile, caught)

            val item = highestObtainedType?.takeIf { it.tier != TrophyFishTiers.NONE }?.item ?: Items.GRAY_DYE.defaultInstance
            val stackText = caught[TrophyFishTiers.NONE]?.takeIf { i -> i != 0 }?.let(numberFormatInstance::format) ?: ""

            Displays.item(item, customStackText = stackText)
                .withTooltip(highestObtainedType?.displayName, tooltip as List<*>)
        }

        val chunked = trophyFishItems.chunked(6)

        return Displays.background(
            SkyBlockPv.id("inventory/inventory-6x3"),
            Displays.padding(2, chunked.map { row -> row.map { Displays.padding(2, it) }.toRow() }.toColumn()),
        ).asWidget()
    }

    private fun getTrophyTable(profile: SkyBlockProfile): LayoutElement {
        return TrophyFishTypes.entries.map { type -> getTrophyTableColumn(type, profile) }.transpose().asTable(4).centerIn(uiWidth, -1).asWidget()
    }

    private fun getCaughtInformation(fishies: List<TrophyFish>, profile: SkyBlockProfile): Map<TrophyFishTiers, Int> {
        return fishies.associate { it.tier to profile.trophyFish.obtainedTypes.getOrDefault(it.apiName, 0) }
    }

    private fun getCaughtInformationTooltip(
        fishies: List<TrophyFish>,
        profile: SkyBlockProfile,
        caught: Map<TrophyFishTiers, Int> = getCaughtInformation(fishies, profile),
    ): List<MutableComponent> {
        return TrophyFishTiers.entries.reversed().map { tiers ->
            Text.of(tiers.displayName).append(": ").append("${caught[tiers] ?: 0}")
        }
    }

    private fun getTrophyTableColumn(types: TrophyFishTypes, profile: SkyBlockProfile): List<Display> {
        val fishies = TrophyFishTiers.entries.reversed().map { tiers -> TrophyFish(types, tiers) }
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

        return Displays.background(
            SkyBlockPv.id("inventory/inventory-1x1"),
            Displays.padding(3, item),
        ).centerIn(-1, -1).let {
            if (trophyFish.tier == TrophyFishTiers.NONE) {
                return@let Displays.padding(0, 0, 0, 0, it)
            }
            return it
        }
    }


    /**
     * Creates a score for a rod to determine which ones to display
     */
    private fun calculateItemScore(itemStack: ItemStack): Int {
        var score = 0

        score += itemStack.getData(DataTypes.RARITY_UPGRADES) ?: 0

        // take the actual level of ultimate enchants since those are worth smth
        itemStack.getData(DataTypes.ENCHANTMENTS)?.let {
            score += it.keys.firstOrNull { key -> key.startsWith("ultimate") }?.let { key -> it[key] } ?: 0
        }

        // only counting t8 and above, since t7 are just 64 t1s, maybe this still has to be tweaked
        score += itemStack.getData(DataTypes.ATTRIBUTES)?.map { it.value - 7 }?.filter { it > 0 }?.sum() ?: 0

        // only counting t5 and t6 enchants as everything else is kinda useless
        score += itemStack.getData(DataTypes.ENCHANTMENTS)?.map { it.value - 4 }?.filter { it > 0 }?.sum() ?: 0

        score += itemStack.getData(DataTypes.MODIFIER)?.let { 1 } ?: 0

        score += ((itemStack.getData(DataTypes.RARITY)?.ordinal ?: 0) - 2).coerceIn(0, 3)


        return score
    }

    private fun createWidget(title: String, element: LayoutElement, padding: Int = 0) = LayoutBuild.vertical {
        widget(Utils.getTitleWidget(title, element.width + padding))
        widget(Utils.getMainContentWidget(element, element.width + padding))
    }
}
