package me.owdding.skyblockpv.api.data.shared.types

import me.owdding.ktcodecs.FieldName
import me.owdding.ktcodecs.GenerateCodec

@GenerateCodec
data class GardenChip(
    val consumed: Int,
    val level: Int
)

@GenerateCodec
data class GardenChips(
    @FieldName("vermin_vaporizer") val verminVaporizer: GardenChip,
    val synthesis: GardenChip,
    val sowledge: GardenChip,
    val mechamind: GardenChip,
    val hypercharge: GardenChip,
    val evergreen: GardenChip,
    val overdrive: GardenChip,
    val cropshot: GardenChip,
    val quickdraw: GardenChip,
    val rarefinder: GardenChip,
)
