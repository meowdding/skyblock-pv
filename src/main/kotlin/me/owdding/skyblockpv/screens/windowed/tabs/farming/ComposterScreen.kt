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
import me.owdding.skyblockpv.screens.windowed.tabs.foraging.AttributeScreen
import me.owdding.skyblockpv.utils.LayoutUtils.asScrollable
import me.owdding.skyblockpv.utils.LayoutUtils.fitsIn
import me.owdding.skyblockpv.utils.components.PvLayouts
import me.owdding.skyblockpv.utils.components.PvWidgets
import me.owdding.skyblockpv.utils.displays.ExtraDisplays
import me.owdding.skyblockpv.utils.theme.PvColors
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.network.chat.MutableComponent
import net.minecraft.world.item.Items
import org.joml.Vector2i
import tech.thatgravyboat.skyblockapi.api.data.SkyBlockRarity
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId
import tech.thatgravyboat.skyblockapi.api.repo.apis.SkyBlockAttributesRepo
import tech.thatgravyboat.skyblockapi.api.repo.apis.SkyBlockItemsRepo
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.text.CommonText
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.Text.wrap
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.bold
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import java.time.Instant

class ComposterScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseFarmingScreen(gameProfile, profile) {
    override fun getLayout(bg: DisplayWidget): Layout {
        val baseLayout = PvLayouts.horizontal {
            widget(getPlots())
            widget(
                PvLayouts.vertical {
                    widget(getInformation())
                    spacer(0, 10)
                    widget(getGreenhouse())
                    spacer(0, 10)
                    widget(getUpgrades())
                },
            )
        }

        if (baseLayout.fitsIn(bg)) return baseLayout

        return PvLayouts.vertical(spacing = 5, alignment = 0.5f) {
            widget(getPlots())
            widget(getUpgrades())
            widget(getGreenhouse())
            widget(getInformation())
        }.asScrollable(bg.width - 10, bg.height)
    }

    private fun getInformation() = PvWidgets.label(
        "Information",
        PvLayouts.vertical {
            string("Organic Matter Stored: ") {
                append(loadingComponent { Text.of(it.composterData.organicMatter.toFormattedString(), PvColors.GREEN) })
            }
            string("Fuel Stored: ") {
                append(loadingComponent { Text.of(it.composterData.fuel.toFormattedString(), PvColors.DARK_GREEN) })
            }
            string("Compost Stored: ") {
                append(loadingComponent { Text.of(it.composterData.compostItems.toFormattedString(), PvColors.RED) })
            }
            string("Last Update: ") {
                append(loadingComponent { Text.of(Instant.ofEpochMilli(it.composterData.lastUpdateTimestamp).toReadableString(), PvColors.DARK_GRAY) })
            }
        },
    )

