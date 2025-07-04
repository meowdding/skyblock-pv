package me.owdding.skyblockpv.screens.tabs.farming

import com.mojang.authlib.GameProfile
import earth.terrarium.olympus.client.utils.Orientation
import me.owdding.lib.displays.*
import me.owdding.lib.extensions.rightPad
import me.owdding.lib.extensions.round
import me.owdding.lib.extensions.shorten
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.api.data.SkyBlockProfile
import me.owdding.skyblockpv.api.predicates.ItemPredicateHelper
import me.owdding.skyblockpv.api.predicates.ItemPredicates
import me.owdding.skyblockpv.data.api.skills.Pet
import me.owdding.skyblockpv.data.api.skills.farming.FarmingData
import me.owdding.skyblockpv.data.api.skills.farming.MedalType
import me.owdding.skyblockpv.data.repo.FarmingGear
import me.owdding.skyblockpv.data.repo.GardenResource
import me.owdding.skyblockpv.data.repo.StaticGardenData
import me.owdding.skyblockpv.utils.LayoutUtils.asScrollable
import me.owdding.skyblockpv.utils.LayoutUtils.fitsIn
import me.owdding.skyblockpv.utils.Utils.append
import me.owdding.skyblockpv.utils.components.PvLayouts
import me.owdding.skyblockpv.utils.components.PvWidgets
import me.owdding.skyblockpv.utils.displays.ExtraDisplays
import me.owdding.skyblockpv.utils.theme.PvColors
import me.owdding.skyblockpv.utils.theme.ThemeSupport
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import tech.thatgravyboat.skyblockapi.api.remote.RepoItemsAPI
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.Text.wrap
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.bold
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

class FarmingScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseFarmingScreen(gameProfile, profile) {
    override fun getLayout(bg: DisplayWidget): Layout {
        val baseLayout = PvLayouts.frame {
            horizontal {
                widget(getGear(profile))
                widget(getContests(profile.farmingData))
                widget(getInfoWidget(profile))
            }
        }

        if (baseLayout.fitsIn(bg)) {
            return baseLayout
        }

        return PvLayouts.vertical(5, alignment = 0.5f) {
            spacer()
            horizontal(5, alignment = 0.5f) {
                widget(getGear(profile))
                widget(getContests(profile.farmingData))
            }
            widget(getInfoWidget(profile))
        }.asScrollable(bg.width - 10, bg.height)
    }

    private fun getInfoWidget(profile: SkyBlockProfile) = PvWidgets.label(
        "Information",
        PvLayouts.vertical {
            val garden = data?.getOrNull()

            string("Copper: ") {
                this.color = PvColors.DARK_GRAY
                append(profile.gardenData.copper.toFormattedString()) {
                    this.color = PvColors.RED
                }
            }

            display(
                ExtraDisplays.text(
                    Text.of("Garden Level: ") {
                        this.color = PvColors.DARK_GRAY
                        append(loadingComponent { Text.of(it.getGardenLevel().toString()) { this.color = PvColors.DARK_GREEN } })
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

                                this.color = PvColors.GRAY
                                append("$xp") { this.color = PvColors.DARK_GREEN }
                                append("/") { this.color = PvColors.GREEN }
                                append("$xpRequired") { this.color = PvColors.DARK_GREEN }
                                if (xpRequired != 0L) {
                                    append(" (")
                                    append(((xp.toFloat() / xpRequired) * 100).round()) {
                                        this.color = PvColors.DARK_AQUA
                                        append("%")
                                    }
                                    append(")")
                                }
                            }

                            add("To max: ") {
                                this.color = PvColors.GRAY

                                append(garden.gardenExperience.toFormattedString()) { this.color = PvColors.DARK_GREEN }
                                append("/") { this.color = PvColors.GREEN }
                                append(totalRequired.toFormattedString()) { this.color = PvColors.DARK_GREEN }
                                append(" (")
                                append(((garden.gardenExperience.toFloat() / totalRequired) * 100).round()) {
                                    this.color = PvColors.DARK_AQUA
                                    append("%")
                                }
                                append(")")
                            }
                        } else {
                            add("Total xp: ") {
                                this.color = PvColors.GRAY
                                append((garden.gardenExperience - totalRequired).toFormattedString()) { this.color = PvColors.DARK_GREEN }
                            }
                        }
                    }
                },
            )
            string("Contests Participated: ") {
                this.color = PvColors.DARK_GRAY
                append("${profile.farmingData.contest.size}") {
                    this.color = PvColors.YELLOW
                }
            }
            string("Medals: ") {
                this.color = PvColors.DARK_GRAY
                Text.join(
                    MedalType.actualMedals.map {
                        val amount = profile.farmingData.medalInventory[it] ?: 0
                        Text.of(amount.toFormattedString()) { this.color = it.color() }
                    },
                    separator = Text.of("/") { this.color = PvColors.DARK_GRAY },
                ).let { append(it) }
            }

