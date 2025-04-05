package tech.thatgravyboat.skyblockpv.data.repo

import com.google.gson.annotations.SerializedName
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.utils.LayoutBuilder
import tech.thatgravyboat.skyblockpv.utils.Utils
import tech.thatgravyboat.skyblockpv.utils.displays.Displays
import tech.thatgravyboat.skyblockpv.utils.displays.withTranslatedTooltip

data class RepoEssenceData(
    val shops: Map<String, Map<String, RepoEssencePerk>>,
)

data class RepoEssencePerk(
    val name: String,
    @SerializedName("max_level") val maxLevel: Int,
)

object EssenceData {
    var allPerks: Map<String, RepoEssencePerk> = emptyMap()
        private set

    init {
        Utils.loadFromRepo<RepoEssenceData>("essence_perks")
            ?.let(RepoEssenceData::shops)
            ?.flatMap { it.value.entries }
            ?.associate { it.key to it.value }
            ?.let { it -> allPerks = it }
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
        display(display.withTranslatedTooltip("gui.skyblockpv.tab.$category.information.$id.desc"))
    }
}