    private fun getGreenhouse() = PvWidgets.label(
        "Greenhouse",
        PvLayouts.vertical {
            string("Spaces: ") {
                append(loadingComponent { Text.of((it.greenhouseSlots.size + 12).toString(), PvColors.GREEN) })
                append("/", PvColors.DARK_GREEN)
                append("100", PvColors.GREEN)
            }

            fun createEntry(key: GreenhouseUpgrade) = display(
                loaded(
                    Displays.text(loadingMessage),
                    Displays.text(errorMessage),
                ) {
                    val level = it.greenhouseUpgrades[key] ?: 0
                    val data = StaticGardenData.greenhouseUpgrades[key]!!
                    Displays.text(
                        Text.of {
                            this.color = PvColors.GRAY
                            append(data.name.stripped)
                            append(": ")
                            append(level.toString(), PvColors.GREEN)
                            append("/", PvColors.DARK_GREEN)
                            append(data.upgrade.size.toString(), PvColors.GREEN)
                        },
                        shadow = false,
                    ).withTooltip {
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
            val upgrades = buildList {
                StaticGardenData.composterData.map { (upgrade, data) ->
                    add(Displays.padding(2, getComposterUpgrade(upgrade, data)))
                }
                add(Displays.padding(2, getComposterUpgrade(ComposterUpgrade.COMPOST_SPEED_ATTRIBUTE, null)))
            }

            display(
                ExtraDisplays.inventoryBackground(
                    upgrades.size, Orientation.HORIZONTAL,
                    Displays.padding(2, upgrades.toRow()),
                ),
            )
        },
    )

    fun getComposterUpgrade(upgrade: ComposterUpgrade, data: StaticComposterData?): Display {
        return loaded(
            onError = Displays.item(Items.BEDROCK).withTooltip { add("Error", PvColors.RED) },
            whileLoading = Displays.item(Items.DYE.orange()).withTooltip { add("Loading...", PvColors.GOLD) },
        ) { createComposterUpgradeDisplay(it, upgrade, data) }
    }

    fun createComposterUpgradeDisplay(gardenProfile: GardenProfile, upgrade: ComposterUpgrade, data: StaticComposterData?): Display {
        val level = gardenProfile.composterData.upgrades[upgrade] ?: 0

        if (upgrade == ComposterUpgrade.COMPOST_SPEED_ATTRIBUTE) {
            val id = "compost_speed"
            val item = SkyBlockId.attribute(id).toItem()
            return Displays.item(item, customStackText = level.toString()).withTooltip {
                AttributeScreen.getAttributeTooltip(
                    SkyBlockRarity.UNCOMMON,
                    SkyBlockAttributesRepo.get(id),
                    profile.attributeData.data.find { it.id == id },
                    item,
                )
            }
        }

        if (data == null) {
            return Displays.item(Items.BARRIER).withTooltip {
                add("Error", PvColors.RED)
                add("Data for $upgrade was null which shouldn't have happened")
            }
        }

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
                append(level.toString(), PvColors.GREEN)
                append("/", PvColors.DARK_GREEN)
                append(data.upgrade.size.toString(), PvColors.GREEN)
            }
            space()

            val map = getProgressMap(level, data.upgrade)
            val rareCrops = map.filter { (key, _) -> StaticGardenData.RARE_CROPS.contains(key) }
            val copper = map["copper"] ?: Text.of(":(")
            val remaining = map.filterNot { rareCrops.containsKey(it.key) || it.key.equals(StaticGardenData.COPPER, ignoreCase = true) }
            add("Copper needed", PvColors.GRAY)
            add(copper)
            space()
            add("Crops needed", PvColors.GRAY)
            remaining.values.forEach(::add)
            space()
            add("Rare Crops needed", PvColors.GRAY)
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
                    append("Copper", PvColors.RED)
                } else {
                    append(SkyBlockItemsRepo.getLazyItemStack(key)?.getDisplayName() ?: Text.of("Unknown Item"))
                }
                append(CommonText.SPACE)
                append(used.toFormattedString(), PvColors.YELLOW)
                append("/", PvColors.GOLD)
                append(neededMax.toFormattedString(), PvColors.YELLOW)
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
                map[it.location] = Displays.tooltip(Displays.item(Items.STAINED_GLASS_PANE.black()), it.getName())
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
                        map[it.location] = Displays.item(Items.STAINED_GLASS_PANE.green()).withTooltip(it.getName().also { it.color = PvColors.GREEN })
                    }
                    val unlockedAmount = data.unlockedPlots.groupBy { it.type }.mapValues { it.value.size }
                    staticPlots.forEach {
                        val plots = unlockedAmount[it.type] ?: 0
                        val cost = StaticGardenData.plotCost[it.type] ?: emptyList()

                        if (plots >= cost.size) {
                            return@forEach
                        }

                        val plotCost = cost[plots]
                        map[it.location] = Displays.item(Items.STAINED_GLASS_PANE.black()).withTooltip(
                            it.getName(),
                            plotCost.getDisplay().copy().apply { append(" x${plotCost.amount}", PvColors.DARK_GRAY) },
                        )
                    }
                },
                loadingValue = {
                    fillMap(
                        Displays.tooltip(
                            Displays.item(Items.STAINED_GLASS_PANE.orange()),
                            Text.of("Loading...", PvColors.GOLD),
                        ),
                    )
                },
                errorValue = {
                    fillMap(
                        Displays.tooltip(
                            Displays.item(Items.BEDROCK),
                            Text.of("Error!", PvColors.RED),
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
