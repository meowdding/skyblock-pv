package tech.thatgravyboat.skyblockpv.api

import com.google.gson.JsonObject
import kotlinx.coroutines.runBlocking
import net.minecraft.resources.ResourceLocation
import tech.thatgravyboat.skyblockapi.utils.extentions.asInt
import tech.thatgravyboat.skyblockapi.utils.extentions.asLong
import tech.thatgravyboat.skyblockapi.utils.extentions.asString
import tech.thatgravyboat.skyblockapi.utils.http.Http
import tech.thatgravyboat.skyblockpv.SkyBlockPv
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.data.SkillData

private const val API_URL = "https://api.hypixel.net/v2/resources/skyblock/skills"

object SkillAPI {
    var skillData: Map<String, SkillData> = emptyMap()
        private set

    fun getProgressToNextLevel(skill: String, exp: Long, profile: SkyBlockProfile): Float {
        val skillData = skillData.firstNotNullOfOrNull { (name, data) ->
            if (convertFromPlayerApiSkillName(skill).equals(name, true)) data else null
        } ?: return 0f
        val currentLevel = getSkillLevel(skill, exp, profile)
        val nextLevel = (currentLevel + 1).coerceAtMost(skillData.maxLevel)
        if (currentLevel == skillData.maxLevel) return 1f
        val currentExp = skillData.skillLevels[currentLevel] ?: return 0f
        val nextExp = skillData.skillLevels[nextLevel] ?: return 1f
        return (exp - currentExp).toFloat() / (nextExp - currentExp)
    }

    fun hasFloatingLevelCap(skill: String): Boolean {
        return when (skill) {
            "SKILL_TAMING", "SKILL_FARMING" -> true
            else -> false
        }
    }

    fun getMaxSkillLevel(skill: String, profile: SkyBlockProfile): Int? {
        return when (skill) {
            "SKILL_TAMING" -> profile.tamingLevelPetsDonated.size + 50
            "SKILL_FARMING" -> profile.farmingData.perks.farmingLevelCap + 50
            else -> skillData.firstNotNullOfOrNull { (name, data) ->
                if (convertFromPlayerApiSkillName(skill).equals(name, true)) data.maxLevel else null
            }
        }
    }

    fun getSkillLevel(skill: String, exp: Long, profile: SkyBlockProfile): Int {
        val maxLevel = getMaxSkillLevel(skill, profile) ?: return 0
        return (getLevelingCurveForSkill(skill).entries.lastOrNull { it.value < exp }?.key ?: 0).coerceAtMost(maxLevel)
    }

    private fun getLevelingCurveForSkill(skill: String): Map<Int, Long> {
        return skillData.firstNotNullOfOrNull { (name, data) ->
            if (convertFromPlayerApiSkillName(skill).equals(name, true)) data.skillLevels else null
        } ?: emptyMap()
    }

    fun getIconFromSkillName(name: String): ResourceLocation = SkyBlockPv.id(
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

    init {
        runBlocking {
            val skills = get()?.getAsJsonObject("skills") ?: return@runBlocking
            skillData = skills.entrySet().associate { (key, value) ->
                key to value.asJsonObject.toSkillData()
            }
        }
    }

    private fun JsonObject.toSkillData() = SkillData(
        name = this["name"].asString(""),
        maxLevel = this["maxLevel"].asInt(0),
        skillLevels = this.getAsJsonArray("levels").associate { it.asJsonObject.let { it["level"].asInt(0) to it["totalExpRequired"].asLong(0) } },
    )

    private suspend fun get(): JsonObject? = Http.getResult<JsonObject>(url = API_URL).getOrNull()
}
