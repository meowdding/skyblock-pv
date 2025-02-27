package tech.thatgravyboat.skyblockpv.screens.tabs

import com.mojang.authlib.GameProfile
import net.minecraft.client.gui.layouts.FrameLayout
import net.minecraft.client.gui.layouts.LayoutElement
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.data.DungeonData
import tech.thatgravyboat.skyblockpv.screens.BasePvScreen
import tech.thatgravyboat.skyblockpv.utils.LayoutBuild
import tech.thatgravyboat.skyblockpv.utils.Utils
import tech.thatgravyboat.skyblockpv.utils.Utils.round
import tech.thatgravyboat.skyblockpv.utils.displays.DisplayWidget
import tech.thatgravyboat.skyblockpv.utils.displays.Displays
import tech.thatgravyboat.skyblockpv.utils.displays.asTable
import tech.thatgravyboat.skyblockpv.utils.displays.asWidget

class DungeonScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BasePvScreen("DUNGEON", gameProfile, profile) {
    override fun create(bg: DisplayWidget) {
        val dungeonData = profile?.dungeonData!!
        val row = LayoutBuild.horizontal(5) {
            widget(createInfoBoxDisplay(dungeonData))
            widget(createRunsDisplay(dungeonData))
        }

        FrameLayout.centerInRectangle(row, bg.x, bg.y, bg.width, bg.height)

        row.visitWidgets(this::addRenderableWidget)
    }

    private fun countRuns(completions: Map<String, Long>?) = completions?.filterKeys { it != "total" }?.values?.sum() ?: 0

    private fun createInfoBoxDisplay(dungeonData: DungeonData) = LayoutBuild.vertical {
        val catacombsCompl = dungeonData.dungeonTypes["catacombs"]?.tierCompletions
        val masterModeCompl = dungeonData.dungeonTypes["master_catacombs"]?.tierCompletions

        val mainContent = LayoutBuild.vertical {
            string("Class Average: TODO")
            string("Secrets: ${dungeonData.secrets.toFormattedString()}")
            string("Secrets/Run: ${(dungeonData.secrets / (countRuns(catacombsCompl) + countRuns(masterModeCompl))).round()}")
        }

        widget(Utils.getTitleWidget("Dungeon Info", mainContent.width + 20))
        widget(Utils.getMainContentWidget(mainContent, mainContent.width + 20))
    }

    private fun createRunsDisplay(dungeonData: DungeonData): LayoutElement {
        val catacombs = dungeonData.dungeonTypes["catacombs"]
        val masterMode = dungeonData.dungeonTypes["master_catacombs"]
        val catacombsComp = catacombs?.tierCompletions
        val masterComp = masterMode?.tierCompletions

        fun MutableList<List<String>>.getRow(name: String, floor: String) {
            add(listOf(name, catacombsComp?.get(floor)?.toString() ?: "0", masterComp?.get(floor)?.toString() ?: "0"))
        }

        val table = buildList {
            add(listOf("", "Cata", "Master"))
            getRow("Bonzo", "1")
            getRow("Scarf", "2")
            getRow("Prof.", "3")
            getRow("Thorn", "4")
            getRow("Livid", "5")
            getRow("Sadan", "6")
            getRow("Necron", "7")
            add(listOf("Total", countRuns(catacombsComp).toString(), countRuns(masterComp).toString()))
        }.map { it.map { Displays.text(it, color = { 0x555555u }, shadow = false) } }.asTable(10).asWidget()

        return LayoutBuild.vertical {
            widget(Utils.getTitleWidget("Dungeon Runs", table.width + 20))
            widget(Utils.getMainContentWidget(table, table.width + 20))
        }
    }

}

