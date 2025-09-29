package me.owdding.skyblockpv.api

import com.google.gson.JsonObject
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.utils.ChatUtils.sendWithPrefix
import me.owdding.skyblockpv.utils.Utils.unaryPlus
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McLevel
import tech.thatgravyboat.skyblockapi.utils.extentions.toLongValue
import tech.thatgravyboat.skyblockapi.utils.http.Http
import tech.thatgravyboat.skyblockapi.utils.json.Json
import tech.thatgravyboat.skyblockapi.utils.time.currentInstant
import java.util.*
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant

private const val API_URL = "https://skyblock-pv.thatgravyboat.tech%s"

private const val AUTHENTICATION_FAILED_MESSAGE = "Failed to authenticate with PV API. Please restart the game and try again."

object PvAPI {

    private var key: String? = null
    private var failedToAuth: Boolean = false

    fun isAuthenticated(): Boolean {
        return key != null && !failedToAuth
    }

    suspend fun authenticate(bypassCaches: Boolean = false) {
        try {
            val server = UUID.randomUUID().toString()
            val user = McClient.self.user

            McClient.sessionService.joinServer(user.profileId, user.accessToken, server)

            val response = Http.get(
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
            failedToAuth = false
        } catch (e: Throwable) {
            SkyBlockPv.error("Failed to authenticate with PV API:", e)
            if (McLevel.hasLevel) (+"messages.api.failed_to_authenticate").sendWithPrefix()

            key = null
            failedToAuth = true
        }
    }

    suspend fun get(endpoint: String): Pair<JsonObject, Instant>? {
        if (this.key == null || failedToAuth) {
            SkyBlockPv.error(AUTHENTICATION_FAILED_MESSAGE)
            return null
        }

        val response = Http.get(
            url = API_URL.format(endpoint),
            headers = mapOf(
                "User-Agent" to "SkyBlockPV (${SkyBlockPv.version.friendlyString})",
                "Authorization" to this.key!!,
            ),
            handler = { this },
        )

        if (response.isOk) {
            val expireTime = response.headers["X-Backend-Expire-In"]?.first()?.toLongValue() ?: CACHE_TIME
            return response.asJson<JsonObject>(Json.gson) to currentInstant() + expireTime.milliseconds
        } else if (response.statusCode == 401) {
            authenticate()
            if (this.key != null) {
                return get(endpoint)
            } else {
                failedToAuth = true
                SkyBlockPv.error(AUTHENTICATION_FAILED_MESSAGE)
            }
        } else {
            SkyBlockPv.error("Failed to fetch data from Hypixel API: ${response.statusCode}")
        }

        return null
    }
}
