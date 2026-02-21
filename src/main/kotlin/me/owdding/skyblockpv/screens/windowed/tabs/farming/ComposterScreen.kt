package me.owdding.skyblockpv.screens.windowed.tabs.farming

import com.mojang.authlib.GameProfile
import earth.terrarium.olympus.client.utils.Orientation
import me.owdding.lib.displays.Display
import me.owdding.lib.displays.DisplayWidget
import me.owdding.lib.displays.Displays
import me.owdding.lib.displays.asTable
import me.owdding.lib.displays.toRow
import me.owdding.lib.displays.withTooltip
import me.owdding.lib.extensions.round
import me.owdding.lib.extensions.toReadableString
import me.owdding.lib.extensions.transpose
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.data.api.skills.farming.ComposterUpgrade
import me.owdding.skyblockpv.data.api.skills.farming.GardenProfile
import me.owdding.skyblockpv.data.repo.GreenhouseUpgrade
import me.owdding.skyblockpv.data.repo.StaticComposterData
import me.owdding.skyblockpv.data.repo.StaticGardenData
import me.owdding.skyblockpv.utils.LayoutUtils.asScrollable
import me.owdding.skyblockpv.utils.LayoutUtils.fitsIn
import me.owdding.skyblockpv.utils.components.PvLayouts
import me.owdding.skyblockpv.utils.components.PvWidgets
import me.owdding.skyblockpv.utils.displays.ExtraDisplays
import me.owdding.skyblockpv.utils.theme.PvColors
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.world.item.Items
import org.joml.Vector2i
import tech.thatgravyboat.skyblockapi.api.remote.RepoItemsAPI
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.text.CommonText
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.Text.wrap
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.bold
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.hover
import java.time.Instant

class ComposterScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseFarmingScreen(gameProfile, profile) {
    override fun getLayout(bg: DisplayWidget): Layout {
        val baseLayout = PvLayouts.horizontal {
            widget(getPlots())
            widget(
                PvLayouts.vertical {
                    widget(getInformation())
                    spacer(0, 10)
                    widget(getUpgrades())
                },
            )
        }

        if (baseLayout.fitsIn(bg)) return baseLayout

        return PvLayouts.vertical(spacing = 5, alignment = 0.5f) {
            widget(getPlots())
            widget(getUpgrades())
            widget(getInformation())
        }.asScrollable(bg.width - 10, bg.height)
    }

    private fun getInformation() = PvWidgets.label(
        "Information",
        PvLayouts.vertical {
            string("Organic Matter Stored: ") {
                append(
                    loadingComponent {
                        Text.of(it.composterData.organicMatter.toFormattedString()) {
                            this.color = PvColors.GREEN
                        }
                    },
                )
            }
            string("Fuel Stored: ") {
                append(
                    loadingComponent {
                        Text.of(it.composterData.fuel.toFormattedString()) {
                            this.color = PvColors.DARK_GREEN
                        }
                    },
                )
            }
            string("Compost Stored: ") {
                append(
                    loadingComponent {
                        Text.of(it.composterData.compostItems.toFormattedString()) {
                            this.color = PvColors.RED
                        }
                    },
                )
            }
            string("Last Update: ") {
                append(
                    loadingComponent {
                        Text.of(Instant.ofEpochMilli(it.composterData.lastUpdateTimestamp).toReadableString()) {
                            this.color = PvColors.DARK_GRAY
                        }
                    },
                )
            }
            string("Greenhouse spaces: ") {
                append(
                    loadingComponent {
                        Text.of((it.greenhouseSlots.size + 12).toString())
                    },
                )
                append("/100")
            }

            fun createEntry(key: GreenhouseUpgrade) = display(
                loaded(
                    Displays.text(loadingMessage),
                    Displays.text(errorMessage),
                ) {
                    val level = it.greenhouseUpgrades[key] ?: 0
                    val data = StaticGardenData.greenhouseUpgrades[key]!!
                    Displays.text(Text.of {
                        append(data.name.stripped)
                        this.color = PvColors.GRAY
                        append(": ")
                        append(level)
                        append("/")
                        append(data.upgrade.size)
                    }).withTooltip {
                        add(data.name)
                        space()
                        add(data.getTooltipForLevel(level))
                        space()
                        val map = getProgressMap(level, data.upgrade)
                        map.values.forEach { entry ->
                            add(entry)
                        }
                    }
                },
            )

            GreenhouseUpgrade.entries.forEach(::createEntry)
        },
    )

    private fun getUpgrades() = PvWidgets.label(
        "Upgrades",
        PvLayouts.frame {
            display(
                ExtraDisplays.inventoryBackground(
                    5, Orientation.HORIZONTAL,
                    Displays.padding(
                        2,
                        StaticGardenData.composterData.map { (upgrade, data) ->
                            Displays.padding(2, getComposterUpgrade(upgrade, data))
                        }.toRow(),
                    ),
                ),
            )
        },
    )

    fun getComposterUpgrade(upgrade: ComposterUpgrade, data: StaticComposterData): Display {
        return loaded(
            onError = Displays.item(Items.BEDROCK).withTooltip { add("Error") { this.color = PvColors.RED } },
            whileLoading = Displays.item(Items.ORANGE_DYE).withTooltip { add("Loading...") { this.color = PvColors.GOLD } },
        ) { createDisplay(it, upgrade, data) }
    }

