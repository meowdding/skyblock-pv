package me.owdding.skyblockpv.api.data.shared.types

import com.mojang.serialization.Codec
import me.owdding.ktcodecs.FieldName
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.skyblockpv.api.data.shared.SharedDataProvider
import me.owdding.skyblockpv.generated.SkyBlockPvCodecs

object GardenChipsDataProvider : SharedDataProvider<GardenChipsData> {
    override val endpoint: String = "garden_chips"
    override val codec: Codec<GardenChipsData> = SkyBlockPvCodecs.GardenChipsDataCodec.codec()

    override fun create(): GardenChipsData = TODO()
}

@GenerateCodec
data class GardenChip(
    val consumed: Int,
    val level: Int
)

@GenerateCodec
data class GardenChipsData(
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
