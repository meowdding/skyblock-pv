package me.owdding.skyblockpv.utils

import me.owdding.ktcodecs.FieldName
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktcodecs.IncludedCodec
import me.owdding.ktcodecs.NamedCodec
import me.owdding.ktmodules.Module
import me.owdding.lib.cosmetics.CosmeticManager
import me.owdding.lib.events.CosmeticLoadEvent
import me.owdding.lib.extensions.associateNotNull
import me.owdding.lib.rendering.text.builtin.GradientTextShader
import me.owdding.skyblockpv.generated.SkyBlockPVCodecs
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TextColor
import net.minecraft.world.entity.animal.parrot.Parrot
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.utils.json.Json.toData
import java.net.URI
import java.util.*

@Module
object ContributorHandler {
    var contributors: MutableMap<UUID, ContributorData> = mutableMapOf()
        private set

    @Subscription
    fun onCostmeticLoad(event: CosmeticLoadEvent) {
        val contributorData = CosmeticManager.playerList.associateNotNull(
            keySelector = { it.uuid },
            valueSelector = { playerEntry ->
                playerEntry.data.toData(SkyBlockPVCodecs.getCodec<ContributorData>()).takeUnless { data -> data == ContributorData.EMPTY }
            },
        )
        contributors.putAll(contributorData)
    }

    @IncludedCodec(named = "cosmetic_url")
    val COSMETIC_URL = CosmeticManager.COSMETIC_URL
}

@GenerateCodec
data class ContributorData(
    @NamedCodec("component_tag") @FieldName("pv:title") val title: Component?,
    @FieldName("pv:parrot") val parrot: ParrotOnShoulder?,
    @NamedCodec("cosmetic_url") @FieldName("pv:cat_texture") val cat: URI?,
    @FieldName("pv:shaking") val shaking: Boolean = false,
    @FieldName("pv:title_colors") val tileColors: List<TextColor>?,
) {
    val titleShader = tileColors?.let { GradientTextShader(*it.toTypedArray()) }

    companion object {
        val EMPTY = ContributorData(null, null, null, false, null)
    }
}

@GenerateCodec
data class ParrotOnShoulder(val variant: Parrot.Variant, @FieldName("left_shoulder") val leftSide: Boolean)
