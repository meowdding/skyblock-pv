package me.owdding.skyblockpv.api.data.shared.types

import me.owdding.ktcodecs.FieldName
import me.owdding.ktcodecs.GenerateCodec

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
