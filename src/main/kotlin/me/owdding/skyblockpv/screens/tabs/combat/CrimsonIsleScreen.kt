package tech.thatgravyboat.skyblockpv.screens.tabs.combat

import com.mojang.authlib.GameProfile
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.client.gui.layouts.LayoutElement
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.data.api.skills.combat.KuudraEntry
import tech.thatgravyboat.skyblockpv.data.repo.CrimsonIsleCodecs
import tech.thatgravyboat.skyblockpv.utils.LayoutBuild
import tech.thatgravyboat.skyblockpv.utils.components.PvWidgets
import tech.thatgravyboat.skyblockpv.utils.displays.DisplayWidget

class CrimsonIsleScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseCombatScreen(gameProfile, profile) {
    override fun getLayout(bg: DisplayWidget): Layout {
        val profile = profile ?: return LayoutBuild.frame { }
        val crimsonIsleData = profile.crimsonIsleData
        return LayoutBuild.vertical {
            widget(getKuudraStats(crimsonIsleData.kuudraStats))
        }
    }

    private fun getKuudraStats(stats: List<KuudraEntry>): LayoutElement {
        return PvWidgets.label(
            "Kuudra Stats",
            LayoutBuild.vertical {
                stats.forEach { string(createKuudraStat(it)) }
                spacer(height = 3)
                string("Total Runs: ${stats.sumOf { it.completions }.toFormattedString()}")
                string("Collection: ${stats.mapIndexed { index, it -> it.completions * (index + 1) }.sum().toFormattedString()}")
                string("Highest Wave: ${stats.maxOf { it.highestWave }.toFormattedString()}")
            },
        )
    }

    private fun createKuudraStat(kuudraEntry: KuudraEntry): String {
        val (_, completions, id) = kuudraEntry
        val name = CrimsonIsleCodecs.KuudraCodecs.idNameMap[id] ?: "<unknown>"

        return "$name: ${completions.toFormattedString()}"
    }
}
