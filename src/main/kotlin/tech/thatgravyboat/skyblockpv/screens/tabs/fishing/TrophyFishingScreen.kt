package tech.thatgravyboat.skyblockpv.screens.tabs.fishing

import com.mojang.authlib.GameProfile
import net.minecraft.ChatFormatting
import net.minecraft.client.gui.layouts.LayoutElement
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.api.data.SkyBlockRarity
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockpv.SkyBlockPv
import tech.thatgravyboat.skyblockpv.api.ItemApi
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.api.or
import tech.thatgravyboat.skyblockpv.data.*
import tech.thatgravyboat.skyblockpv.screens.BasePvScreen
import tech.thatgravyboat.skyblockpv.utils.LayoutBuild
import tech.thatgravyboat.skyblockpv.utils.LayoutBuilder
import tech.thatgravyboat.skyblockpv.utils.LayoutBuilder.Companion.setPos
import tech.thatgravyboat.skyblockpv.utils.Utils.getMainContentWidget
import tech.thatgravyboat.skyblockpv.utils.Utils.getTitleWidget
import tech.thatgravyboat.skyblockpv.utils.displays.*
import java.math.RoundingMode
import java.text.DecimalFormat

private val SkyBlockRarity.displayname: String
    get() = name.lowercase().replaceFirstChar { it.uppercase() }

class TrophyFishingScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) :
    BasePvScreen("Fishing", gameProfile, profile) {

    private val numberFormatInstance = DecimalFormat.getCompactNumberInstance().apply {
        this.roundingMode = RoundingMode.FLOOR
    }

    override fun create(bg: DisplayWidget) {
        val profile = profile ?: return

        val useSmallTable = (bg.width < 480)

        LayoutBuild.frame {
            spacer(bg.width, bg.height)
            LayoutBuild.vertical {
                spacer(height = 5)
                LayoutBuild.horizontal {
                    widget(getInfoWidget(profile))
                    widget(getStatWidget(profile))
                    widget(getGearWidget(profile, 122))
                }.let { widget(it) }
            }.let {
                widget(it) {
                    alignVerticallyTop()
                    alignHorizontallyLeft()
                }
            }
            widget(
                LayoutBuild.vertical {
                    widget(getTitleWidget("Trophy Fish", uiWidth))
                    if (useSmallTable) {
                        widget(getMainContentWidget(getSmallTrophyTable(profile), uiWidth))
                    } else {
                        widget(getMainContentWidget(getTrophyTable(profile), uiWidth))
                    }
                    spacer(height = 5)
                },
            ) {
                alignVerticallyBottom()
            }
        }.setPos(bg.x, bg.y).visitWidgets(this::addRenderableWidget)
    }

    private fun getStatWidget(profile: SkyBlockProfile) = LayoutBuild.vertical {
        addWidget(
            "Stats",
            LayoutBuild.vertical {
                val sharksKilled = profile.miscFishData.festivalSharksKilled
                display(
                    Displays.text(
                        text("Festival sharks killed: ") {

                            append(
                                text(sharksKilled.coerceAtMost(5000).toFormattedString()) {
                                    if (sharksKilled > 5000) {
                                        withStyle(ChatFormatting.GREEN)
                                    } else if (sharksKilled >= 2500) {
                                        withStyle(ChatFormatting.YELLOW)
                                    } else if (sharksKilled > 0) {
                                        withStyle(ChatFormatting.RED)
                                    } else {
                                        withStyle(ChatFormatting.DARK_RED)
                                    }
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

                display(
                    Displays.text(
                        text("Sea creatures killed: ") {
                            append(profile.miscFishData.seaCreatureKills.toFormattedString())
                        },
                        shadow = false,
                    ),
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
                    withTooltip(
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
                        }.toList(),

                        )
                }
            },
            padding = 30,
        )
    }

    private fun getSmallTrophyTable(profile: SkyBlockProfile): LayoutElement {
        val trophyFishItems = TrophyFishTypes.entries.map { type ->
            val fishies = TrophyFishTiers.entries.map { tier -> TrophyFish(type, tier) }.sortedBy { it.tier.ordinal }.reversed()
            val highestObtainedType = fishies.firstOrNull { profile.trophyFish.obtainedTypes.containsKey(it.apiName) || it.tier == TrophyFishTiers.NONE }
            val caught = getCaughtInformation(fishies, profile)
            val tooltip = getCaughtInformationTooltip(fishies, profile, caught)

            val item = if (highestObtainedType?.tier == TrophyFishTiers.NONE) {
                Items.GRAY_DYE.defaultInstance
            } else {
                highestObtainedType?.item ?: Items.GRAY_DYE.defaultInstance
            }

            val stackText = if (caught[TrophyFishTiers.NONE] == 0) "" else numberFormatInstance.format(caught[TrophyFishTiers.NONE])

            Displays.item(item, customStackText = stackText).withTooltip(highestObtainedType?.displayName, tooltip as List<*>)
        }

        val chunked = trophyFishItems.chunked(6)

        return Displays.background(
            SkyBlockPv.id("inventory/inventory-6x3"),
            Displays.padding(2, chunked.map { row -> row.map { Displays.padding(2, it) }.toRow() }.toColumn()),
        ).asWidget()
    }

    private fun getGearWidget(profile: SkyBlockProfile, width: Int) = LayoutBuild.vertical {
        widget(getTitleWidget("Gear", width))

        LayoutBuild.horizontal {
            widget(getTrophyArmor(profile))
            spacer(width = 5)
            widget(getArmorAndEquipment(profile))
            spacer(width = 5)
            widget(getRods(profile))
        }.let { widget(getMainContentWidget(it, width)) }
    }

    private fun getTrophyArmor(profile: SkyBlockProfile): LayoutElement {
        val trophyArmor = ItemApi.getItemsMatching(
            profile,
            ItemApi.anySkyblockId(FishingEquipment.trophyArmor),
        ) ?: emptyList()

        val displayArmor = getDisplayArmor(trophyArmor).toColumn()
        return Displays.background(
            SkyBlockPv.id("inventory/inventory-1x4"),
            Displays.padding(2, displayArmor),
        ).asWidget()
    }

    private fun getDisplayArmor(list: List<ItemStack>) = buildList {
        fun addArmor(type: String) {
            val itemStack = list.firstOrNull { it.getData(DataTypes.ID)?.contains(type) ?: false } ?: ItemStack.EMPTY
            add(
                Displays.padding(
                    2,
                    Displays.item(itemStack, showTooltip = true)
                        .let {
                            if (itemStack.isEmpty) {
                                return@let Displays.background(ResourceLocation.parse("container/slot/${type.lowercase()}"), it).centerIn(-1, -1)
                            } else {
                                return@let it
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

    private fun getArmorAndEquipment(profile: SkyBlockProfile): LayoutElement {
        val armorAndEquipment = ItemApi.getItemsMatching(
            profile,
            ItemApi.anySkyblockId(FishingEquipment.armor).or(ItemApi.anySkyblockId(FishingEquipment.equipment)),
        )?.sortedBy(::calculateItemScore)?.reversed() ?: emptyList()

        val displayArmor = getDisplayArmor(armorAndEquipment)

        val displayEquipment = buildList {
            fun addEquipment(type: FishingEquipment) {
                val item = armorAndEquipment.firstOrNull { it.getData(DataTypes.ID)?.let { id -> type.list.contains(id) } ?: false } ?: ItemStack.EMPTY

                Displays.item(item = item, showTooltip = true)
                    .let {
                        if (item.isEmpty) {
                            return@let Displays.background(SkyBlockPv.id("icon/slot/${type.name.lowercase().dropLast(1)}"), it)
                        }

                        it
                    }.let { Displays.padding(2, it) }
                    .let { add(it) }
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
        val fishingRods = ItemApi.getItemsMatching(
            profile = profile,
            predicate = ItemApi.anySkyblockId(FishingEquipment.rods),
        )?.sortedBy(::calculateItemScore)?.reversed()?.take(4)?.toMutableList() ?: mutableListOf()

        if (fishingRods.size != 4) {
            for (i in (fishingRods.size - 1).coerceAtLeast(0)..3) {
                fishingRods.add(i, Items.AIR.defaultInstance)
            }
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

    /**
     * Creates a score for a rod to determine which ones to display
     */
    private fun calculateItemScore(itemStack: ItemStack): Int {
        var score = 0

        // TODO add attributes to calculation after skyblockapi #55 is merged
        score += itemStack.getData(DataTypes.RARITY_UPGRADES) ?: 0

        // take the actual level of ultimate enchants since those are worth smth
        itemStack.getData(DataTypes.ENCHANTMENTS)?.let {
            score += it.keys.firstOrNull { key -> key.startsWith("ultimate") }?.let { key -> it[key] } ?: 0
        }

        // only counting t5 and t6 enchants as everything else is kinda useless
        score += itemStack.getData(DataTypes.ENCHANTMENTS)?.map { it.value - 4 }?.filter { it > 0 }?.sum() ?: 0

        score += itemStack.getData(DataTypes.MODIFIER)?.let { 1 } ?: 0

        score += ((itemStack.getData(DataTypes.RARITY)?.ordinal ?: 0) - 2).coerceIn(0, 3)


        return score
    }

    private fun LayoutBuilder.addWidget(title: String, element: LayoutElement, padding: Int = 0) {
        widget(getTitleWidget(title, element.width + padding))
        widget(getMainContentWidget(element, element.width + padding))
    }

    private fun text(text: String = "", color: UInt = 0x555555u, init: MutableComponent.() -> Unit = {}): MutableComponent {
        return Text.of(text) {
            withColor(color.toInt())
            init(this)
        }
    }

    private fun whiteText(text: String = "", init: MutableComponent.() -> Unit = {}) = text(text, 0xFFFFFFu, init)

    private fun getInfoWidget(profile: SkyBlockProfile) = LayoutBuild.vertical {
        addWidget(
            "Information",
            LayoutBuild.vertical {
                display(
                    Displays.text(
                        text {
                            if (profile.trophyFish.lastCatch == null) {
                                append(Text.of("Never caught a trophy fish!") { withStyle(ChatFormatting.RED) })
                                return@text
                            }

                            append(
                                text("Last Catch: ") {
                                    append(profile.trophyFish.lastCatch.displayName)
                                },
                            )
                        },
                        shadow = false,
                    ),
                )

                display(
                    Displays.text(
                        text("Trophy Rank: ") {
                            val rank = TrophyFishRanks.getById((profile.trophyFish.rewards.maxOrNull() ?: 0) - 1)

                            if (rank == null) {
                                append(text("None") { withStyle(ChatFormatting.RED) })
                                return@text
                            }

                            append(rank.displayName)
                        },
                        shadow = false,
                    ),
                )

                fun addPerk(perkName: String, perkLevel: Int, maxLevel: Int, config: Display.() -> Display = { this }) {
                    display(
                        Displays.text(
                            text {
                                append("$perkName: ")
                                append(
                                    text("$perkLevel") {
                                        if (perkLevel == maxLevel) {
                                            withStyle(ChatFormatting.GREEN)
                                        } else {
                                            withStyle(ChatFormatting.RED)
                                        }
                                    },
                                )
                                append("/$maxLevel")
                            },
                            shadow = false,
                        ).let(config),
                    )
                }

                addPerk("Drake Piper", profile.miscFishData.drakePiper, 1) {
                    withTooltip(
                        whiteText("Can be upgraded at the ") {
                            append(text("Ice Essence Shop") { withStyle(ChatFormatting.LIGHT_PURPLE) })
                        },
                        whiteText("on ") {
                            append(whiteText("Jerry's Workshop") { withStyle(ChatFormatting.RED) })
                        },
                        "",
                        whiteText("Increase the chance to spawn a ") {
                            append(whiteText("Reindrake") { withStyle(ChatFormatting.GOLD) })
                        },
                        whiteText("by ") {
                            append(text("10%") { withStyle(ChatFormatting.GREEN) })
                            append(" while on ")
                            append(text("Jerry's Workshop") { withStyle(ChatFormatting.RED) })
                            append(".")
                        },
                    )
                }
                addPerk("Midas Lure", profile.miscFishData.midasLure, 10) {
                    withTooltip(
                        whiteText("Can be upgraded at the ") {
                            append(text("Gold Essence Shop") { withStyle(ChatFormatting.LIGHT_PURPLE) })
                        },
                        whiteText("in the ") {
                            append(text("Royal Mines") { withStyle(ChatFormatting.GOLD) })
                            append(" in the ")
                            append(text("Dwarven Mines") { withStyle(ChatFormatting.DARK_GREEN) })
                        },
                        "",
                        whiteText("Increases your chances of fishing up a ") {
                        },
                        whiteText {
                            append(text("Gold Trophy Fish") { withStyle(ChatFormatting.GOLD) })
                            append(" in the ")
                            append(text("Crimson Isle") { withStyle(ChatFormatting.RED) })
                            append(" by ")
                            append(text("2-20%") { withStyle(ChatFormatting.GREEN) })
                            append(".")
                        },
                    )
                }
                addPerk("Radiant Fisher", profile.miscFishData.radiantFisher, 10) {
                    withTooltip(
                        whiteText("Can be upgraded at the ") {
                            append(text("Diamond Essence Shop") { withStyle(ChatFormatting.LIGHT_PURPLE) })
                        },
                        whiteText("in the ") {
                            append(text("Crystal Nucleus") { withStyle(ChatFormatting.DARK_PURPLE) })
                            append(" in the ")
                            append(text("Crystal Hollows") { withStyle(ChatFormatting.DARK_PURPLE) })
                        },
                        "",
                        whiteText("Increases your chances of fishing up a ") {
                        },
                        whiteText {
                            append(text("Diamond Trophy Fish") { withStyle(ChatFormatting.AQUA) })
                            append(" in the ")
                            append(text("Crimson Isle") { withStyle(ChatFormatting.RED) })
                            append(" by ")
                            append(text("2-20%") { withStyle(ChatFormatting.GREEN) })
                            append(".")
                        },
                    )
                }

                val seaCreatureKills = profile.miscFishData.seaCreatureKills
                display(
                    Displays.text(
                        text("Dolphin Pet: ") {
                            val dolphin = DolphinBrackets.getByKills(seaCreatureKills)

                            if (dolphin == null) {
                                append(text("None") { withStyle(ChatFormatting.RED) })
                                return@text
                            }

                            append(
                                text(dolphin.rarity.displayname) {
                                    withColor(dolphin.rarity.color)
                                },
                            )
                        },
                        shadow = false,
                    ).withTooltip(
                        whiteText("Sea Creatures Killed: ") {
                            append(
                                text(DecimalFormat.getIntegerInstance().format(seaCreatureKills)) {
                                    withStyle(ChatFormatting.AQUA)
                                },
                            )
                        },
                        "",
                        DolphinBrackets.entries.map {
                            whiteText {
                                val hasObtained = it.killsRequired <= seaCreatureKills
                                if (!hasObtained) {
                                    withStyle(ChatFormatting.STRIKETHROUGH)
                                    withStyle(ChatFormatting.DARK_GRAY)
                                }
                                append(
                                    text(DecimalFormat.getIntegerInstance().format(it.killsRequired)) {
                                        withStyle(if (hasObtained) ChatFormatting.AQUA else ChatFormatting.RED)
                                    },
                                )
                                append(" for a" + (if (it.rarity == SkyBlockRarity.UNCOMMON || it.rarity == SkyBlockRarity.EPIC) "n" else "") + " ")
                                append(
                                    text("${it.rarity.displayname} Dolphin") {
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
    }

    private fun getTrophyTable(profile: SkyBlockProfile): LayoutElement {
        return TrophyFishTypes.entries.map { type ->
            getTrophyTableColumn(type, profile)
        }.swapAxis().asTable(4).centerIn(uiWidth, -1).asWidget()
    }

    private fun getCaughtInformation(fishies: List<TrophyFish>, profile: SkyBlockProfile): Map<TrophyFishTiers, Int> {
        return fishies.associate {
            it.tier to profile.trophyFish.obtainedTypes.getOrDefault(it.apiName, 0)
        }
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
}

