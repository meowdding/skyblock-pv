package tech.thatgravyboat.skyblockpv.screens.tabs.farming

import com.mojang.authlib.GameProfile
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import org.joml.Vector2i
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.Text.wrap
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.bold
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockpv.SkyBlockPv
import tech.thatgravyboat.skyblockpv.api.ItemAPI
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.api.predicates.ItemPredicateHelper
import tech.thatgravyboat.skyblockpv.api.predicates.ItemPredicates
import tech.thatgravyboat.skyblockpv.data.*
import tech.thatgravyboat.skyblockpv.utils.LayoutBuild
import tech.thatgravyboat.skyblockpv.utils.Utils.append
import tech.thatgravyboat.skyblockpv.utils.Utils.rightPad
import tech.thatgravyboat.skyblockpv.utils.Utils.round
import tech.thatgravyboat.skyblockpv.utils.Utils.shorten
import tech.thatgravyboat.skyblockpv.utils.components.PvWidgets
import tech.thatgravyboat.skyblockpv.utils.displays.*

class FarmingScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseFarmingScreen(gameProfile, profile) {
    override fun getLayout(): Layout {
        val profile = profile ?: return LayoutBuild.frame {}

        return LayoutBuild.frame {
            horizontal {
                widget(getPlots())
                widget(getGear(profile))
                widget(getCrops(profile.farmingData))
                widget(getInfoWidget(profile))
            }
        }
    }

