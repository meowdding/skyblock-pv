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
            widget(createLevelingDisplay(dungeonData))
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

    private fun createLevelingDisplay(dungeonData: DungeonData) = LayoutBuild.vertical {
        val catacombsXp = dungeonData.dungeonTypes["catacombs"]?.experience ?: 0
        val classXp = dungeonData.classExperience

        val catacombsLevel = levelXpMap.entries.findLast { it.value < catacombsXp }?.key ?: 50
        val catacombsProgressToNext = (catacombsXp - levelXpMap[catacombsLevel]!!).toFloat() / (levelXpMap[catacombsLevel + 1]!! - levelXpMap[catacombsLevel]!!)

        val mainContent = LayoutBuild.vertical(10) {
            vertical(5) {
                string("Catacombs: $catacombsLevel")
                display(Displays.progress(catacombsProgressToNext))
            }
        }

        widget(Utils.getTitleWidget("Dungeon Levels", mainContent.width + 20))
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

    // region Leveling
    private val levelXpMap = mapOf(
        1 to 50,
        2 to 125,
        3 to 235,
        4 to 395,
        5 to 625,
        6 to 955,
        7 to 1425,
        8 to 2095,
        9 to 3045,
        10 to 4385,
        11 to 6275,
        12 to 8940,
        13 to 12700,
        14 to 17960,
        15 to 25340,
        16 to 35640,
        17 to 50040,
        18 to 70040,
        19 to 97640,
        20 to 135640,
        21 to 188140,
        22 to 259640,
        23 to 356640,
        24 to 488640,
        25 to 668640,
        26 to 911640,
        27 to 1239640,
        28 to 1684640,
        29 to 2284640,
        30 to 3084640,
        31 to 4149640,
        32 to 5559640,
        33 to 7459640,
        34 to 9959640,
        35 to 13259640,
        36 to 17559640,
        37 to 23159640,
        38 to 30359640,
        39 to 39559640,
        40 to 51559640,
        41 to 66559640,
        42 to 85559640,
        43 to 109559640,
        44 to 139559640,
        45 to 177559640,
        46 to 225559640,
        47 to 285559640,
        48 to 360559640,
        49 to 453559640,
        50 to 569809640
    )
    // endregion


}

