package tech.thatgravyboat.skyblockpv.api

import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.utils.ChatUtils
import java.util.*

private const val PATH = "v2/skyblock/profiles"
private const val CACHE_TIME = 10 * 60 * 1000 // 10 minutes

object ProfileAPI {

    private val cache: MutableMap<UUID, CacheEntry<List<SkyBlockProfile>>> = mutableMapOf()

    suspend fun getProfiles(uuid: UUID): List<SkyBlockProfile> = cache.getOrPut(uuid) {
        val result = HypixelAPI.get(PATH, mapOf("uuid" to uuid.toString())) ?: run {
            ChatUtils.chat("Something went wrong :3")
            return emptyList()
        }

        val profiles = result.getAsJsonArray("profiles").mapNotNull {
            SkyBlockProfile.fromJson(it.asJsonObject, uuid)
        }

        CacheEntry(profiles, System.currentTimeMillis())
    }.takeIf { System.currentTimeMillis() - it.timestamp < CACHE_TIME }?.data ?: run {
        cache.remove(uuid)
        getProfiles(uuid)
    }

    fun clearCache() {
        cache.clear()
    }
}

internal class CacheEntry<T>(
    val data: T,
    val timestamp: Long = System.currentTimeMillis(),
)