            string("Larva Consumed: ") {
                this.color = PvColors.DARK_GRAY
                append("${profile.gardenData.larvaConsumed}") {
                    this.color = PvColors.GREEN.takeIf { profile.gardenData.larvaConsumed == StaticGardenData.miscData.maxLarvaConsumed } ?: PvColors.RED
                }
                append("/${StaticGardenData.miscData.maxLarvaConsumed}")
            }
            fun addLevelingPerk(perkLevel: Int, name: String, values: List<Map<String, Int>>) {
                display(
                    ExtraDisplays.text(
                        Text.of("$name: ") {
                            this.color = PvColors.DARK_GRAY
                            append("$perkLevel") {
                                this.color = PvColors.GREEN.takeIf { perkLevel == values.size } ?: PvColors.RED
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
                                        else -> RepoItemsAPI.getItemName(it.key).stripped
                                    },
                                ) {
                                    this.color = PvColors.GRAY
                                    val paidAmount = paid[it.key] ?: 0
                                    append(": ")
                                    append(paidAmount.toFormattedString()) {
                                        this.color = PvColors.DARK_GREEN
                                    }
                                    append("/") {
                                        this.color = PvColors.GREEN
                                    }
                                    append(it.value.toFormattedString()) {
                                        this.color = PvColors.DARK_GREEN
                                    }

                                    append(
                                        Text.of(((paidAmount / it.value.toFloat()).times(100).round())) {
                                            append("%")
                                            this.color = PvColors.DARK_AQUA
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
        icon = SkyBlockPv.id("icon/item/clipboard"),
    )

    private fun getPets(profile: SkyBlockProfile) = profile.pets.asSequence()
        .filter { FarmingGear.pets.contains(it.type) }
        .sortedWith(Comparator.comparingInt<Pet> { it.rarity.ordinal }.thenByDescending { pet -> pet.exp })
        .distinctBy { it.type }
        .mapTo(mutableListOf()) { Displays.item(it.itemStack, showTooltip = true, customStackText = it.level) }
        .rightPad(4, Displays.background(ThemeSupport.texture(SkyBlockPv.id("icon/slot/bone")), Displays.empty(16, 16)))
        .map { Displays.padding(2, it) }
        .toColumn()
        .let { ExtraDisplays.inventoryBackground(4, Orientation.VERTICAL, Displays.padding(2, it)) }

    private fun getGear(profile: SkyBlockProfile) = PvWidgets.label(
        "Gear",
        PvLayouts.horizontal {
            PvWidgets.armorAndEquipment(
                profile,
                ::calculateEquipmentScore,
                FarmingGear.necklaces,
                FarmingGear.cloaks,
                FarmingGear.belts,
                FarmingGear.gloves,
                FarmingGear.armor,
            ).let { widget(it) }
            spacer(width = 5)
            display(getPets(profile))
            spacer(width = 5)
            widget(ExtraDisplays.inventorySlot(getVacuum(profile)).asWidget()) { alignVerticallyMiddle() }
        },
    )

    private fun getVacuum(profile: SkyBlockProfile): Display {
        val vacuums = ItemPredicateHelper.getItemsMatching(profile, ItemPredicates.AnySkyblockID(FarmingGear.vacuum)) ?: emptyList()

        val vacuum = vacuums.sortedBy { it.getData(DataTypes.RARITY)?.ordinal ?: 0 }.reversed().firstOrNull()
            ?: return Displays.padding(4, Displays.background(ThemeSupport.texture(SkyBlockPv.id("icon/slot/minecart")), Displays.empty(16, 16)))

        return Displays.padding(4, Displays.item(vacuum, showTooltip = true))
    }

    private fun calculateEquipmentScore(stack: ItemStack): Int {
        var score = 0

        score += stack.getData(DataTypes.RARITY)?.ordinal ?: 0

        score += stack.getData(DataTypes.ENCHANTMENTS)?.map { it.value }?.sum() ?: 0

        score += 1.takeIf { stack.getData(DataTypes.RECOMBOBULATOR) ?: false } ?: 0

        score += stack.getData(DataTypes.MODIFIER)?.let { 1 } ?: 0

        return score
    }

    private fun getContests(farmingData: FarmingData) = PvWidgets.label(
        "Contests",
        GardenResource.actualValues.map { entry ->
            Displays.item(entry.getItem()).withTooltip {
                add(entry.getItem().hoverName.copy().stripped) {
                    this.bold = true
                    this.color = PvColors.WHITE
                }

                val contests = farmingData.contest.filter { it.isOfType(entry) }
                space()
                add("Brackets: ") {
                    this.color = PvColors.GRAY

                    fun getDot(unlocked: Boolean) = if (unlocked) "●" else "◌"

                    MedalType.entries.forEach {
                        append(getDot(farmingData.uniqueBrackets[it]?.contains(entry) == true)) {
                            this.color = it.color()
                        }
                    }

                    val medals = contests.groupByTo(mutableMapOf()) { it.claimedMedal }
                        .apply { remove(null) }
                        .mapKeys { MedalType.valueOf(it.key?.uppercase() ?: "") }
                        .toSortedMap(MedalType::compareTo)

                    if (medals.isNotEmpty()) {
                        medals.map { (key, value) ->
                            Text.of(value.size.toFormattedString()) {
                                this.color = key.color()
                            }
                        }.let {
                            Text.join(it, separator = Text.of("/") { this.color = PvColors.DARK_GRAY })
                        }
                            .wrap(" (", ")")
                            .let { append(it) }
                    }
                }

                add("Personal Best: ") {
                    this.color = PvColors.GRAY
                    if (!farmingData.perks.personalBests) {
                        append("Not Unlocked!") {
                            this.color = PvColors.RED
                        }
                        return@add
                    }

                    val maxPersonalBest = StaticGardenData.miscData.personalBests[entry] ?: 0
                    val personalBest = farmingData.personalBests[entry] ?: 0
                    append(personalBest.toFormattedString()) {
                        this.color = PvColors.YELLOW
                    }
                    if (maxPersonalBest >= personalBest) {
                        append("/") { this.color = PvColors.GOLD }
                        append(maxPersonalBest.shorten(0)) { this.color = PvColors.YELLOW }

                        append(
                            Text.of("${((personalBest / maxPersonalBest.toFloat()) * 100).round()}%") {
                                this.color = PvColors.DARK_AQUA
                            }.wrap(" (", ")"),
                        )
                    }
                }

                add("Contests participated: ") {
                    this.color = PvColors.GRAY
                    append(contests.count().toFormattedString()) {
                        this.color = PvColors.YELLOW
                    }
                }
                contests.mapNotNull { it.position?.plus(1) }.minByOrNull { it }?.let { rank ->
                    add("Highest Position: ") {
                        this.color = PvColors.GRAY
                        append("#") {
                            this.color = PvColors.YELLOW
                            append(rank.toFormattedString())
                        }
                    }
                }
            }
        }.map { Displays.padding(2, it) }.chunked(5)
            .map { it.toColumn() }
            .toRow().let { ExtraDisplays.inventoryBackground(2, 5, Displays.padding(2, it)) }.asWidget(),
    )
}

