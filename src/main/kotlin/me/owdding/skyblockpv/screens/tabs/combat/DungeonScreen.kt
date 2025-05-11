package me.owdding.skyblockpv.screens.tabs.combat

import com.mojang.authlib.GameProfile
import me.owdding.lib.builder.LayoutFactory
import me.owdding.lib.displays.DisplayWidget
import me.owdding.lib.displays.Displays
import me.owdding.lib.displays.asTable
import me.owdding.lib.displays.asWidget
import me.owdding.lib.extensions.round
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.api.data.SkyBlockProfile
import me.owdding.skyblockpv.data.api.skills.combat.DungeonData
import me.owdding.skyblockpv.utils.LayoutUtils.asScrollable
import me.owdding.skyblockpv.utils.components.PvWidgets
import me.owdding.skyblockpv.utils.displays.ExtraDisplays
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.client.gui.layouts.LayoutElement
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString

class DungeonScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseCombatScreen(gameProfile, profile) {
    val classToLevel by lazy {
        this.profile.dungeonData?.classExperience?.map { (name, xp) ->
            name to (levelXpMap.entries.findLast { it.value < xp }?.key ?: 50)
        }?.toMap()
    }

    val classToProgress by lazy {
        this.profile.dungeonData?.classExperience?.map { (name, xp) ->
            val level = classToLevel?.get(name)!!
            val currentXp = levelXpMap[level]!!
            val nextXp = levelXpMap[level + 1]

            name to if (nextXp == null) 1.0f else (xp - currentXp).toFloat() / (nextXp - currentXp)
        }?.toMap()
    }

    override fun getLayout(bg: DisplayWidget): Layout {
        val dungeonData = profile.dungeonData ?: return LayoutFactory.vertical {
            string("No Dungeon Data")
        }

        val info = createInfoBoxDisplay(dungeonData)
        val leveling = createLevelingDisplay(dungeonData)
        val runs = createRunsDisplay(dungeonData)

        return LayoutFactory.frame(bg.width, bg.height) {
            if (info.width + leveling.width + runs.width + 10 > bg.width) {
                widget(
                    LayoutFactory.vertical(5) {
                        widget(createInfoBoxDisplay(dungeonData))
                        widget(createLevelingDisplay(dungeonData))
                        widget(createRunsDisplay(dungeonData))
                    }.asScrollable(bg.width, bg.height),
                )
            } else {
                horizontal(5) {
                    widget(createInfoBoxDisplay(dungeonData))
                    widget(createLevelingDisplay(dungeonData))
                    widget(createRunsDisplay(dungeonData))
                }
            }
        }
    }

    private fun countRuns(completions: Map<String, Long>?) = completions?.filterKeys { it != "total" }?.values?.sum() ?: 0

    private fun createInfoBoxDisplay(dungeonData: DungeonData): LayoutElement {
        val catacombsCompl = dungeonData.dungeonTypes["catacombs"]?.tierCompletions
        val masterModeCompl = dungeonData.dungeonTypes["master_catacombs"]?.tierCompletions

        val runCounts = (countRuns(catacombsCompl) + countRuns(masterModeCompl)).coerceAtLeast(1)

        val mainContent = LayoutFactory.vertical {
            string("Class Average: ${classToLevel?.map { it.value }?.toList()?.average()}")
            string("Secrets: ${dungeonData.secrets.toFormattedString()}")
            string("Secrets/Run: ${(dungeonData.secrets / runCounts).round()}")
        }

        return PvWidgets.label("Dungeon Info", mainContent, 20, SkyBlockPv.id("icon/item/clipboard"))
    }

    private fun createLevelingDisplay(dungeonData: DungeonData): LayoutElement {
        val catacombsXp = dungeonData.dungeonTypes["catacombs"]?.experience ?: 0

        val catacombsLevel = levelXpMap.entries.findLast { it.value < catacombsXp }?.key ?: 50
        val catacombsProgressToNext = if (levelXpMap.containsKey(catacombsLevel + 1)) {
            (catacombsXp - levelXpMap[catacombsLevel]!!).toFloat() / (levelXpMap[catacombsLevel + 1]!! - levelXpMap[catacombsLevel]!!)
        } else {
            1.0f
        }

        fun getClass(name: String) = LayoutFactory.vertical(5) {
            val level = classToLevel?.get(name)!!
            val progress = classToProgress?.get(name)!!
            string("${name.replaceFirstChar { it.uppercase() }}: $level")
            display(ExtraDisplays.progress(progress))
        }

        val mainContent = LayoutFactory.vertical(10) {
            vertical(5) {
                string("Catacombs: $catacombsLevel")
                display(ExtraDisplays.progress(catacombsProgressToNext))
                widget(getClass("healer"))
                widget(getClass("mage"))
                widget(getClass("berserk"))
                widget(getClass("archer"))
                widget(getClass("tank"))
            }
        }

        return PvWidgets.label("Dungeon Levels", mainContent, 20)
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

        return PvWidgets.label("Dungeon Runs", table, 20)
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
        50 to 569809640,
    )
    // endregion
}
