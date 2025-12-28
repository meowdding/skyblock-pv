package me.owdding.skyblockpv.screens.windowed.tabs.combat

import com.mojang.authlib.GameProfile
import me.owdding.lib.displays.DisplayWidget
import me.owdding.lib.displays.withTooltip
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.data.api.skills.combat.CrimsonIsleData
import me.owdding.skyblockpv.data.api.skills.combat.DojoEntry
import me.owdding.skyblockpv.data.api.skills.combat.KuudraEntry
import me.owdding.skyblockpv.data.repo.CrimsonIsleCodecs
import me.owdding.skyblockpv.data.repo.CrimsonIsleCodecs.getFor
import me.owdding.skyblockpv.utils.LayoutUtils.asScrollable
import me.owdding.skyblockpv.utils.components.PvLayouts
import me.owdding.skyblockpv.utils.components.PvWidgets
import me.owdding.skyblockpv.utils.displays.ExtraDisplays
import me.owdding.skyblockpv.utils.theme.PvColors
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.client.gui.layouts.LayoutElement
import net.minecraft.network.chat.CommonComponents
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.Text.wrap
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

class CrimsonIsleScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseCombatScreen(gameProfile, profile) {
    override fun getLayout(bg: DisplayWidget): Layout {
        val crimsonIsleData = profile.crimsonIsleData
        val dojoWidget = getDojoStats(crimsonIsleData.dojoStats)
        val kuudraWidget = getKuudraStats(crimsonIsleData.kuudraStats)
        val reputationWidget = getReputationWidget(crimsonIsleData)

        if (bg.width < dojoWidget.width + kuudraWidget.width + reputationWidget.width + 35) {
            return PvLayouts.vertical(5) {
                widget(dojoWidget) { alignHorizontallyCenter() }
                widget(kuudraWidget) { alignHorizontallyCenter() }
                widget(reputationWidget) { alignHorizontallyCenter() }
            }.asScrollable(bg.width, bg.height)
        }

        return PvLayouts.horizontal {
            widget(dojoWidget) { alignVerticallyMiddle() }
            widget(kuudraWidget) { alignVerticallyMiddle() }
            widget(reputationWidget) { alignVerticallyMiddle() }
        }
    }

    private fun getReputationWidget(crimsonIsleData: CrimsonIsleData): LayoutElement {
        return PvWidgets.label(
            "Reputation",
            PvLayouts.vertical {
                string(
                    Text.of {
                        append("Faction: ")
                        append(crimsonIsleData.selectedFaction?.displayName() ?: Text.of("None :c") { this.color = PvColors.YELLOW })
                    },
                )
                crimsonIsleData.factionReputation.forEach { (faction, rep) ->
                    string(
                        Text.of {
                            append(faction.displayName())
                            append(": ")
                            append(rep.toFormattedString())
                            append(CommonComponents.SPACE)
                            append(Text.of(CrimsonIsleCodecs.factionRanks.getFor(rep) ?: "Unknown :c").wrap("(", ")"))
                        },
                    )
                }
                val highestRep = crimsonIsleData.factionReputation.values.max()
                val maxTier = CrimsonIsleCodecs.KuudraCodecs.requirements.entries
                    .sortedByDescending { it.value }.firstOrNull { (_, value) -> value <= highestRep }?.key

                string(
                    Text.of {
                        append("Highest Kuudra: ")
                        if (maxTier == null) {
                            append("None")
                        } else {
                            append(CrimsonIsleCodecs.KuudraCodecs.idNameMap[maxTier] ?: "Unknown :c")
                        }
                    },
                )
            },
        )
    }

    private fun getDojoStats(stats: List<DojoEntry>): LayoutElement {
        return PvWidgets.label(
            "Dojo Stats",
            PvLayouts.vertical {
                val totalPoints = stats.sumOf { it.points.coerceAtLeast(0) }.takeUnless { stats.all { it.points == -1 } } ?: -1
                stats.forEach { (points, id, _) ->
                    val name = CrimsonIsleCodecs.DojoCodecs.idNameMap.getOrDefault(id, id)
                    string(
                        Text.of {
                            append("$name: ${points.takeUnless { it == -1 } ?: "None"} (")
                            append(CrimsonIsleCodecs.DojoCodecs.grades.getFor(points.coerceAtLeast(0)) ?: Text.of("Error") { this.color = PvColors.RED })
                            append(")")
                        },
                    )
                }
                string(
                    Text.of {
                        append("Total points: ${totalPoints.takeUnless { it == -1 } ?: "None"} (")
                        append(
                            CrimsonIsleCodecs.DojoCodecs.belts.getFor(totalPoints.coerceAtLeast(0))?.value?.hoverName ?: Text.of("Error") {
                                this.color = PvColors.RED
                            },
                        )
                        append(")")
                    },
                )
            },
        )
    }

    private fun getKuudraStats(stats: List<KuudraEntry>): LayoutElement {
        return PvWidgets.label(
            "Kuudra Stats",
            PvLayouts.vertical {
                stats.forEach { string(createKuudraStat(it)) }
                spacer(height = 3)
                val kuudraCollection = stats.mapIndexed { index, it -> it.completions * (index + 1) }.sum()
                val currentCollection = CrimsonIsleCodecs.KuudraCodecs.collection.sortedByDescending { it }.indexOfFirst { it <= kuudraCollection }.plus(1)
                val maxCollection = CrimsonIsleCodecs.KuudraCodecs.collection.size

                string("Total Runs: ${stats.sumOf { it.completions }.toFormattedString()}")
                string("Collection: ${kuudraCollection.toFormattedString()} (${currentCollection.toFormattedString()}/${maxCollection})")

                display(
                    ExtraDisplays.text(
                        "Highest Wave: ${stats.maxOf { it.highestWave }.toFormattedString()}",
                        color = { PvColors.DARK_GRAY.toUInt() },
                        shadow = false,
                    ).withTooltip {
                        stats.map { "${CrimsonIsleCodecs.KuudraCodecs.idNameMap[it.id] ?: it.id}: ${it.highestWave.toFormattedString()}" }
                            .forEach { add(it) }
                    },
                )
            },
        )
    }

    private fun createKuudraStat(kuudraEntry: KuudraEntry): String {
        val (_, completions, id) = kuudraEntry
        val name = CrimsonIsleCodecs.KuudraCodecs.idNameMap[id] ?: id

        return "$name: ${completions.toFormattedString()}"
    }
}
