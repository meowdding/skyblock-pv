package me.owdding.skyblockpv.api

import com.google.gson.JsonObject
import kotlinx.coroutines.runBlocking
import me.owdding.ktmodules.Module
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.utils.ChatUtils.sendWithPrefix
import me.owdding.skyblockpv.utils.Utils.asTranslated
import me.owdding.skyblockpv.utils.Utils.unaryPlus
import net.minecraft.network.chat.Component
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.OnlyOnSkyBlock
import tech.thatgravyboat.skyblockapi.api.events.misc.RegisterCommandsEvent
import tech.thatgravyboat.skyblockapi.api.events.time.TickEvent
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.extentions.toLongValue
import tech.thatgravyboat.skyblockapi.utils.http.Http
import tech.thatgravyboat.skyblockapi.utils.json.Json
import tech.thatgravyboat.skyblockapi.utils.time.currentInstant
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

private const val API_URL = "https://skyblock-pv.thatgravyboat.tech%s"

private const val AUTHENTICATION_FAILED_MESSAGE = "Failed to authenticate with PV API. Please restart the game and try again."

@Module
object PvAPI {

    private var key: String? = null
    private var failedToAuth: Boolean = false
    private var scheduledSendMessage: Component? = null
    private var lastAuthTry = Instant.DISTANT_PAST

    @Subscription
    @OnlyOnSkyBlock
    fun onTick(event: TickEvent) {
        scheduledSendMessage?.let {
            it.sendWithPrefix()
            scheduledSendMessage = null
        }
    }

    @Subscription
    fun onCommand(event: RegisterCommandsEvent) {
        event.registerWithCallback("sbpv refreshauth") {
            val now = currentInstant()
            if (now - lastAuthTry < 1.minutes) {
                val seconds = 60 - (now - lastAuthTry).inWholeSeconds
                "messages.api.auth_cooldown".asTranslated(seconds).sendWithPrefix()
                return@registerWithCallback
            }
            lastAuthTry = now

            (+"messages.api.authenticating").sendWithPrefix()
            CompletableFuture.runAsync {
                runBlocking {
                    authenticate()
                    McClient.runOrNextTick {
                        if (isAuthenticated()) {
                            (+"messages.api.auth_successful").sendWithPrefix()
                        } else {
                            (+"messages.api.auth_failed").sendWithPrefix()
                        }
                    }
                }
            }
        }
    }

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
                    "User-Agent" to "SkyBlockPV/${SkyBlockPv.version.friendlyString}/${McClient.version}",
                    "x-minecraft-username" to user.name,
                    "x-minecraft-server" to server,
                ),
            ) { if (isOk) asText() else "" }

            key = response.takeIf { it.isNotEmpty() }
            failedToAuth = false
        } catch (e: Throwable) {
            SkyBlockPv.error("Failed to authenticate with PV API:", e)
            scheduledSendMessage = +"messages.api.failed_to_authenticate"

            key = null
            failedToAuth = true
        }
    }

    suspend fun get(endpoint: String, intent: String? = null): Pair<JsonObject, Instant>? {
        if (this.key == null || failedToAuth) {
            SkyBlockPv.error(AUTHENTICATION_FAILED_MESSAGE)
            return null
        }

        val response = Http.get(
            url = API_URL.format(endpoint),
            headers = mapOf(
                "User-Agent" to "SkyBlockPV/${SkyBlockPv.version.friendlyString}/${McClient.version}",
                "Authorization" to this.key!!,
                "X-Intent" to (intent ?: "unknown"),
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
