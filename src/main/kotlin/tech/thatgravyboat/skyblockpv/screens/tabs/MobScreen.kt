package tech.thatgravyboat.skyblockpv.screens.tabs

import com.mojang.authlib.GameProfile
import earth.terrarium.olympus.client.components.Widgets
import earth.terrarium.olympus.client.components.base.ListWidget
import net.minecraft.client.gui.layouts.FrameLayout
import net.minecraft.client.gui.layouts.LinearLayout
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockpv.api.data.SkyblockProfile
import tech.thatgravyboat.skyblockpv.data.MobData
import tech.thatgravyboat.skyblockpv.screens.BasePvScreen
import tech.thatgravyboat.skyblockpv.utils.LayoutBuild
import tech.thatgravyboat.skyblockpv.utils.displays.DisplayWidget

class MobScreen(gameProfile: GameProfile, profile: SkyblockProfile? = null) : BasePvScreen("MOB", gameProfile, profile) {
    override fun create(bg: DisplayWidget) {
        val columnWidth = uiWidth / 2 - 20
        val columnHeight = uiHeight - 20

        val row = LinearLayout.horizontal().spacing(5)

        val mobData = profile?.mobData ?: emptyList()
        val sortedByKills = mobData.filter { it.kills != 0L }.sortedByDescending { it.kills }
        val sortedByDeaths = mobData.filter { it.deaths != 0L }.sortedByDescending { it.deaths }

        val killsColumn = createList("Kills", sortedByKills, true, columnWidth, columnHeight)
        val deathsColumn = createList("Deaths", sortedByDeaths, false, columnWidth, columnHeight)

        row.addChild(killsColumn)
        row.addChild(deathsColumn)

        row.arrangeElements()

        FrameLayout.centerInRectangle(row, bg.x, bg.y, bg.width, bg.height)

        row.visitWidgets(this::addRenderableWidget)
    }

    private fun createList(name: String, list: List<MobData>, useKills: Boolean, width: Int, height: Int) = LayoutBuild.vertical(5) {
        string(name)

        val listWidget = ListWidget(width, height)

        list.forEach { (id, kills, death) ->
            val formattedName = id.split("_").joinToString(" ") { it.replaceFirstChar { it.titlecase() } }
            listWidget.add(Widgets.text("$formattedName: ${(if (useKills) kills else death).toFormattedString()}"))
        }

        widget(listWidget)
    }
}