    private fun getInfoWidget(profile: SkyBlockProfile) = PvWidgets.label(
        "Information",
        LayoutBuild.vertical {
            val garden = gardenProfile?.getOrNull()

            string("Copper: ") {
                this.color = TextColor.DARK_GRAY
                append(profile.gardenData.copper.toFormattedString()) {
                    this.color = TextColor.RED
                }
            }

            display(
                Displays.text(
                    Text.of("Garden Level: ") {
                        this.color = TextColor.DARK_GRAY
                        append(loadingComponent(Text.of((garden?.getGardenLevel() ?: 0).toString()) { this.color = TextColor.DARK_GREEN }))
                    },
                    shadow = false,
                ).let {
                    garden?.getGardenLevel() ?: return@let it

                    val gardenLevel = garden.getGardenLevel()
                    val maxLevel = StaticGardenData.miscData.gardenLevelBrackets.size


                    it.withTooltip {
                        val totalRequired = StaticGardenData.miscData.gardenLevelBrackets.last()
                        if (gardenLevel != maxLevel) {
                            add("To lvl $gardenLevel: ") {
                                val xpRequired = StaticGardenData.miscData.getXpRequired(gardenLevel).toLong()
                                val xp = garden.gardenExperience - StaticGardenData.miscData.gardenLevelBrackets[(gardenLevel - 1).coerceAtLeast(0)]

                                this.color = TextColor.GRAY
                                append("$xp") { this.color = TextColor.DARK_GREEN }
                                append("/") { this.color = TextColor.GREEN }
                                append("$xpRequired") { this.color = TextColor.DARK_GREEN }
                                if (xpRequired != 0L) {
                                    append(" (")
                                    append(((xp.toFloat() / xpRequired) * 100).round()) {
                                        this.color = TextColor.DARK_AQUA
                                        append("%")
                                    }
                                    append(")")
                                }
                            }

                            add("To max: ") {
                                this.color = TextColor.GRAY

                                append(garden.gardenExperience.toFormattedString()) { this.color = TextColor.DARK_GREEN }
                                append("/") { this.color = TextColor.GREEN }
                                append(totalRequired.toFormattedString()) { this.color = TextColor.DARK_GREEN }
                                append(" (")
                                append(((garden.gardenExperience.toFloat() / totalRequired) * 100).round()) {
                                    this.color = TextColor.DARK_AQUA
                                    append("%")
                                }
                                append(")")
                            }
                        } else {
                            add("Total xp: ") {
                                this.color = TextColor.GRAY
                                append((garden.gardenExperience - totalRequired).toFormattedString()) { this.color = TextColor.DARK_GREEN }
                            }
                        }
                    }
                },
            )
            string("Contests Participated: ") {
                this.color = TextColor.DARK_GRAY
                append("${profile.farmingData.contest.size}") {
                    this.color = TextColor.YELLOW
                }
            }
            string("Medals: ") {
                this.color = TextColor.DARK_GRAY
                Text.join(
                    MedalType.actualMedals.map {
                        val amount = profile.farmingData.medalInventory[it] ?: 0
                        Text.of(amount.toFormattedString()) { this.color = it.color }
                    },
                    separator = Text.of("/") { this.color = TextColor.DARK_GRAY },
                ).let { append(it) }
            }

            string("Larva Consumed: ") {
                this.color = TextColor.DARK_GRAY
                append("${profile.gardenData.larvaConsumed}") {
                    this.color = TextColor.GREEN.takeIf { profile.gardenData.larvaConsumed == StaticGardenData.miscData.maxLarvaConsumed } ?: TextColor.RED
                }
                append("/${StaticGardenData.miscData.maxLarvaConsumed}")
            }
            fun addLevelingPerk(perkLevel: Int, name: String, values: List<Map<String, Int>>) {
                display(
                    Displays.text(
                        Text.of("$name: ") {
                            this.color = TextColor.DARK_GRAY
                            append("$perkLevel") {
                                this.color = TextColor.GREEN.takeIf { perkLevel == values.size } ?: TextColor.RED
                            }
                            append("/${values.size}")
                        },
                        shadow = false,
                    ).let {
                        if (perkLevel == values.size) {
                            return@let it
                        }

                        it.withTooltip {
                            val neededForMax = values.last()
                            val paid = if (perkLevel > 1) values[perkLevel - 1] else emptyMap()
                            neededForMax.forEach {
                                add(
                                    when (it.key) {
                                        "gold_medal" -> "Gold Medal"
                                        else -> ItemAPI.getItemName(it.key).stripped
                                    },
                                ) {
                                    this.color = TextColor.GRAY
                                    val paidAmount = paid[it.key] ?: 0
                                    append(": ")
                                    append(paidAmount.toFormattedString()) {
                                        this.color = TextColor.DARK_GREEN
                                    }
                                    append("/") {
                                        this.color = TextColor.GREEN
                                    }
                                    append(it.value.toFormattedString()) {
                                        this.color = TextColor.DARK_GREEN
                                    }

                                    append(
                                        Text.of(((paidAmount / it.value.toFloat()).times(100).round())) {
                                            append("%")
                                            this.color = TextColor.DARK_AQUA
                                        }.wrap(" (", ")"),
                                    )
                                }
                            }
                        }
                    },
                )
            }

            addLevelingPerk(profile.farmingData.perks.farmingLevelCap, "Farming Level Cap", StaticGardenData.miscData.farmingLevelCap)
            addLevelingPerk(profile.farmingData.perks.doubleDrops, "Double Drops", StaticGardenData.miscData.bonusDrops)
        },
        padding = 20,
    )

    private fun getPets(profile: SkyBlockProfile) = profile.pets.asSequence()
        .filter { FarmingGear.pets.contains(it.type) }
        .sortedWith(Comparator.comparingInt<Pet> { it.rarity.ordinal }.thenByDescending { pet -> pet.exp })
        .distinctBy { it.type }
        .mapTo(mutableListOf()) { Displays.item(it.itemStack, showTooltip = true, customStackText = it.level) }
        .rightPad(4, Displays.background(SkyBlockPv.id("icon/slot/bone"), Displays.empty(16, 16)))
        .map { Displays.padding(2, it) }
        .toColumn()
        .let { Displays.background(SkyBlockPv.id("inventory/inventory-1x4"), Displays.padding(2, it)) }