    fun createDisplay(gardenProfile: GardenProfile, upgrade: ComposterUpgrade, data: StaticComposterData): Display {
        val level = gardenProfile.composterData.upgrades[upgrade] ?: 0
        return Displays.item(data.item, customStackText = level).withTooltip {
            add(
                data.name.copy().apply {
                    this.bold = true
                },
            )
            add(data.getTooltipForLevel(level))
            space()
            add("Level ") {
                this.color = PvColors.GRAY
                append(level.toString()) { this.color = PvColors.GREEN }
                append("/") { this.color = PvColors.DARK_GREEN }
                append(data.upgrade.size.toString()) {
                    this.color = PvColors.GREEN
                }
            }
            space()

            val map = getProgressMap(level, data.upgrade)
            val rareCrops = map.filter { (key, _) -> StaticGardenData.RARE_CROPS.contains(key) }
            val copper = map["copper"] ?: Text.of(":(")
            val remaining = map.filterNot { rareCrops.containsKey(it.key) || it.key.equals(StaticGardenData.COPPER, ignoreCase = true) }
            add("Copper needed") {
                this.color = PvColors.GRAY
            }
            add(copper)
            space()
            add("Crops needed") {
                this.color = PvColors.GRAY
            }
            remaining.values.forEach(::add)
            space()
            add("Rare Crops needed") {
                this.color = PvColors.GRAY
            }
            rareCrops.values.forEach(::add)
        }
    }

    fun getProgressMap(level: Int, data: List<Map<String, Int>>): MutableMap<String, MutableComponent> {
        val current = if (level != 0) data[level - 1] else emptyMap()
        val neededForMax = data.last()

        val map = mutableMapOf<String, MutableComponent>()
        neededForMax.forEach { (key, neededMax) ->
            val used = current[key] ?: 0
            val percentage = used.toFloat() / neededMax

            Text.of("") {
                this.color = PvColors.GRAY
                if (key.equals(StaticGardenData.COPPER, ignoreCase = true)) {
                    append("Copper") {
                        this.color = PvColors.RED
                    }
                } else {
                    append(RepoItemsAPI.getItemName(key))
                }
                append(CommonText.SPACE)
                append(used.toFormattedString()) {
                    this.color = PvColors.YELLOW
                }
                append("/") { this.color = PvColors.GOLD }
                append(neededMax.toFormattedString()) {
                    this.color = PvColors.YELLOW
                }
                append(CommonText.SPACE)
                append(
                    Text.of(percentage.times(100).round()) {
                        this.color = PvColors.DARK_AQUA
                        append("%")
                    }.wrap("(", ")"),
                )
            }.let { map[key] = it }
        }
        return map
    }

    fun getPlots() = PvWidgets.label(
        "Plots",
        PvLayouts.frame {
            val map = MutableList(5) { MutableList(5) { Displays.empty() } }

            StaticGardenData.plots.forEach {
                map[it.location] = Displays.tooltip(Displays.item(Items.BLACK_STAINED_GLASS_PANE), it.getName())
            }

            fun fillMap(value: Display) {
                map.forEach {
                    it.fill(value)
                }
            }

            loading(
                onSuccess = { data ->
                    val staticPlots = StaticGardenData.plots.toMutableList().apply { removeAll(data.unlockedPlots) }
                    data.unlockedPlots.forEach {
                        map[it.location] = Displays.item(Items.GREEN_STAINED_GLASS_PANE).withTooltip(it.getName().also { it.color = PvColors.GREEN })
                    }
                    val unlockedAmount = data.unlockedPlots.groupBy { it.type }.mapValues { it.value.size }
                    staticPlots.forEach {
                        val plots = unlockedAmount[it.type] ?: 0
                        val cost = StaticGardenData.plotCost[it.type] ?: emptyList()

                        if (plots >= cost.size) {
                            return@forEach
                        }

                        val plotCost = cost[plots]
                        map[it.location] = Displays.item(Items.BLACK_STAINED_GLASS_PANE).withTooltip(
                            it.getName(),
                            plotCost.getDisplay().copy().apply { append(Text.of(" x${plotCost.amount}") { color = PvColors.DARK_GRAY }) },
                        )
                    }
                },
                loadingValue = {
                    fillMap(
                        Displays.tooltip(
                            Displays.item(Items.ORANGE_STAINED_GLASS_PANE),
                            Text.of("Loading...") { this.color = PvColors.GOLD },
                        ),
                    )
                },
                errorValue = {
                    fillMap(
                        Displays.tooltip(
                            Displays.item(Items.BEDROCK),
                            Text.of("Error!") { this.color = PvColors.RED },
                        ),
                    )
                },
            )

            map[2][2] = Displays.tooltip(
                Displays.item(
                    loaded(
                        whileLoading = Items.BARRIER.defaultInstance,
                        onError = Items.BEDROCK.defaultInstance,
                    ) { it.selectedBarnSkin },
                ),
                loadingComponent { it.selectedBarnSkin.hoverName },
            )

            val plots = map.map { it.reversed().map { Displays.padding(2, it) } }.transpose().asTable()



            display(
                ExtraDisplays.inventoryBackground(
                    5, 5,
                    Displays.padding(2, plots),
                ),
            )
        },
    )

    private operator fun <E> MutableList<MutableList<E>>.set(location: Vector2i, value: E) {
        this[location.x][location.y] = value
    }
}
