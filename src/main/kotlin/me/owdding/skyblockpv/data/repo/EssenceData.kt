package me.owdding.skyblockpv.data.repo

import com.mojang.serialization.Codec
import me.owdding.ktcodecs.FieldName
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.lib.builder.LayoutBuilder
import me.owdding.skyblockpv.api.data.SkyBlockProfile
import me.owdding.skyblockpv.utils.Utils
import me.owdding.skyblockpv.utils.codecs.CodecUtils
import me.owdding.skyblockpv.utils.codecs.ExtraData
import me.owdding.skyblockpv.utils.codecs.LoadData
import me.owdding.skyblockpv.utils.displays.ExtraDisplays
import me.owdding.skyblockpv.utils.displays.withTranslatedTooltip
import me.owdding.skyblockpv.utils.theme.PvColors
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

@GenerateCodec
data class RepoEssencePerk(
    val name: String,
    @FieldName("max_level") val maxLevel: Int,
)

@LoadData
object EssenceData : ExtraData {
    lateinit var allPerks: Map<String, RepoEssencePerk>
        private set

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

        val display = ExtraDisplays.text(
            Text.join(
                perk?.name,
                ": ",
                Text.of("$perkLevel") { this.color = if (perkLevel == maxLevel) PvColors.GREEN else PvColors.RED },
                "/$maxLevel",
            ),
            { PvColors.DARK_GRAY.toUInt() },
            false,
        )
        display(display.withTranslatedTooltip("skyblockpv.essence.$category.$id"))
    }

    override suspend fun load() {
        allPerks = Utils.loadRepoData("essence_perks", Codec.unboundedMap(Codec.STRING, CodecUtils.map<String, RepoEssencePerk>()))
            .flatMap { it.value.entries }.associateBy({ it.key }, { it.value })
    }
}
