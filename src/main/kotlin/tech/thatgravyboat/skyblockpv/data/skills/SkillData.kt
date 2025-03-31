package tech.thatgravyboat.skyblockpv.data.skills

data class SkillData(
    val name: String,
    val maxLevel: Int,
    val skillLevels: Map<Int, Long>,
)

