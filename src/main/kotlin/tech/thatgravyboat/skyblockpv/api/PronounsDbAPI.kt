package tech.thatgravyboat.skyblockpv.api

import com.google.gson.JsonObject
import tech.thatgravyboat.skyblockapi.utils.http.Http
import java.util.*

private const val API_URL = "https://pronoundb.org/api/v2/lookup"

object PronounsDbAPI {
    private val pronouns = mutableMapOf<UUID, List<String>>()

    suspend fun get(uuid: UUID) = pronouns.getOrPut(uuid) {
        val id = uuid.toString()
        Http.getResult<JsonObject>(
            url = API_URL,
            queries = mapOf(
                "ids" to id,
                "platform" to "minecraft",
            ),
        ).getOrNull()?.getAsJsonObject(id)?.getAsJsonObject("sets")?.getAsJsonArray("en")?.map { it.asString } ?: emptyList()
    }
}