    private fun getGear(profile: SkyBlockProfile) = PvWidgets.label(
        "Gear",
        LayoutBuild.horizontal {
            widget(
                PvWidgets.armorAndEquipment(
                    profile,
                    ::calculateEquipmentScore,
                    FarmingGear.necklaces,
                    FarmingGear.cloaks,
                    FarmingGear.belts,
                    FarmingGear.gloves,
                    FarmingGear.armor,
                ),
            )
            spacer(width = 5)
            display(getPets(profile))
            spacer(width = 5)
            widget(Displays.background(SkyBlockPv.id("inventory/inventory-1x1"), getVacuum(profile)).asWidget()) { alignVerticallyMiddle() }
        },
        padding = 20,
    )

    private fun getVacuum(profile: SkyBlockProfile): Display {
        val vacuums = ItemPredicateHelper.getItemsMatching(profile, ItemPredicates.AnySkyblockID(FarmingGear.vacuum)) ?: emptyList()

        val vacuum = vacuums.sortedBy { it.getData(DataTypes.RARITY)?.ordinal ?: 0 }.reversed().firstOrNull()
            ?: return Displays.padding(4, Displays.background(SkyBlockPv.id("icon/slot/minecart"), Displays.empty(16, 16)))

        return Displays.padding(4, Displays.item(vacuum, showTooltip = true))
    }

    private fun calculateEquipmentScore(stack: ItemStack): Int {
        var score = 0

        score += stack.getData(DataTypes.RARITY)?.ordinal ?: 0

        score += stack.getData(DataTypes.ENCHANTMENTS)?.map { it.value }?.sum() ?: 0

        score += stack.getData(DataTypes.RARITY_UPGRADES) ?: 0

        score += stack.getData(DataTypes.MODIFIER)?.let { 1 } ?: 0

        return score
    }

    fun getPlots() = PvWidgets.label(
        "Plots",
        LayoutBuild.frame {
            val data = gardenProfile?.getOrNull()
            val map = MutableList(5) { MutableList(5) { Displays.empty() } }

            StaticGardenData.plots.forEach {
                map[it.location] = Displays.tooltip(Displays.item(Items.BLACK_STAINED_GLASS_PANE.defaultInstance), it.getName())
            }

            fun fillMap(value: Display) {
                map.forEach {
                    it.fill(value)
                }
            }

            loading(
                {
                    val data = data ?: return@loading
                    val staticPlots = StaticGardenData.plots.toMutableList().apply { removeAll(data.unlockedPlots) }
                    data.unlockedPlots.forEach {
                        map[it.location] = Displays.tooltip(
                            Displays.item(
                                Items.GREEN_STAINED_GLASS_PANE.defaultInstance,
                            ),
                            it.getName().also { it.color = TextColor.GREEN },
                        )
                    }
                    val unlockedAmount = data.unlockedPlots.groupBy { it.type }.mapValues { it.value.size }
                    staticPlots.forEach {
                        val plots = unlockedAmount[it.type] ?: 0
                        val cost = StaticGardenData.plotCost[it.type] ?: emptyList()

                        if (plots >= cost.size) {
                            return@forEach
                        }

                        val plotCost = cost[plots]
                        map[it.location] = Displays.item(Items.BLACK_STAINED_GLASS_PANE.defaultInstance).withTooltip(
                            it.getName(),
                            plotCost.getDisplay().copy().apply { append(Text.of(" x${plotCost.amount}") { color = TextColor.DARK_GRAY }) },
                        )
                    }
                },
                {
                    fillMap(
                        Displays.tooltip(
                            Displays.item(Items.ORANGE_STAINED_GLASS_PANE.defaultInstance),
                            Text.of("Loading...") { this.color = TextColor.GOLD },
                        ),
                    )
                },
                {
                    fillMap(
                        Displays.tooltip(
                            Displays.item(Items.BEDROCK.defaultInstance),
                            Text.of("Error!") { this.color = TextColor.RED },
                        ),
                    )
                },
            ).invoke()

            map[2][2] = Displays.tooltip(
                Displays.item(
                    loading<ItemStack>(
                        data?.selectedBarnSkin?.getItem() ?: ItemStack.EMPTY,
                        Items.BARRIER.defaultInstance,
                        Items.BEDROCK.defaultInstance,
                    ),
                ),
                loadingComponent(data?.selectedBarnSkin?.displayName ?: Component.empty()),
            )

            val plots = map.map { it.reversed().map { Displays.padding(2, it) }.toColumn() }.toRow()
            display(
                Displays.background(
                    SkyBlockPv.id("inventory/inventory-5x5"),
                    Displays.padding(2, plots),
                ),
            )
        },
        padding = 20,
    )

