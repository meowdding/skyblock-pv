package tech.thatgravyboat.skyblockpv.api

import com.google.gson.JsonObject
import tech.thatgravyboat.skyblockapi.utils.http.Http
import java.lang.Long.parseUnsignedLong
import java.util.*

private const val API_URL = "https://api.mojang.com/%s"

object MojangAPI {

    private var uuidCache: MutableMap<String, UUID> = mutableMapOf()

    suspend fun getUUID(name: String): UUID? {
        if (!uuidCache.containsKey(name)) {
            val string = get("users/profiles/minecraft/$name")?.get("id")?.asString ?: return null
            uuidCache[name] = fromDashlessUUID(string)
        }
        return uuidCache[name]
    }

    suspend fun get(endpoint: String): JsonObject? {
        return Http.getResult<JsonObject>(
            url = API_URL.format(endpoint),
        ).getOrNull()
    }

    private fun fromDashlessUUID(uuid: String) = UUID(
        parseUnsignedLong(uuid.substring(0, 16), 16),
        parseUnsignedLong(uuid.substring(16), 16),
    )
}
