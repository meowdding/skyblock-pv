package tech.thatgravyboat.skyblockpv.api

import tech.thatgravyboat.skyblockpv.api.data.SkyblockProfile
import java.util.*

private const val PATH = "v2/skyblock/profiles"
private const val CACHE_TIME = 10 * 60 * 1000 // 10 minutes

object ProfileAPI {

    private val cache: MutableMap<String, CacheEntry> = mutableMapOf()

    private fun getCachedProfiles(uuid: UUID): List<SkyblockProfile>? {
        val entry = cache[uuid.toString()] ?: return null
        return if (System.currentTimeMillis() - entry.timestamp < CACHE_TIME) {
            entry.profiles
        } else {
            cache.remove(uuid.toString())
            null
        }
    }

    suspend fun getProfiles(uuid: UUID): List<SkyblockProfile> {
        val cached = getCachedProfiles(uuid)
        if (cached != null) return cached

        val result = HypixelAPI.get(PATH, mapOf("uuid" to uuid.toString())) ?: return emptyList()

        val profiles = result.getAsJsonArray("profiles").mapNotNull { SkyblockProfile.fromJson(it.asJsonObject, uuid) }

        cache[uuid.toString()] = CacheEntry(profiles, System.currentTimeMillis())

        return profiles
    }
}

private class CacheEntry(
    val profiles: List<SkyblockProfile>,
    val timestamp: Long
)
