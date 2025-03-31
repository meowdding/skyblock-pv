package tech.thatgravyboat.skyblockpv.screens.tabs.combat

import com.mojang.authlib.GameProfile
import earth.terrarium.olympus.client.components.Widgets
import earth.terrarium.olympus.client.components.base.ListWidget
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.client.gui.layouts.LinearLayout
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.data.skills.combat.MobData
import tech.thatgravyboat.skyblockpv.utils.LayoutBuild
import tech.thatgravyboat.skyblockpv.utils.LayoutUtils.centerHorizontally

class MobScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseCombatScreen(gameProfile, profile) {

    override fun getLayout(): Layout {
        val columnWidth = uiWidth / 2 - 20
        val columnHeight = uiHeight - 20

        val row = LinearLayout.horizontal().spacing(5)

        val mobData = profile?.mobData?.combineMobs() ?: emptyList()
        val sortedByKills = mobData.filter { it.kills != 0L }.sortedByDescending { it.kills }
        val sortedByDeaths = mobData.filter { it.deaths != 0L }.sortedByDescending { it.deaths }

        val killsColumn = createList("Kills", sortedByKills, true, columnWidth, columnHeight)
        val deathsColumn = createList("Deaths", sortedByDeaths, false, columnWidth, columnHeight)

        row.addChild(killsColumn)
        row.addChild(deathsColumn)

        return row
    }

    private fun List<MobData>.combineMobs() = groupBy { mob ->
        val parts = mob.mobId.split("_")
        if (parts.last().all { it.isDigit() } && parts.size > 1) {
            parts.dropLast(1).joinToString("_")
        } else mob.mobId
    }.map { (id, data) ->
        MobData(id, data.sumOf { it.kills }, data.sumOf { it.deaths })
    }


    private fun createList(name: String, list: List<MobData>, useKills: Boolean, width: Int, height: Int) = LayoutBuild.vertical(5) {
        widget(Widgets.text(name).centerHorizontally(width))

        val listWidget = ListWidget(width, height)

        list.forEach { (id, kills, death) ->
            val formattedName = id.split("_").joinToString(" ") { it.replaceFirstChar { it.titlecase() } }
            listWidget.add(Widgets.text("$formattedName: ${(if (useKills) kills else death).toFormattedString()}"))
        }

        widget(listWidget)
    }
}
