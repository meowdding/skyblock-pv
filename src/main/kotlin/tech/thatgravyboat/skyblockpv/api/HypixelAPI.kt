package tech.thatgravyboat.skyblockpv.api

import com.google.gson.JsonObject
import tech.thatgravyboat.skyblockapi.utils.http.Http

private const val API_URL = "https://api.hypixel.net/%s"

object HypixelAPI {

    suspend fun get(endpoint: String, params: Map<String, String>): JsonObject? {
        return Http.getResult<JsonObject>(
            url = API_URL.format(endpoint),
            queries = params,
            headers = mapOf("API-Key" to "feb64670-351b-46d2-a728-458b27085f7a"),
        ).map { it.takeIf { it.get("success")?.asBoolean == true } }.getOrNull()
    }
}
