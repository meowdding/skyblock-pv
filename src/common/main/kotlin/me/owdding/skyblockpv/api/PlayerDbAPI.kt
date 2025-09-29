package me.owdding.skyblockpv.api

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.PropertyMap
import com.mojang.authlib.properties.PropertyMap.Serializer
import kotlinx.coroutines.runBlocking
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.utils.Utils.toDashlessString
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.platform.GameProfile
import tech.thatgravyboat.skyblockapi.platform.id
import tech.thatgravyboat.skyblockapi.utils.extentions.asUUID
import tech.thatgravyboat.skyblockapi.utils.http.Http
import java.util.concurrent.CompletableFuture

private const val API_URL = "https://playerdb.co/api/player/minecraft/%s"

object PlayerDbAPI {

    private val cache = mutableMapOf<String, GameProfile?>()

    init {
        val user = McClient.self.user
        val profile = McClient.self.gameProfile
        cache.put(user.name, profile)
        cache.put(profile.id.toDashlessString(), profile)
        cache.put(profile.id.toString(), profile)
    }

    fun getProfileAsync(usernameOrId: String): CompletableFuture<GameProfile?> = CompletableFuture.supplyAsync {
        getProfile(usernameOrId)
    }

    fun getProfile(usernameOrId: String) = cache.getOrPut(usernameOrId) {
        runBlocking {
            val player = get(usernameOrId)?.get("data")?.asJsonObject?.get("player")?.asJsonObject
            val uuid = player?.get("id")?.asUUID()
            val name = player?.get("username")?.asString

            if (uuid != null) {
                val gson = GsonBuilder().registerTypeAdapter(PropertyMap::class.java, Serializer()).create()
                val property = gson.fromJson(player.get("properties"), PropertyMap::class.java)

                GameProfile(name ?: "meow", uuid, property)
            } else {
                null
            }
        }
    }

    private suspend fun get(name: String): JsonObject? {
        return Http.getResult<JsonObject>(
            url = API_URL.format(name),
            headers = mapOf("User-Agent" to SkyBlockPv.useragent),
        ).map { it.takeIf { it.get("success")?.asBoolean == true } }.getOrNull()
    }

}

