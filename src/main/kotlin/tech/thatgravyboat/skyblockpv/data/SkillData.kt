package tech.thatgravyboat.skyblockpv.data

import net.minecraft.resources.ResourceLocation
import tech.thatgravyboat.skyblockpv.Init
import tech.thatgravyboat.skyblockpv.api.SkillAPI

fun getSkillLevel(skill: String, exp: Long): Int {
    val maxLevel = SkillAPI.skillData.firstNotNullOf { (name, data) ->
        if (convertFromPlayerApiSkillName(skill).equals(name, true)) data.maxLevel else null
    }
    return (SkillAPI.skillLevels.entries.lastOrNull { it.value < exp }?.key ?: 0).coerceAtMost(maxLevel)
}

fun getIconFromSkillName(name: String): ResourceLocation = when (name) {
    "SKILL_COMBAT" -> Init.id("skill/combat")
    "SKILL_FARMING" -> Init.id("skill/farming")
    "SKILL_FISHING" -> Init.id("skill/fishing")
    "SKILL_MINING" -> Init.id("skill/mining")
    "SKILL_FORAGING" -> Init.id("skill/foraging")
    "SKILL_ENCHANTING" -> Init.id("skill/enchanting")
    "SKILL_ALCHEMY" -> Init.id("skill/alchemy")
    "SKILL_TAMING" -> Init.id("skill/taming")
    "SKILL_SOCIAL" -> Init.id("skill/social")
    "SKILL_RUNECRAFTING" -> Init.id("skill/runecrafting")
    "SKILL_CARPENTRY" -> Init.id("skill/carpentry")
    else -> Init.id("skill/idk")
}

private fun convertFromPlayerApiSkillName(name: String) = name.split("_").drop(1).joinToString("_").lowercase()


data class SkillData(
    val name: String,
    val maxLevel: Int,
)

