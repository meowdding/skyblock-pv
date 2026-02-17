package me.owdding.skyblockpv.api.data.shared.types

import com.mojang.serialization.Codec
import me.owdding.ktcodecs.FieldName
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.skyblockpv.api.data.shared.SharedDataProvider
import me.owdding.skyblockpv.generated.SkyBlockPvCodecs

object ConsumeablesDataProvider : SharedDataProvider<ConsumeablesData> {
    override val endpoint: String = "consumeables"
    override val codec: Codec<ConsumeablesData> = SkyBlockPvCodecs.ConsumeablesDataCodec.codec()

    override fun create(): ConsumeablesData = TODO()
}

@GenerateCodec
data class ConsumeablesData(
    @FieldName("metaphysical_serum") val metaphysicalSerum: Int,
    @FieldName("reaper_peppers") val reaperPeppers: Int,
    @FieldName("wriggling_larva") val wrigglingLarva: Int,
    @FieldName("vial_of_venom") val vialOfVenom: Int,
    @FieldName("festering_maggot") val festeringMaggot: Int,
    val jyrre: Int,
    @FieldName("dark_cocoa") val darkCocoa: Int,
    @FieldName("spotlite") val spotlite: Int,
    @FieldName("dwarven_ore") val dwarvenOre: Int,
    @FieldName("dwarven_block") val dwarvenBlock: Int,
    @FieldName("dwarven_gemstone") val dwarvenGemstone: Int,
    @FieldName("dwarven_metallic") val dwarvenMetallic: Int,
    @FieldName("moby_duck") val mobyDuck: Int,
    @FieldName("brain_food") val brainFood: Int,
    @FieldName("rosewater_flask") val rosewaterFlask: Int,
)
