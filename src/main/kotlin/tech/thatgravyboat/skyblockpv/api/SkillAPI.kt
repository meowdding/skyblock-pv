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
    var skills: List<Skill> = emptyList()
        private set

    fun getProgressToNextLevel(skill: Skill, exp: Long, profile: SkyBlockProfile): Float {
        val currentLevel = getSkillLevel(skill, exp, profile)
        val nextLevel = (currentLevel + 1).coerceAtMost(skill.maxLevel(profile))
        if (currentLevel == skill.data.maxLevel) return 1f
        val currentExp = skill.data.skillLevels[currentLevel] ?: return 0f
        val nextExp = skill.data.skillLevels[nextLevel] ?: return 1f
        return (exp - currentExp).toFloat() / (nextExp - currentExp)
    }

    fun getSkillLevel(skill: Skill, exp: Long, profile: SkyBlockProfile): Int {
        val maxLevel = skill.maxLevel(profile)
        return skill.data.skillLevels.entries.lastOrNull { it.value < exp }?.key?.coerceAtMost(maxLevel) ?: 0
    }

    fun getSkill(name: String): Skill {
        return this.skills.find { it.id.equals(name, true) || it.skillApiId.equals(name, true) } ?: UnknownSkill(name, SkillData(name, 0, emptyMap()))
    }

    interface Skill {
        val data: SkillData
        val id: String
        val icon: ResourceLocation
        fun maxLevel(profile: SkyBlockProfile): Int = data.maxLevel
        fun hasFloatingLevelCap(): Boolean = false
        val skillApiId
            get() = "SKILL_$id"
    }

    init {
        Skills.initialize()
    }

    enum class Skills : Skill {
        COMBAT,
        FISHING,
        MINING,
        FORAGING,
        ENCHANTING,
        ALCHEMY,
        SOCIAL,
        RUNECRAFTING,
        CARPENTRY,
        FARMING {
            override fun hasFloatingLevelCap() = true
            override fun maxLevel(profile: SkyBlockProfile) = profile.farmingData.perks.farmingLevelCap + 50
        },
        TAMING {
            override fun hasFloatingLevelCap() = true
            override fun maxLevel(profile: SkyBlockProfile) = profile.tamingLevelPetsDonated.size + 50
        };

        private var internalSkillData: SkillData? = null
        override val data: SkillData
            get() = internalSkillData ?: throw UnsupportedOperationException("Internal skill data is not yet supported")
        override val id: String = name
        override val icon: ResourceLocation by lazy { SkyBlockPv.id("icon/skill/${id.lowercase()}") }

        companion object {
            fun initialize() {
                runBlocking {
                    val skills = get()?.getAsJsonObject("skills") ?: return@runBlocking
                    SkillAPI.skills = skills.entrySet().map { (key, value) ->
                        val skillData = value.asJsonObject.toSkillData()

                        runCatching {
                            Skills.valueOf(key).also { skill -> skill.internalSkillData = skillData }
                        }.getOrElse {
                            UnknownSkill(key, skillData)
                        }
                    }
                }
            }
        }
    }

    data class UnknownSkill(override val id: String, override val data: SkillData) : Skill {
        override val icon: ResourceLocation = QUESTIONMARK

        companion object {
            val QUESTIONMARK: ResourceLocation = SkyBlockPv.id("icon/questionmark")
        }
    }

    private fun JsonObject.toSkillData() = SkillData(
        name = this["name"].asString(""),
        maxLevel = this["maxLevel"].asInt(0),
        skillLevels = this.getAsJsonArray("levels").associate { it.asJsonObject.let { it["level"].asInt(0) to it["totalExpRequired"].asLong(0) } },
    )

    private suspend fun get(): JsonObject? = Http.getResult<JsonObject>(url = API_URL).getOrNull()
}
