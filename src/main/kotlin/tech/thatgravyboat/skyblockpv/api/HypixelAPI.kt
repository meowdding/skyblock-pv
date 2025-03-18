package tech.thatgravyboat.skyblockpv.api

import com.google.gson.JsonObject
import tech.thatgravyboat.skyblockapi.utils.http.Http
import tech.thatgravyboat.skyblockpv.SkyBlockPv

private const val API_URL = "https://hypixel-api.thatgravyboat.tech/%s"

object HypixelAPI {

    suspend fun get(endpoint: String, params: Map<String, String>): JsonObject? {
        return Http.getResult<JsonObject>(
            url = API_URL.format(endpoint),
            queries = params,
            headers = mapOf("User-Agent" to "SkyBlockPV (${SkyBlockPv.version.friendlyString})"),
        ).map { it.takeIf { it.get("success")?.asBoolean == true } }.getOrNull()
    }
}
