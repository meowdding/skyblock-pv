package me.owdding.skyblockpv.api.data.shared.types

import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktcodecs.Inline

@GenerateCodec
data class AttributeData(
    val syphoned: Int,
    val owned: Int,
)

@GenerateCodec
data class HuntingBox(
    @Inline val data: Map<String, AttributeData>
)

