package me.owdding.skyblockpv.data.repo

import com.mojang.serialization.Codec
import me.owdding.ktcodecs.FieldName
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktmodules.Module
import me.owdding.lib.builder.LayoutBuilder
import me.owdding.lib.displays.Displays
import me.owdding.skyblockpv.api.data.SkyBlockProfile
import me.owdding.skyblockpv.utils.Utils
import me.owdding.skyblockpv.utils.codecs.CodecUtils
import me.owdding.skyblockpv.utils.displays.withTranslatedTooltip
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

@GenerateCodec
data class RepoEssencePerk(
    val name: String,
    @FieldName("max_level") val maxLevel: Int,
)

@Module
object EssenceData {
    val allPerks: Map<String, RepoEssencePerk>

    init {
        allPerks = Utils.loadRepoData<Map<String, Map<String, RepoEssencePerk>>>("essence_perks") {
            Codec.unboundedMap(Codec.STRING, CodecUtils.map<String, RepoEssencePerk>())
        }.flatMap { it.value.entries }.associateBy({ it.key }, { it.value })
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
