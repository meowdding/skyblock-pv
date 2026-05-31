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
import me.owdding.skyblockpv.utils.displays.ExtraDisplays.grayText
import me.owdding.skyblockpv.utils.theme.PvColors
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.client.gui.layouts.LayoutElement
import tech.thatgravyboat.skyblockapi.utils.builders.TooltipBuilder
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
                        widget(info)
                        widget(leveling)
                        widget(runs)
                    }.asScrollable(bg.width, bg.height),
                )
            } else {
                horizontal(5) {
                    widget(info)
                    widget(leveling)
                    widget(runs)
                }
            }
        }
    }

    private fun countRuns(completions: Map<String, Long>?) = completions?.filterKeys { it != "total" }?.values?.sum() ?: 0

    private fun createInfoBoxDisplay(dungeonData: DungeonData): LayoutElement {
        val catacombsCompl = dungeonData.dungeonTypes["catacombs"]?.completions
        val masterModeCompl = dungeonData.dungeonTypes["master_catacombs"]?.completions
        val runCounts = (countRuns(catacombsCompl) + countRuns(masterModeCompl)).coerceAtLeast(1)

        val classAvg = dungeonData.classToLevel.values.map { it.first.coerceAtMost(50) }.average().round()
        val secretsPerRun = (dungeonData.secrets / runCounts.toDouble()).round()

        val mainContent = PvLayouts.vertical {
            string("Class Average: $classAvg")
            string("Secrets: ${dungeonData.secrets.toFormattedString()}")
            string("Secrets/Run: $secretsPerRun")
        }

        return PvWidgets.label("Dungeon Info", mainContent, 20, icon = SkyBlockPv.id("icon/item/clipboard"))
    }

    private fun createLevelingDisplay(dungeonData: DungeonData): LayoutElement {
        val catacombsXp = dungeonData.dungeonTypes["catacombs"]?.experience ?: 0
        val (catacombsLevel, catacombsProgress) = CatacombsCodecs.getLevelAndProgress(catacombsXp, Config.skillOverflow)

        fun getClass(name: String) = PvLayouts.vertical(5) {
            val (level, progress) = dungeonData.classToLevel[name] ?: (0 to 0f)
            val isSelected = dungeonData.selectedClass == name

            val classTooltip = TooltipBuilder().apply {
                add(name.replaceFirstChar { it.uppercase() }) { color = PvColors.YELLOW }
                add("Progress: ${(progress * 100).round()}%") { color = PvColors.GRAY }
                if (level >= 50) add("Maxed!") { color = PvColors.GOLD }
            }.build()

            val classDisplay = Displays.text(
                "${name.replaceFirstChar { it.uppercase() }}: $level",
                color = { if (isSelected) PvColors.DARK_GREEN.toUInt() else PvColors.DARK_GRAY.toUInt() },
                shadow = false,
            ).withTooltip(classTooltip)

            display(classDisplay)
            display(ExtraDisplays.progress(progress, maxed = level >= 50).withTooltip(classTooltip))
        }

        val mainContent = PvLayouts.vertical(10) {
            vertical(5) {
                val cataTooltip = TooltipBuilder().apply {
                    add("Catacombs") { color = PvColors.YELLOW }
                    add("Total XP: ${catacombsXp.toFormattedString()}") { color = PvColors.GRAY }
                    add("Progress: ${(catacombsProgress * 100).round()}%") { color = PvColors.GRAY }
                }.build()

                display(grayText("Catacombs: $catacombsLevel").withTooltip(cataTooltip))
                display(ExtraDisplays.progress(catacombsProgress, maxed = catacombsLevel >= 50).withTooltip(cataTooltip))

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

        fun MutableList<List<Display>>.getRow(name: String, floor: String) = add(
            buildList {
                fun getFloorWidget(floorData: DungeonFloor?) = grayText((floorData?.completions ?: 0).toString()).withTooltip {
                    val data = floorData ?: DungeonFloor.EMPTY
                    add("Completions: ") { color = TextColor.GRAY; append(data.completions.toString()) { color = TextColor.WHITE } }
                    add("Fastest Time: ") {
                        color = TextColor.GRAY; append(if (data.fastestTime.isPositive()) data.fastestTime.toReadableTime(allowMs = true) else "N/A") {
                        color = TextColor.WHITE
                    }
                    }
                    add("Fastest S+: ") {
                        color =
                            TextColor.GRAY; append(if (data.fastestTimeSplus.isPositive()) data.fastestTimeSplus.toReadableTime(allowMs = true) else "N/A") {
                        color = TextColor.WHITE
                    }
                    }
                    add("Best Score: ") {
                        color = TextColor.GRAY; append(if (data.bestScore > 0) data.bestScore.toString() else "N/A") {
                        color = TextColor.WHITE
                    }
                    }
                }

                add(grayText(name))
                add(getFloorWidget(catacombs?.floors?.get(floor)))
                add(getFloorWidget(masterMode?.floors?.get(floor)))
            },
        )

        val table = buildList {
            add(listOf(grayText(""), grayText("Cata"), grayText("Master")))
            getRow("Bonzo", "1")
            getRow("Scarf", "2")
            getRow("Prof.", "3")
            getRow("Thorn", "4")
            getRow("Livid", "5")
            getRow("Sadan", "6")
            getRow("Necron", "7")
            add(listOf(grayText("Total"), grayText(countRuns(catacombs?.completions).toString()), grayText(countRuns(masterMode?.completions).toString())))
        }.asTable(10).asWidget()

        return PvWidgets.label("Dungeon Runs", table, 20)
    }
}
