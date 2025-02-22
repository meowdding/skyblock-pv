package tech.thatgravyboat.skyblockpv.data

import net.minecraft.resources.ResourceLocation
import tech.thatgravyboat.skyblockpv.Init
import tech.thatgravyboat.skyblockpv.api.SkillAPI

fun getSkillLevel(skill: String, exp: Long): Int {
    val maxLevel = SkillAPI.skillData.firstNotNullOfOrNull { (name, data) ->
        if (convertFromPlayerApiSkillName(skill).equals(name, true)) data.maxLevel else null
    }
    if (maxLevel == null) return 0
    return (SkillAPI.skillLevels.entries.lastOrNull { it.value < exp }?.key ?: 0).coerceAtMost(maxLevel)
}

fun getIconFromSkillName(name: String): ResourceLocation = Init.id(
    when (name) {
        "SKILL_COMBAT" -> "icon/skill/combat"
        "SKILL_FARMING" -> "icon/skill/farming"
        "SKILL_FISHING" -> "icon/skill/fishing"
        "SKILL_MINING" -> "icon/skill/mining"
        "SKILL_FORAGING" -> "icon/skill/foraging"
        "SKILL_ENCHANTING" -> "icon/skill/enchanting"
        "SKILL_ALCHEMY" -> "icon/skill/alchemy"
        "SKILL_TAMING" -> "icon/skill/taming"
        "SKILL_SOCIAL" -> "icon/skill/social"
        "SKILL_RUNECRAFTING" -> "icon/skill/runecrafting"
        "SKILL_CARPENTRY" -> "icon/skill/carpentry"
        else -> "icon/questionmark"
    },
)

private fun convertFromPlayerApiSkillName(name: String) = name.split("_").drop(1).joinToString("_").lowercase()


data class SkillData(
    val name: String,
    val maxLevel: Int,
)

