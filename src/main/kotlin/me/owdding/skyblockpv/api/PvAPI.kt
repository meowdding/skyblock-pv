package me.owdding.skyblockpv.api

import com.google.gson.JsonObject
import me.owdding.skyblockpv.SkyBlockPv
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.http.Http
import tech.thatgravyboat.skyblockapi.utils.json.Json
import java.util.*

private const val API_URL = "https://skyblock-pv.thatgravyboat.tech%s"

private const val AUTHENTICATION_FAILED_MESSAGE = "Failed to authenticate with PV API. Please restart the game and try again."

object PvAPI {

    private var key: String? = null
    private var failedReauth: Boolean = false

    fun isAuthenticated(): Boolean {
        return key != null && !failedReauth
    }

    suspend fun authenticate(bypassCaches: Boolean = false) {
        val server = UUID.randomUUID().toString()
        val user = McClient.self.user

        McClient.self.minecraftSessionService.joinServer(user.profileId, user.accessToken, server)

        val response = Http.get<String>(
            url = API_URL.format("/authenticate"),
            queries = if (bypassCaches) {
                mapOf("bypassCache" to "true")
            } else {
                emptyMap()
            },
            headers = mapOf(
                "User-Agent" to "SkyBlockPV (${SkyBlockPv.version.friendlyString})",
                "x-minecraft-username" to user.name,
                "x-minecraft-server" to server,
            ),
        ) { if (isOk) asText() else "" }

        key = response.takeIf { it.isNotEmpty() }
    }

    suspend fun get(endpoint: String): JsonObject? {
        if (this.key == null || failedReauth) {
            SkyBlockPv.error(AUTHENTICATION_FAILED_MESSAGE)
            return null
        }

        val response = Http.get(
            url = API_URL.format(endpoint),
            headers = mapOf(
                "User-Agent" to "SkyBlockPV (${SkyBlockPv.version.friendlyString})",
                "Authorization" to this.key!!,
            ),
            handler = { this }
        )

        if (response.isOk) {
            return response.asJson(Json.gson)
        } else if (response.statusCode == 401) {
            authenticate()
            if (this.key != null) {
                return get(endpoint)
            } else {
                failedReauth = true
                SkyBlockPv.error(AUTHENTICATION_FAILED_MESSAGE)
            }
        } else {
            SkyBlockPv.error("Failed to fetch data from Hypixel API: ${response.statusCode}")
        }

        return null
    }
}
