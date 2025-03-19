package tech.thatgravyboat.skyblockpv.api

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.PropertyMap
import com.mojang.authlib.properties.PropertyMap.Serializer
import kotlinx.coroutines.runBlocking
import tech.thatgravyboat.skyblockapi.utils.http.Http
import tech.thatgravyboat.skyblockpv.utils.asUUID
import java.util.*

private const val API_URL = "https://playerdb.co/api/player/minecraft/%s"
private val identifier = String(Base64.getDecoder().decode("Y29udGFjdEB0aGF0Z3Jhdnlib2F0LnRlY2g="))

object PlayerDbAPI {

    private val cache = mutableMapOf<String, GameProfile?>()

    fun getUUID(username: String) = cache.getOrPut(username) {
        runBlocking {
            val player = get(username)?.get("data")?.asJsonObject?.get("player")?.asJsonObject
            val uuid = player?.get("id")?.asUUID

            if (uuid != null) {
                val profile = GameProfile(uuid, username)

                val gson = GsonBuilder().registerTypeAdapter(PropertyMap::class.java, Serializer()).create()
                val property = gson.fromJson<PropertyMap?>(player.get("properties"), PropertyMap::class.java)
                profile.properties.putAll(property)

                profile
            } else {
                null
            }
        }
    }

    private suspend fun get(name: String): JsonObject? {
        return Http.getResult<JsonObject>(
            url = API_URL.format(name),
            headers = mapOf("User-Agent" to "SkyBlockPv $identifier"),
        ).map { it.takeIf { it.get("success")?.asBoolean == true } }.getOrNull()
    }

}

