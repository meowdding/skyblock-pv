package tech.thatgravyboat.skyblockpv.data

import tech.thatgravyboat.skyblockpv.api.SkillAPI

fun getSkillLevel(exp: Long) = SkillAPI.skillLevels.entries.lastOrNull { it.value < exp }?.key ?: 0

data class SkillData(
    val name: String,
    val maxLevel: Int,
)

