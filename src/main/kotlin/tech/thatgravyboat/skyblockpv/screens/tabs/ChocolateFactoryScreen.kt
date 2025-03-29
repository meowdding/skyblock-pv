package tech.thatgravyboat.skyblockpv.screens.tabs

import com.mojang.authlib.GameProfile
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.Text.wrap
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.bold
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockpv.SkyBlockPv
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.data.CFCodecs
import tech.thatgravyboat.skyblockpv.data.CFData
import tech.thatgravyboat.skyblockpv.data.RabbitEmployee
import tech.thatgravyboat.skyblockpv.screens.BasePvScreen
import tech.thatgravyboat.skyblockpv.utils.LayoutBuild
import tech.thatgravyboat.skyblockpv.utils.LayoutBuilder
import tech.thatgravyboat.skyblockpv.utils.LayoutBuilder.Companion.setPos
import tech.thatgravyboat.skyblockpv.utils.Utils.append
import tech.thatgravyboat.skyblockpv.utils.components.PvWidgets
import tech.thatgravyboat.skyblockpv.utils.displays.*

class ChocolateFactoryScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BasePvScreen("CF", gameProfile, profile) {

    override fun create(bg: DisplayWidget) {
        val profile = profile ?: return
        val cf = profile.chocolateFactoryData ?: return
        val data = CFCodecs.data ?: return

        LayoutBuild.horizontal {
            getEmployees(cf, data)
            getRarities(cf, data)
        }.setPos(bg.x + 5, bg.y + 5).visitWidgets(this::addRenderableWidget)
    }

    private fun LayoutBuilder.getRarities(cf: CFData, data: CFCodecs.CfRepoData) =
        cf.rabbits.entries.groupBy { data.rabbits.entries.find { repo -> repo.value.contains(it.key) }?.key }.map { rarity ->
            val item = data.textures.find { it.id == rarity.key?.name }?.createSkull() ?: Items.BARRIER.defaultInstance

            listOf(
                Displays.item(item, 12, 12),
                Displays.padding(
                    0,
                    0,
                    2,
                    0,
                    Displays.text(Text.of("${rarity.value.size} - ${rarity.value.sumOf { it.value }}") { color = rarity.key?.color ?: 0x000000 }),
                ),
            ).toRow(1).let { Displays.background(SkyBlockPv.id("box/rounded_box_thin"), Displays.padding(2, it)) }
        }.chunked(4).map { it.toRow(1) }.toColumn(1, Alignment.CENTER).let { widget(PvWidgets.label("Rarities", it.asWidget())) }

    private fun LayoutBuilder.getEmployees(cf: CFData, data: CFCodecs.CfRepoData) = data.employees.map { repoEmployee ->
        val employee = cf.employees.find { it.id == repoEmployee.id } ?: RabbitEmployee(repoEmployee.id, 0)
        val item = (CFCodecs.data?.textures?.find { it.id == employee.id }?.createSkull() ?: Items.BARRIER.defaultInstance).takeIf { employee.level > 0 }
            ?: Items.GRAY_DYE.defaultInstance
        listOf(
            Displays.item(item, 12, 12),
            Displays.padding(0, 0, 2, 0, Displays.text(Text.of("${employee.level}") { color = employee.color })),
        ).toRow(1).let { Displays.background(SkyBlockPv.id("box/rounded_box_thin"), Displays.padding(2, it)) }.withTooltip {
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
    }.chunked(4).map { it.toRow(1) }.toColumn(1, Alignment.CENTER).let { widget(PvWidgets.label("Employees", it.asWidget())) }
}
