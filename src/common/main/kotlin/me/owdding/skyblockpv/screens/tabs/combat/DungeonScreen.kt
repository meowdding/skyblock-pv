package me.owdding.skyblockpv.screens.tabs.combat

import com.mojang.authlib.GameProfile
import me.owdding.lib.displays.DisplayWidget
import me.owdding.lib.displays.asTable
import me.owdding.lib.displays.asWidget
import me.owdding.lib.extensions.round
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.api.data.SkyBlockProfile
import me.owdding.skyblockpv.config.Config
import me.owdding.skyblockpv.data.api.skills.combat.DungeonData
import me.owdding.skyblockpv.data.repo.CatacombsCodecs
import me.owdding.skyblockpv.utils.LayoutUtils.asScrollable
import me.owdding.skyblockpv.utils.components.PvLayouts
import me.owdding.skyblockpv.utils.components.PvWidgets
import me.owdding.skyblockpv.utils.displays.ExtraDisplays
import me.owdding.skyblockpv.utils.theme.PvColors
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.client.gui.layouts.LayoutElement
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

class DungeonScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseCombatScreen(gameProfile, profile) {

    var classToLevel: Map<String, Pair<Int, Float>>? = null

    override fun onProfileSwitch(profile: SkyBlockProfile) {
        super.onProfileSwitch(profile)

        classToLevel = profile.dungeonData?.classExperience?.map { (name, xp) ->
            name to CatacombsCodecs.getLevelAndProgress(xp, Config.skillOverflow)
        }?.toMap()
    }

    override fun getLayout(bg: DisplayWidget): Layout {
        val dungeonData = profile.dungeonData ?: return PvLayouts.vertical {
            string("No Dungeon Data")
        }

        val info = createInfoBoxDisplay(dungeonData)
        val leveling = createLevelingDisplay(dungeonData)
        val runs = createRunsDisplay(dungeonData)

        return PvLayouts.frame(bg.width, bg.height) {
            if (info.width + leveling.width + runs.width + 10 > bg.width) {
                widget(
                    PvLayouts.vertical(5) {
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

        val mainContent = PvLayouts.vertical {
            string("Class Average: ${classToLevel?.map { it.value.first.coerceAtMost(50) }?.toList()?.average()}")
            string("Secrets: ${dungeonData.secrets.toFormattedString()}")
            string("Secrets/Run: ${(dungeonData.secrets / runCounts).round()}")
        }

        return PvWidgets.label("Dungeon Info", mainContent, 20, icon = SkyBlockPv.id("icon/item/clipboard"))
    }

    private fun createLevelingDisplay(dungeonData: DungeonData): LayoutElement {
        val catacombsXp = dungeonData.dungeonTypes["catacombs"]?.experience ?: 0

        val (catacombsLevel, catacombsProgressToNext) = CatacombsCodecs.getLevelAndProgress(catacombsXp, Config.skillOverflow)

        fun getClass(name: String) = PvLayouts.vertical(5) {
            val (level, progress) = classToLevel?.get(name)!!
            textDisplay("${name.replaceFirstChar { it.uppercase() }}: $level") {
                color = if (dungeonData.selectedClass == name) PvColors.DARK_GREEN else PvColors.DARK_GRAY
            }
            display(ExtraDisplays.progress(progress, maxed = level >= 50))
        }

        val mainContent = PvLayouts.vertical(10) {
            vertical(5) {
                string("Catacombs: $catacombsLevel")
                display(ExtraDisplays.progress(catacombsProgressToNext, maxed = catacombsLevel >= 50))
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
        }.map { it.map { ExtraDisplays.grayText(it) } }.asTable(10).asWidget()

        return PvWidgets.label("Dungeon Runs", table, 20)
    }
}
