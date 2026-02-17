package me.owdding.skyblockpv.api.data.shared.types

import com.mojang.serialization.Codec
import me.owdding.ktcodecs.FieldName
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.skyblockpv.api.SharedData
import me.owdding.skyblockpv.api.data.shared.SharedDataProvider
import me.owdding.skyblockpv.generated.SkyBlockPvCodecs
import tech.thatgravyboat.skyblockapi.api.profile.currency.CurrencyAPI

@SharedData
object MiscGardenDataProvider : SharedDataProvider<MiscGardenData> {
    override val endpoint: String = "garden"
    override val codec: Codec<MiscGardenData> = SkyBlockPvCodecs.MiscGardenDataCodec.codec()

    override fun create(): MiscGardenData = MiscGardenData(
        unlockedGreenhouseTiles = -1,
        growthSpeed = -1,
        plantYield = -1,
        sowdust = CurrencyAPI.sowdust,
        mutations = emptyMap()
    )
}

enum class MutationState {
    UNLOCKED,
    ANALYZED,
    UNKNOWN,
}

@GenerateCodec
data class MiscGardenData(
    @FieldName("unlocked_greenhouse_tiles") val unlockedGreenhouseTiles: Int,
    @FieldName("growth_speed") val growthSpeed: Int,
    @FieldName("plant_yield") val plantYield: Int,
    val sowdust: Long,
    val mutations: Map<String, MutationState>,
)
