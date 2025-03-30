package tech.thatgravyboat.skyblockpv.screens.tabs

import com.mojang.authlib.GameProfile
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.Text.wrap
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.bold
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.data.CfCodecs
import tech.thatgravyboat.skyblockpv.data.CfData
import tech.thatgravyboat.skyblockpv.data.RabbitEmployee
import tech.thatgravyboat.skyblockpv.data.SortedEntry.Companion.sortToRarityOrder
import tech.thatgravyboat.skyblockpv.screens.BasePvScreen
import tech.thatgravyboat.skyblockpv.utils.LayoutBuild
import tech.thatgravyboat.skyblockpv.utils.LayoutBuilder.Companion.setPos
import tech.thatgravyboat.skyblockpv.utils.LayoutUtils.asScrollable
import tech.thatgravyboat.skyblockpv.utils.Utils.append
import tech.thatgravyboat.skyblockpv.utils.Utils.shorten
import tech.thatgravyboat.skyblockpv.utils.components.PvWidgets
import tech.thatgravyboat.skyblockpv.utils.displays.*

class ChocolateFactoryScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BasePvScreen("CF", gameProfile, profile) {

    override fun create(bg: DisplayWidget) {
        val profile = profile ?: return
        val cf = profile.chocolateFactoryData ?: return
        val data = CfCodecs.data ?: return

        val employees = getEmployees(cf, data)
        val rarities = getRarities(cf, data)
        val info = getInfo(cf, data)

        LayoutBuild.frame(bg.width, bg.height) {
            if (maxOf(employees.width, rarities.width) + info.width + 3 > bg.width) {
                widget(
                    LayoutBuild.vertical(3, 0.5f) {
                        widget(employees)
                        widget(rarities)
                        widget(info)
                    }.asScrollable(bg.width, bg.height),
                )
            } else {
                horizontal(3, 0.5f) {
                    vertical(3, 0.5f) {
                        widget(employees)
                        widget(rarities)
                    }
                    widget(info)
                }
            }
        }.setPos(bg.x, bg.y).visitWidgets(this::addRenderableWidget)
    }

    private fun getInfo(cf: CfData, data: CfCodecs.CfRepoData) = PvWidgets.label(
        "Information",
        LayoutBuild.vertical {
            string("Chocolate: ") {
                color = TextColor.DARK_GRAY
                append(cf.chocolate.shorten()) {
                    color = TextColor.GOLD
                }
            }
            string("Total Chocolate: ") {
                color = TextColor.DARK_GRAY
                append(cf.totalChocolate.shorten()) {
                    color = TextColor.GOLD
                }
            }
            string("Chocolate since Prestige: ") {
                color = TextColor.DARK_GRAY
                append(cf.chocolateSincePrestige.shorten()) {
                    color = TextColor.GOLD
                }
            }
            if (cf.prestigeLevel != data.misc.maxPrestigeLevel) {
                val needed = (data.misc.chocolatePerPrestige[cf.prestigeLevel + 1] ?: 0) - cf.chocolateSincePrestige
                val string = "§aReady!".takeIf { needed <= 0 } ?: needed.shorten()
                string("Chocolate for next Prestige: ") {
                    color = TextColor.DARK_GRAY
                    append(string) {
                        color = TextColor.GOLD
                    }
                }
            }
            string("Prestige Level: ") {
                color = TextColor.DARK_GRAY
                append("${cf.prestigeLevel}") {
                    color = TextColor.LIGHT_PURPLE
                }
            }
            string("Barn Capacity: ") {
                color = TextColor.DARK_GRAY
                append("${cf.barnCapacity}") {
                    color = TextColor.GREEN
                }
            }
        },
    )

    private fun getRarities(cf: CfData, data: CfCodecs.CfRepoData) = cf.rabbits.entries
        .groupBy { data.rabbits.entries.find { repo -> repo.value.contains(it.key) }?.key }
        .mapNotNull { if (it.key != null) it.key!! to it.value else null }.toMap()
        .sortToRarityOrder()
        .map { rarity ->
            val item = data.textures.find { it.id == rarity.key.name }?.createSkull() ?: Items.BARRIER.defaultInstance

            PvWidgets.iconNumberElement(item, Text.of("${rarity.value.size}") { color = rarity.key.color })
        }.chunked(4).map { it.toRow(1) }.toColumn(1, Alignment.CENTER).let { PvWidgets.label("Rarities", it.asWidget()) }

    private fun getEmployees(cf: CfData, data: CfCodecs.CfRepoData) = data.employees.map { repoEmployee ->
        val employee = cf.employees.find { it.id == repoEmployee.id } ?: RabbitEmployee(repoEmployee.id, 0)
        val item = (CfCodecs.data?.textures?.find { it.id == employee.id }?.createSkull() ?: Items.BARRIER.defaultInstance).takeIf { employee.level > 0 }
            ?: Items.GRAY_DYE.defaultInstance

        PvWidgets.iconNumberElement(item, Text.of("${employee.level}") { color = employee.color }).withTooltip {
            add(repoEmployee.name) {
                bold = true
                color = employee.color

                append(
                    Text.of("${employee.level}") {
                        color = employee.color
                        bold = false
                    }.wrap(" §7(", "§7)"),
                )
            }
            space()
            add("Produces ") {
                color = TextColor.GRAY
                append("+${repoEmployee.getReward(employee.level).toFormattedString()} Chocolate") { color = TextColor.GOLD }
                append(" per second.") { color = TextColor.GRAY }
            }
        }
    }.chunked(4).map { it.toRow(1) }.toColumn(1, Alignment.CENTER).let { PvWidgets.label("Employees", it.asWidget()) }
}
