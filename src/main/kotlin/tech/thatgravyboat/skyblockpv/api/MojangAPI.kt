package tech.thatgravyboat.skyblockpv.api

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import tech.thatgravyboat.skyblockapi.utils.http.Http
import java.util.UUID

private const val API_URL = "https://api.mojang.com/%s"

object MojangAPI {
    suspend fun getUUID(name: String): UUID? {
        val string = get("users/profiles/minecraft/$name")?.get("id")?.jsonPrimitive?.content ?: return null
        return UUID.fromString(string)
    }

    suspend fun get(endpoint: String): JsonObject? {
        return Http.getResult<JsonObject>(
            url = API_URL.format(endpoint)
        ).getOrNull()
    }
}