    private fun getCrops(farmingData: FarmingData) = PvWidgets.label(
        "Contests",
        GardenResource.actualValues.map { entry ->
            Displays.item(entry.getItem()).withTooltip {
                add(entry.getItem().hoverName.copy().stripped) {
                    this.bold = true
                    this.color = TextColor.WHITE
                }

                val contests = farmingData.contest.filter { it.isOfType(entry) }
                space()
                add("Brackets: ") {
                    this.color = TextColor.GRAY

                    fun getDot(unlocked: Boolean) = if (unlocked) "●" else "◌"

                    MedalType.entries.forEach {
                        append(getDot(farmingData.uniqueBrackets[it]?.contains(entry) == true)) {
                            this.color = it.color
                        }
                    }

                    val medals = contests.groupByTo(mutableMapOf()) { it.claimedMedal }
                        .apply { remove(null) }
                        .mapKeys { MedalType.valueOf(it.key?.uppercase() ?: "") }
                        .toSortedMap(MedalType::compareTo)

                    if (medals.isNotEmpty()) {
                        medals.map { (key, value) ->
                            Text.of(value.size.toFormattedString()) {
                                this.color = key.color
                            }
                        }.let {
                            Text.join(it, separator = Text.of("/") { this.color = TextColor.DARK_GRAY })
                        }
                            .wrap(" (", ")")
                            .let { append(it) }
                    }
                }

                add("Personal Best: ") {
                    this.color = TextColor.GRAY
                    if (!farmingData.perks.personalBests) {
                        append("Not Unlocked!") {
                            this.color = TextColor.RED
                        }
                        return@add
                    }

                    val maxPersonalBest = StaticGardenData.miscData.personalBests[entry] ?: 0
                    val personalBest = farmingData.personalBests[entry] ?: 0
                    append(personalBest.toFormattedString()) {
                        this.color = TextColor.YELLOW
                    }
                    if (maxPersonalBest >= personalBest) {
                        append("/") { this.color = TextColor.GOLD }
                        append(maxPersonalBest.shorten(0)) { this.color = TextColor.YELLOW }

                        append(
                            Text.of("${((personalBest / maxPersonalBest.toFloat()) * 100).round()}%") {
                                this.color = TextColor.DARK_AQUA
                            }.wrap(" (", ")"),
                        )
                    }
                }

                add("Contests participated: ") {
                    this.color = TextColor.GRAY
                    append(contests.count().toFormattedString()) {
                        this.color = TextColor.YELLOW
                    }
                }
                contests.mapNotNull { it.position?.plus(1) }.minByOrNull { it }?.let { rank ->
                    add("Highest Position: ") {
                        this.color = TextColor.GRAY
                        append("#") {
                            this.color = TextColor.YELLOW
                            append(rank.toFormattedString())
                        }
                    }
                }
            }
        }.map { Displays.padding(2, it) }.chunked(5)
            .map { it.toColumn() }
            .toRow().let { Displays.background(SkyBlockPv.id("inventory/inventory-2x5"), Displays.padding(2, it)) }.asWidget(),
        padding = 20,
    )
}

private operator fun <E> MutableList<MutableList<E>>.set(location: Vector2i, value: E) {
    this[location.x][location.y] = value
}
