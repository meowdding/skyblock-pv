package tech.thatgravyboat.skyblockpv.api

import com.google.gson.JsonObject
import kotlinx.coroutines.runBlocking
import tech.thatgravyboat.skyblockapi.utils.http.Http
import tech.thatgravyboat.skyblockpv.utils.asInt
import tech.thatgravyboat.skyblockpv.utils.asLong
import tech.thatgravyboat.skyblockpv.utils.asString

private const val API_URL = "https://api.hypixel.net/v2/resources/skyblock/skills"

object SkillAPI {
    var skillData: Map<String, SkillData> = emptyMap()
        private set

    init {
        runBlocking {
            skillData = get()?.getAsJsonObject("skills")?.entrySet()?.associate { (key, value) ->
                key to value.asJsonObject.toSkillData()
            } ?: emptyMap()
            println(skillData)
        }
    }

    private fun JsonObject.toSkillData() = SkillData(
        name = this["name"].asString(""),
        description = this["description"].asString(""),
        maxLevel = this["maxLevel"].asInt(0),
        levels = getAsJsonArray("levels").mapNotNull { it.asJsonObject.toSkillLevel() },
    )

    private fun JsonObject.toSkillLevel() = SkillLevel(
        level = this["level"].asInt(0),
        totalExpRequired = this["totalExpRequired"].asLong(0),
        //unlocks = getAsJsonArray("unlocks").mapNotNull { it.asString },
    )

    private suspend fun get(): JsonObject? = Http.getResult<JsonObject>(url = API_URL).getOrNull()
}

data class SkillData(
    val name: String,
    val description: String,
    val maxLevel: Int,
    val levels: List<SkillLevel>,
)

data class SkillLevel(
    val level: Int,
    val totalExpRequired: Long,
    //val unlocks: List<String>,
)
