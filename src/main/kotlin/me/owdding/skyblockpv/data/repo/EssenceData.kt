package me.owdding.skyblockpv.data.repo

import com.google.gson.annotations.SerializedName
import me.owdding.ktmodules.Module
import me.owdding.lib.builder.LayoutBuilder
import me.owdding.lib.displays.Displays
import me.owdding.skyblockpv.api.data.SkyBlockProfile
import me.owdding.skyblockpv.utils.Utils
import me.owdding.skyblockpv.utils.displays.withTranslatedTooltip
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

data class RepoEssenceData(
    val shops: Map<String, Map<String, RepoEssencePerk>>,
)

data class RepoEssencePerk(
    val name: String,
    @SerializedName("max_level") val maxLevel: Int,
)

@Module
object EssenceData {
    var allPerks: Map<String, RepoEssencePerk>
        private set

    init {
        allPerks = Utils.loadFromRepo<RepoEssenceData>("essence_perks")
            ?.let(RepoEssenceData::shops)
            ?.flatMap { it.value.entries }
            ?.associate { it.key to it.value } ?: emptyMap()
    }

    fun LayoutBuilder.addFishingPerk(profile: SkyBlockProfile, id: String) {
        addPerk(profile, id, "fishing")
    }

    fun LayoutBuilder.addMiningPerk(profile: SkyBlockProfile, id: String) {
        addPerk(profile, id, "mining")
    }

    fun LayoutBuilder.addPerk(profile: SkyBlockProfile, id: String, category: String) {
        val perkLevel = profile.essenceUpgrades[id] ?: 0
        val perk = allPerks.entries.find { it.key == id }?.value
        val maxLevel = perk?.maxLevel ?: 0

        val display = Displays.text(
            Text.join(
                perk?.name,
                ": ",
                Text.of("$perkLevel") { this.color = if (perkLevel == maxLevel) TextColor.GREEN else TextColor.RED },
                "/$maxLevel",
            ),
            { TextColor.DARK_GRAY.toUInt() },
            false,
        )
        display(display.withTranslatedTooltip("skyblockpv.essence.$category.$id"))
    }
}
