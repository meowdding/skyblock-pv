package tech.thatgravyboat.skyblockpv.screens.tabs.farming

import com.mojang.authlib.GameProfile
import earth.terrarium.olympus.client.utils.Orientation
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
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
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.api.predicates.ItemPredicateHelper
import tech.thatgravyboat.skyblockpv.api.predicates.ItemPredicates
import tech.thatgravyboat.skyblockpv.data.skills.farming.GardenResource
import tech.thatgravyboat.skyblockpv.data.skills.farming.StaticGardenData
import tech.thatgravyboat.skyblockpv.utils.LayoutBuild
import tech.thatgravyboat.skyblockpv.utils.Utils.append
import tech.thatgravyboat.skyblockpv.utils.Utils.round
import tech.thatgravyboat.skyblockpv.utils.Utils.shorten
import tech.thatgravyboat.skyblockpv.utils.displays.Display
import tech.thatgravyboat.skyblockpv.utils.displays.Displays
import tech.thatgravyboat.skyblockpv.utils.displays.toRow
import tech.thatgravyboat.skyblockpv.utils.displays.withTooltip

class CropScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseFarmingScreen(gameProfile, profile) {

    override fun getLayout(): Layout {
        val profile = profile ?: return LayoutBuild.frame {}

        val resourcesDisplay = GardenResource.actualValues.map {
            Displays.inventoryBackground(
                3, Orientation.VERTICAL,
                Displays.padding(
                    2,
                    Displays.column(
                        Displays.padding(2, getTool(it, profile)),
                        Displays.padding(2, getCropUpgrade(it)),
                        Displays.padding(2, getCropMilestone(it)),
                    ),
                ),
            )
        }

        return resourcesDisplay.toRow(2).let { LayoutBuild.frame { display(it) } }
    }

    private fun getCropUpgrade(it: GardenResource): Display {
        val gardenData = gardenProfile?.getOrNull()
        val cropLevel = gardenData?.cropUpgradeLevels?.get(it)?.toInt() ?: 0

        val cropUpgradeCost = StaticGardenData.miscData.cropUpgradeCost
        val maxUpgrades = cropUpgradeCost.size
        val totalCopper = cropUpgradeCost.sum()

        val icon = if (cropLevel == maxUpgrades) Items.NETHERRACK else Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE
        val color = if (cropLevel == maxUpgrades) TextColor.GREEN else TextColor.RED

        val cropLevelText = Text.of(cropLevel.toString()) { this.color = color }
        return loadingValue(
            Displays.item(icon, customStackText = cropLevelText).withTooltip {
                add("Crop Upgrade") { this.bold = true }
                add("Upgrades: ") {
                    this.color = TextColor.GRAY

                    append(cropLevelText)
                    append("/$maxUpgrades")
                }
                add("Copper paid: ") {
                    this.color = TextColor.GRAY

                    val copperUsed = cropUpgradeCost.take(cropLevel).sum()
                    append(copperUsed.toFormattedString()) { this.color = TextColor.RED }
                    append("/") { this.color = TextColor.GOLD }
                    append(totalCopper.toFormattedString()) { this.color = TextColor.RED }
                    if (totalCopper != copperUsed) {
                        append(
                            Text.of("${((copperUsed.toFloat() / totalCopper) * 100).round()}%") {
                                this.color = TextColor.YELLOW
                            }.wrap(" (", ")"),
                        )
                    }
                }
            },
            Displays.item(Items.ORANGE_DYE).withTooltip(Text.of("Loading...") { this.color = TextColor.LIGHT_PURPLE }),
            Displays.item(Items.BEDROCK).withTooltip(Text.of("Error!") { this.color = TextColor.RED }),
        )
    }

    private fun getCropMilestone(resource: GardenResource): Display {
        val gardenData = gardenProfile?.getOrNull()

        val resourcesCollected = gardenData?.resourcesCollected?.get(resource) ?: 0

        val milestoneBrackets = StaticGardenData.cropMilestones[resource] ?: emptyList()
        val milestone = milestoneBrackets.findLast { it <= resourcesCollected }?.let { milestoneBrackets.indexOf(it) } ?: 0
        val maxLevel = milestoneBrackets.size - 1

        val milestoneText = Text.of(milestone.toString()) { color = if (maxLevel == milestone) TextColor.GREEN else TextColor.RED }
        return loadingValue(
            Displays.item(resource.getItem(), customStackText = milestoneText).withTooltip {
                add(resource.getItem().customName?.stripped ?: "Unknown") {
                    bold = true
                    append(" Milestone")
                }
                add("Progress: ") {
                    color = TextColor.GRAY
                    append(milestoneText)
                    append("/")
                    append(maxLevel.toString())
                }
                if (milestone != maxLevel) {
                    add("Progress to ${milestone + 1}: ") {
                        this.color = TextColor.GRAY

                        val collected = resourcesCollected - milestoneBrackets[(milestone).coerceAtLeast(0)]
                        val needed = milestoneBrackets[milestone + 1] - milestoneBrackets[(milestone).coerceAtLeast(0)]

                        append(collected.toFormattedString()) { color = TextColor.YELLOW }
                        append("/") { color = TextColor.GOLD }
                        append(needed.shorten()) { color = TextColor.YELLOW }

                        append(
                            Text.of("${((collected.toFloat() / needed) * 100).round()}%") {
                                this.color = TextColor.DARK_AQUA
                            }.wrap(" (", ")"),
                        )
                    }
                }
                add("Total") {
                    this.color = TextColor.GRAY

                    if (milestone != maxLevel) {
                        append(" Progress")
                    }
                    append(": ")

                    append(resourcesCollected.toFormattedString()) { color = TextColor.YELLOW }
                    if (milestone != maxLevel) {
                        append("/") { color = TextColor.GOLD }
                        append("${milestoneBrackets.last().shorten()} ") {
                            this.color = TextColor.YELLOW
                        }
                        append(
                            Text.of("${((resourcesCollected.toFloat() / milestoneBrackets.last()) * 100).round()}%") {
                                this.color = TextColor.DARK_AQUA
                            }.wrap("(", ")"),
                        )
                    }
                }
            },
            Displays.item(Items.ORANGE_DYE).withTooltip(Text.of("Loading...") { this.color = TextColor.LIGHT_PURPLE }),
            Displays.item(Items.BEDROCK).withTooltip(Text.of("Error!") { this.color = TextColor.RED }),
        )
    }

    private fun getTool(resource: GardenResource, profile: SkyBlockProfile): Display {
        val staticToolInfo = StaticGardenData.tools[resource]
        val backgroundDisplay = Displays.background(SkyBlockPv.id(staticToolInfo?.type?.id ?: "icon/questionmark"), 16, 16)

        if (staticToolInfo == null) return backgroundDisplay.withTooltip(resource.name)

        val tool = ItemPredicateHelper.getItemsMatching(profile, ItemPredicates.AnySkyblockID(staticToolInfo.ids))
            ?.asSequence()
            ?.sortedByDescending(::evaluateToolScore)
            ?.firstOrNull()

        return tool?.let { Displays.item(it, showTooltip = true) } ?: backgroundDisplay
    }

    private fun evaluateToolScore(stack: ItemStack): Long {
        return stack.getData(DataTypes.CROPS_BROKEN) ?: stack.getData(DataTypes.RARITY)?.ordinal?.toLong() ?: -1L
    }
}
