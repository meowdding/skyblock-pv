package me.owdding.skyblockpv.api.data.shared.types

import com.mojang.serialization.Codec
import me.owdding.ktcodecs.FieldName
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.skyblockpv.api.data.shared.SharedDataProvider
import me.owdding.skyblockpv.generated.SkyBlockPvCodecs

object MelodyDataProvider : SharedDataProvider<MelodyData> {
    override val endpoint: String = "melody"
    override val codec: Codec<MelodyData> = SkyBlockPvCodecs.MelodyDataCodec.codec()

    override fun create(): MelodyData = TODO()
}

@GenerateCodec
data class MelodyData(
    @FieldName("hymn_to_the_joy") val hymnToTheJoy: Int,
    @FieldName("frere_jacques") val frereJacques: Int,
    @FieldName("amazing_grace") val amazingGrace: Int,
    @FieldName("brahams_lullaby") val brahamsLullaby: Int,
    @FieldName("happy_birthday_to_you") val happyBirthdayToYou: Int,
    val greensleeves: Int,
    val geothermy: Int,
    val minuet: Int,
    @FieldName("joy_to_the_world") val joyToTheWorld: Int,
    @FieldName("godly_imagination") val godlyImagination: Int,
    @FieldName("la_vie_en_rose") val laVieEnRose: Int,
    @FieldName("through_the_campfire") val throughTheCampfire: Int,
    val pachelbel: Int,
)
