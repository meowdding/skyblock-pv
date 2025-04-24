package me.owdding.skyblockpv.api

import com.google.gson.JsonObject
import me.owdding.skyblockpv.SkyBlockPv
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.http.Http
import java.util.*

private const val API_URL = "https://skyblock-pv.thatgravyboat.tech%s"
// TODO im lazy and dont want to reauth after a day

object HypixelAPI {

    private var key: String? = null

    suspend fun authenticate() {
        val server = UUID.randomUUID().toString()
        val user = McClient.self.user

        McClient.self.minecraftSessionService.joinServer(user.profileId, user.accessToken, server)

        val response = Http.get<String>(
            url = API_URL.format("/authenticate"),
//             queries = mapOf(
//                 "bypassCache" to "true"
//             ),
            headers = mapOf(
                "User-Agent" to "SkyBlockPV (${SkyBlockPv.version.friendlyString})",
                "x-minecraft-username" to user.name,
                "x-minecraft-server" to server,
            ),
        ) { if (isOk) asText() else "" }

        key = response.takeIf { it.isNotEmpty() }
    }

    suspend fun get(endpoint: String): JsonObject? {
        if (this.key == null) {
            SkyBlockPv.error("Hypixel API key is not set. Please authenticate first.")
            return null
        }

        val response = Http.getResult<JsonObject>(
            url = API_URL.format(endpoint),
            headers = mapOf(
                "User-Agent" to "SkyBlockPV (${SkyBlockPv.version.friendlyString})",
                "Authorization" to this.key!!,
            ),
        ).map { it.takeIf { it.get("success")?.asBoolean == true } }

        if (response.isFailure) {
            SkyBlockPv.error("Failed to get data from Hypixel API:", response.exceptionOrNull())
            return null
        }

        return response.getOrNull()
    }
}
