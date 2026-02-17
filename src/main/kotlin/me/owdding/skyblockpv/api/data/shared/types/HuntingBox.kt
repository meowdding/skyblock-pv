package me.owdding.skyblockpv.api.data.shared.types

import com.mojang.serialization.Codec
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktcodecs.Inline
import me.owdding.skyblockpv.api.data.shared.SharedDataProvider
import me.owdding.skyblockpv.generated.SkyBlockPvCodecs

object HuntingBoxDataProvider : SharedDataProvider<HuntingBoxData> {
    override val endpoint: String = "hunting_box"
    override val codec: Codec<HuntingBoxData> = SkyBlockPvCodecs.HuntingBoxDataCodec.codec()

    override fun create(): HuntingBoxData = TODO()
}

@GenerateCodec
data class AttributeData(
    val syphoned: Int,
    val owned: Int,
)

@GenerateCodec
data class HuntingBoxData(
    @Inline val data: Map<String, AttributeData>
)

