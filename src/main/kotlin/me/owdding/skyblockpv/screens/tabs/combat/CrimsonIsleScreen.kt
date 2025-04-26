package me.owdding.skyblockpv.screens.tabs.combat

import com.mojang.authlib.GameProfile
import me.owdding.lib.builder.LayoutFactory
import me.owdding.lib.displays.DisplayWidget
import me.owdding.lib.displays.withTooltip
import me.owdding.skyblockpv.api.data.SkyBlockProfile
import me.owdding.skyblockpv.data.api.skills.combat.CrimsonIsleData
import me.owdding.skyblockpv.data.api.skills.combat.DojoEntry
import me.owdding.skyblockpv.data.api.skills.combat.KuudraEntry
import me.owdding.skyblockpv.data.repo.CrimsonIsleCodecs
import me.owdding.skyblockpv.data.repo.CrimsonIsleCodecs.getFor
import me.owdding.skyblockpv.utils.LayoutUtils.asScrollable
import me.owdding.skyblockpv.utils.components.PvWidgets
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.client.gui.layouts.LayoutElement
import net.minecraft.network.chat.CommonComponents
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.Text.wrap
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

class CrimsonIsleScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseCombatScreen(gameProfile, profile) {
    override fun getLayout(bg: DisplayWidget): Layout {
        val profile = profile ?: return LayoutFactory.frame { }
        val crimsonIsleData = profile.crimsonIsleData
        if (bg.width < 400) {
            return LayoutFactory.vertical(5) {
                widget(getDojoStats(crimsonIsleData.dojoStats)) { alignHorizontallyCenter() }
                widget(getKuudraStats(crimsonIsleData.kuudraStats)) { alignHorizontallyCenter() }
                widget(getReputationWidget(crimsonIsleData)) { alignHorizontallyCenter() }
            }.asScrollable(bg.width, bg.height)
        }

        return LayoutFactory.horizontal {
            widget(getDojoStats(crimsonIsleData.dojoStats)) { alignVerticallyMiddle() }
            widget(getKuudraStats(crimsonIsleData.kuudraStats)) { alignVerticallyMiddle() }
            widget(getReputationWidget(crimsonIsleData)) { alignVerticallyMiddle() }
        }
    }

    private fun getReputationWidget(crimsonIsleData: CrimsonIsleData): LayoutElement {
        return PvWidgets.label(
            "Reputation",
            LayoutFactory.vertical {
                textDisplay {
                    append("Faction: ")
                    append(crimsonIsleData.selectedFaction?.displayName() ?: Text.of("None :c") { this.color = TextColor.YELLOW })
                }
                crimsonIsleData.factionReputation.forEach { (faction, rep) ->
                    textDisplay {
                        append(faction.displayName())
                        append(": ")
                        append(rep.toFormattedString())
                        append(CommonComponents.SPACE)
                        append(Text.of(CrimsonIsleCodecs.factionRanks.getFor(rep) ?: "Unknown :c").wrap("(", ")"))
                    }
                }
                val highestRep = crimsonIsleData.factionReputation.values.max()
                val maxTier = CrimsonIsleCodecs.KuudraCodecs.requirements.entries
                    .sortedByDescending { it.value }.firstOrNull { (_, value) -> value <= highestRep }?.key

                textDisplay {
                    append("Highest Kuudra: ")
                    if (maxTier == null) {
                        append("None")
                    } else {
                        append(CrimsonIsleCodecs.KuudraCodecs.idNameMap[maxTier] ?: "Unknown :c")
                    }
                }
            },
        )
    }

    private fun getDojoStats(stats: List<DojoEntry>): LayoutElement {
        return PvWidgets.label(
            "Dojo Stats",
            LayoutFactory.vertical {
                val totalPoints = stats.sumOf { it.points.coerceAtLeast(0) }.takeUnless { stats.all { it.points == -1 } } ?: -1
                stats.forEach { (points, id, _) ->
                    val name = CrimsonIsleCodecs.DojoCodecs.idNameMap.getOrDefault(id, id)
                    textDisplay {
                        append("$name: ${points.takeUnless { it == -1 } ?: "None"} (")
                        append(CrimsonIsleCodecs.DojoCodecs.grades.getFor(points.coerceAtLeast(0)) ?: Text.of("Error") { this.color = TextColor.RED })
                        append(")")
                    }
                }
                textDisplay {
                    append("Total points: ${totalPoints.takeUnless { it == -1 } ?: "None"} (")
                    append(
                        CrimsonIsleCodecs.DojoCodecs.belts.getFor(totalPoints.coerceAtLeast(0))?.value?.hoverName ?: Text.of("Error") {
                            this.color = TextColor.RED
                        },
                    )
                    append(")")
                }
            },
        )
    }

    private fun getKuudraStats(stats: List<KuudraEntry>): LayoutElement {
        return PvWidgets.label(
            "Kuudra Stats",
            LayoutFactory.vertical {
                stats.forEach { string(createKuudraStat(it)) }
                spacer(height = 3)
                val kuudraCollection = stats.mapIndexed { index, it -> it.completions * (index + 1) }.sum()
                val currentCollection = CrimsonIsleCodecs.KuudraCodecs.collection.sortedByDescending { it }.indexOfFirst { it <= kuudraCollection }.plus(1)
                val maxCollection = CrimsonIsleCodecs.KuudraCodecs.collection.size

                string("Total Runs: ${stats.sumOf { it.completions }.toFormattedString()}")
                string("Collection: ${kuudraCollection.toFormattedString()} (${currentCollection.toFormattedString()}/${maxCollection})")

                textDisplay(
                    "Highest Wave: ${stats.maxOf { it.highestWave }.toFormattedString()}",
                    displayModifier = {
                        withTooltip {
                            stats.map { "${CrimsonIsleCodecs.KuudraCodecs.idNameMap[it.id] ?: it.id}: ${it.highestWave.toFormattedString()}" }
                                .forEach { add(it) }
                        }
                    },
                    init = {},
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
