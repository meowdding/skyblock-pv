package tech.thatgravyboat.skyblockpv.api

import com.google.gson.JsonObject
import kotlinx.coroutines.runBlocking
import tech.thatgravyboat.skyblockapi.utils.http.Http
import tech.thatgravyboat.skyblockpv.data.SkillData
import tech.thatgravyboat.skyblockpv.utils.asInt
import tech.thatgravyboat.skyblockpv.utils.asLong
import tech.thatgravyboat.skyblockpv.utils.asString

private const val API_URL = "https://api.hypixel.net/v2/resources/skyblock/skills"

object SkillAPI {
    var skillData: Map<String, SkillData> = emptyMap()
        private set
    var skillLevels: Map<Int, Long> = emptyMap()
        private set

    init {
        runBlocking {
            val skills = get()?.getAsJsonObject("skills") ?: return@runBlocking
            skillData = skills.entrySet().associate { (key, value) ->
                key to value.asJsonObject.toSkillData()
            }
            skillLevels = skills.entrySet().first().value.asJsonObject.getAsJsonArray("levels").associate {
                it.asJsonObject.let {
                    it["level"].asInt(0) to it["totalExpRequired"].asLong(0)
                }
            }
        }
    }

    private fun JsonObject.toSkillData() = SkillData(
        name = this["name"].asString(""),
        maxLevel = this["maxLevel"].asInt(0),
    )

    private suspend fun get(): JsonObject? = Http.getResult<JsonObject>(url = API_URL).getOrNull()
}
