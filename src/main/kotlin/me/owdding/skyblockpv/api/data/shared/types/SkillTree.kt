package me.owdding.skyblockpv.api.data.shared.types

import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktcodecs.OptionalBoolean
import me.owdding.ktcodecs.OptionalInt

@GenerateCodec
data class SkillTreeNode(
    val id: String,
    @OptionalInt val level: Int,
    @OptionalBoolean(false) val disabled: Boolean,
)
