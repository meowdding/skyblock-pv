package tech.thatgravyboat.skyblockpv.screens.tabs

import com.mojang.authlib.GameProfile
import net.minecraft.client.gui.layouts.FrameLayout
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.data.DungeonData
import tech.thatgravyboat.skyblockpv.screens.BasePvScreen
import tech.thatgravyboat.skyblockpv.utils.LayoutBuild
import tech.thatgravyboat.skyblockpv.utils.displays.Display
import tech.thatgravyboat.skyblockpv.utils.displays.DisplayWidget
import tech.thatgravyboat.skyblockpv.utils.displays.Displays
import tech.thatgravyboat.skyblockpv.utils.displays.asTable

class DungeonScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BasePvScreen("DUNGEON", gameProfile, profile) {
    override fun create(bg: DisplayWidget) {
        val dungeonData = profile?.dungeonData!!
        val row = LayoutBuild.horizontal(5) {
            display(createRunsDisplay(dungeonData))
        }

        FrameLayout.centerInRectangle(row, bg.x, bg.y, bg.width, bg.height)

        row.visitWidgets(this::addRenderableWidget)
    }

    private fun createRunsDisplay(dungeonData: DungeonData): Display {
        val catacombs = dungeonData.dungeonTypes["catacombs"]
        val masterMode = dungeonData.dungeonTypes["master_catacombs"]
        val catacombsComp = catacombs?.tierCompletions
        val masterComp = masterMode?.tierCompletions

        fun MutableList<List<String>>.getRow(name: String, floor: String) {
            add(listOf(name, catacombsComp?.get(floor)?.toString() ?: "0", masterComp?.get(floor)?.toString() ?: "0"))
        }

        fun countRuns(completions: Map<String, Long>?) = completions?.filterKeys { it != "total" }?.values?.sum() ?: 0

        return buildList {
            add(listOf("", "Catacombs", "Master"))
            getRow("Bonzo", "1")
            getRow("Scarf", "2")
            getRow("Professor", "3")
            getRow("Thorn", "4")
            getRow("Livid", "5")
            getRow("Sadan", "6")
            getRow("Necron", "7")
            add(listOf("Total", countRuns(catacombsComp).toString(), countRuns(masterComp).toString()))
        }.map { it.map { Displays.text(it) } }.asTable(10)
    }

}

