package tech.thatgravyboat.skyblockpv.screens.tabs.farming

import com.mojang.authlib.GameProfile
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.bold
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockpv.SkyBlockPv
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.api.predicates.ItemPredicateHelper
import tech.thatgravyboat.skyblockpv.api.predicates.ItemPredicates
import tech.thatgravyboat.skyblockpv.data.GardenResource
import tech.thatgravyboat.skyblockpv.data.StaticGardenData
import tech.thatgravyboat.skyblockpv.utils.LayoutBuild
import tech.thatgravyboat.skyblockpv.utils.Utils.addText
import tech.thatgravyboat.skyblockpv.utils.Utils.addTextIf
import tech.thatgravyboat.skyblockpv.utils.Utils.append
import tech.thatgravyboat.skyblockpv.utils.Utils.round
import tech.thatgravyboat.skyblockpv.utils.Utils.shorten
import tech.thatgravyboat.skyblockpv.utils.displays.*

class CropScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseFarmingScreen(gameProfile, profile) {

    override fun getLayout(): Layout {
        val profile = profile ?: return LayoutBuild.frame {}

        return GardenResource.entries.mapNotNull {
            if (it == GardenResource.UNKNOWN) return@mapNotNull null

            buildList {
                add(getTool(it, profile))
                add(getCropUpgrade(it))
                add(getCropMilestone(it))
            }.map { Displays.padding(2, it) }.toColumn()
        }.map { Displays.background(SkyBlockPv.id("inventory/inventory-1x3"), Displays.padding(2, it)) }.toRow(2).let { LayoutBuild.frame { display(it) } }
    }

    private fun getCropUpgrade(it: GardenResource): Display {
        val gardenData = gardenProfile?.getOrNull()
        val cropLevel = gardenData?.cropUpgradeLevels?.get(it)?.toInt() ?: 0

        val cropUpgradeCost = StaticGardenData.miscData.cropUpgradeCost
        val maxUpgrades = cropUpgradeCost.size
        val totalCopper = cropUpgradeCost.sum()

        val isMax = cropLevel == maxUpgrades

        fun <T> select(maxed: T, nonMaxed: T) = if (isMax) maxed else nonMaxed

        val cropLevelText = Text.of(cropLevel.toString()) { color = select(TextColor.GREEN, TextColor.RED) }
        return loading(
            Displays.item(select(Items.NETHERRACK, Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE).defaultInstance, customStackText = cropLevelText).buildTooltip {
                addText("Crop Upgrade") { this.bold = true }
                addText("Upgrades: ") {
                    this.color = TextColor.GRAY
                    append(cropLevelText)
                    append("/$maxUpgrades")
                }
                addText("Copper paid: ") {
                    this.color = TextColor.GRAY
                    val copperUsed = cropUpgradeCost.take(cropLevel).sum()
                    append(copperUsed.toFormattedString()) { this.color = TextColor.RED }
                    append("/") { this.color = TextColor.GOLD }
                    append(totalCopper.toFormattedString()) { this.color = TextColor.RED }
                    if (totalCopper != copperUsed) {
                        append(" (")
                        append("${((copperUsed.toFloat() / totalCopper) * 100).round()}%") {
                            this.color = TextColor.YELLOW
                        }
                        append(")")
                    }
                }
            },
            Displays.item(Items.ORANGE_DYE.defaultInstance).withTooltip(Text.of("Loading...") { this.color = TextColor.LIGHT_PURPLE }),
            Displays.item(Items.BEDROCK.defaultInstance).withTooltip(Text.of("Error!") { this.color = TextColor.RED }),
        )
    }

    private fun getCropMilestone(resource: GardenResource): Display {
        val gardenData = gardenProfile?.getOrNull()

        val resourcesCollected = gardenData?.resourcesCollected?.get(resource) ?: 0

        val milestoneBrackets = StaticGardenData.cropMilestones[resource] ?: emptyList()
        val milestone = milestoneBrackets.findLast { it <= resourcesCollected }?.let { milestoneBrackets.indexOf(it) } ?: 0
        val maxLevel = milestoneBrackets.size - 1

        val milestoneText = Text.of(milestone.toString()) { color = if (maxLevel == milestone) TextColor.GREEN else TextColor.RED }
        return loading(
            Displays.item(resource.getItem(), customStackText = milestoneText).buildTooltip {
                addText(resource.getItem().customName?.stripped ?: "meow :(") {
                    bold = true
                    append(" Milestone")
                }
                addText("Progress: ") {
                    color = TextColor.GRAY
                    append(milestoneText)
                    append("/")
                    append(maxLevel.toString())
                }
                addTextIf("Progress to ${milestone + 1}: ", { maxLevel != milestone }) {
                    this.color = TextColor.GRAY

                    val collected = resourcesCollected.minus(milestoneBrackets[(milestone).coerceAtLeast(0)])
                    val needed = milestoneBrackets[milestone + 1].minus(milestoneBrackets[(milestone).coerceAtLeast(0)])

                    append(collected.toFormattedString()) { color = TextColor.YELLOW }
                    append("/") { color = TextColor.GOLD }
                    append(needed.shorten()) { color = TextColor.YELLOW }
                    append(" (")
                    append("${((collected.toFloat() / needed) * 100).round()}%") {
                        this.color = TextColor.DARK_AQUA
                    }
                    append(")")
                }
                addText("Total") {
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
                        append("(")
                        append("${((resourcesCollected.toFloat() / milestoneBrackets.last()) * 100).round()}%") {
                            this.color = TextColor.DARK_AQUA
                        }
                        append(")")
                    }
                }
            },
            Displays.item(Items.ORANGE_DYE.defaultInstance).withTooltip(Text.of("Loading...") { this.color = TextColor.LIGHT_PURPLE }),
            Displays.item(Items.BEDROCK.defaultInstance).withTooltip(Text.of("Error!") { this.color = TextColor.RED }),
        )
    }

    private fun getTool(resource: GardenResource, profile: SkyBlockProfile): Display {
        val staticToolInfo = StaticGardenData.tools[resource]

        staticToolInfo ?: return Displays.background(SkyBlockPv.id("icon/questionmark"), 16, 16).withTooltip(resource.name)

        return ItemPredicateHelper.getItemsMatching(profile, ItemPredicates.AnySkyblockID(staticToolInfo.ids))
            ?.asSequence()
            ?.sortedByDescending(::evaluateToolScore)
            ?.take(1)
            ?.map { Displays.item(it, showTooltip = true) }
            ?.toMutableList()
            ?.firstOrNull()
            ?: Displays.background(SkyBlockPv.id(staticToolInfo.type.id), 16, 16)
    }

    private fun evaluateToolScore(stack: ItemStack): Long {
        return stack.getData(DataTypes.CROPS_BROKEN) ?: stack.getData(DataTypes.RARITY)?.ordinal?.toLong() ?: -1L
    }
}
