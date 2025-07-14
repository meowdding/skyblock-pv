package me.owdding.skyblockpv.data.api.skills

data class SkillData(
    val name: String,
    val maxLevel: Int,
    val skillLevels: Map<Int, Long>,
)

