package me.owdding.skyblockpv.screens.windowed.tabs.combat

import com.mojang.authlib.GameProfile
import me.owdding.lib.displays.*
import me.owdding.lib.extensions.round
import me.owdding.lib.extensions.toReadableTime
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.config.Config
import me.owdding.skyblockpv.data.api.skills.combat.DungeonData
import me.owdding.skyblockpv.data.api.skills.combat.DungeonFloor
import me.owdding.skyblockpv.data.repo.CatacombsCodecs
import me.owdding.skyblockpv.utils.LayoutUtils.asScrollable
import me.owdding.skyblockpv.utils.Utils.append
import me.owdding.skyblockpv.utils.components.PvLayouts
import me.owdding.skyblockpv.utils.components.PvWidgets
import me.owdding.skyblockpv.utils.displays.ExtraDisplays
import me.owdding.skyblockpv.utils.theme.PvColors
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.client.gui.layouts.LayoutElement
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

class DungeonScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseCombatScreen(gameProfile, profile) {
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
        val catacombsCompl = dungeonData.dungeonTypes["catacombs"]?.completions
        val masterModeCompl = dungeonData.dungeonTypes["master_catacombs"]?.completions

        val runCounts = (countRuns(catacombsCompl) + countRuns(masterModeCompl)).coerceAtLeast(1)

        val mainContent = PvLayouts.vertical {
            string("Class Average: ${dungeonData.classToLevel.map { it.value.first.coerceAtMost(50) }.toList().average()}")
            string("Secrets: ${dungeonData.secrets.toFormattedString()}")
            string("Secrets/Run: ${(dungeonData.secrets / runCounts).round()}")
        }

        return PvWidgets.label("Dungeon Info", mainContent, 20, icon = SkyBlockPv.id("icon/item/clipboard"))
    }

    private fun createLevelingDisplay(dungeonData: DungeonData): LayoutElement {
        val catacombsXp = dungeonData.dungeonTypes["catacombs"]?.experience ?: 0

        val (catacombsLevel, catacombsProgressToNext) = CatacombsCodecs.getLevelAndProgress(catacombsXp, Config.skillOverflow)

        fun getClass(name: String) = PvLayouts.vertical(5) {
            val (level, progress) = dungeonData.classToLevel[name]!!
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
        val catacombsComp = catacombs?.completions
        val masterComp = masterMode?.completions

        fun MutableList<List<Display>>.getRow(name: String, floor: String) = buildList {
            fun widget(floor: DungeonFloor) = ExtraDisplays.grayText(floor.completions.toString()).withTooltip {
                fun add(text: String, value: String) {
                    add("$text: ") {
                        color = TextColor.GRAY
                        append(value) { color = TextColor.WHITE }
                    }
                }

                add("Times Played", floor.timesPlayed.toString())
                add("Completions", floor.completions.toString())
                add("Fastest Time", if (floor.fastestTime.isPositive()) floor.fastestTime.toReadableTime(allowMs = true) else "N/A")
                add("Best Score", if (floor.bestScore > 0) floor.bestScore.toString() else "N/A")
            }

            add(ExtraDisplays.grayText(name))
            add(widget(catacombs?.floors?.get(floor) ?: DungeonFloor.EMPTY))
            add(widget(masterMode?.floors?.get(floor) ?: DungeonFloor.EMPTY))
        }.let(this::add)

        val table = buildList {
            fun add(strings: List<String>) = add(strings.map { ExtraDisplays.grayText(it) })

            add(listOf("", "Cata", "Master"))
            getRow("Bonzo", "1")
            getRow("Scarf", "2")
            getRow("Prof.", "3")
            getRow("Thorn", "4")
            getRow("Livid", "5")
            getRow("Sadan", "6")
            getRow("Necron", "7")
            add(listOf("Total", countRuns(catacombsComp).toString(), countRuns(masterComp).toString()))
        }.asTable(10).asWidget()

        return PvWidgets.label("Dungeon Runs", table, 20)
    }
}
