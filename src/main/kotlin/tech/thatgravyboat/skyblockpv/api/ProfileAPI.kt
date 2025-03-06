package tech.thatgravyboat.skyblockpv.api

import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.Text.send
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import java.util.*

private const val PATH = "v2/skyblock/profiles"
private const val CACHE_TIME = 10 * 60 * 1000 // 10 minutes

object ProfileAPI {

    private val cache: MutableMap<UUID, CacheEntry> = mutableMapOf()

    suspend fun getProfiles(uuid: UUID): List<SkyBlockProfile> = cache.getOrPut(uuid) {
        val result = HypixelAPI.get(PATH, mapOf("uuid" to uuid.toString())) ?: run {
            Text.of("Something went wrong :3").send()
            return emptyList()
        }

        val profiles = result.getAsJsonArray("profiles").mapNotNull {
            SkyBlockProfile.fromJson(it.asJsonObject, uuid)
        }

        CacheEntry(profiles, System.currentTimeMillis())
    }.takeIf { System.currentTimeMillis() - it.timestamp < CACHE_TIME }?.profiles ?: run {
        cache.remove(uuid)
        getProfiles(uuid)
    }
}

private class CacheEntry(
    val profiles: List<SkyBlockProfile>,
    val timestamp: Long,
)
