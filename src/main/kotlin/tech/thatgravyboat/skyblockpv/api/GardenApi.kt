package tech.thatgravyboat.skyblockpv.api

import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.data.GardenProfile
import tech.thatgravyboat.skyblockpv.utils.ChatUtils
import java.util.*

private const val PATH = "v2/skyblock/garden"
private const val CACHE_TIME = 10 * 60 * 1000 // 10 minutes

object GardenApi {

    private val cache: MutableMap<UUID, CacheEntry<Result<GardenProfile>>> = mutableMapOf()

    suspend fun getGardenData(profile: SkyBlockProfile): Result<GardenProfile> = cache.getOrPut(profile.id.id) {
        val result = HypixelAPI.get(PATH, mapOf("profile" to profile.id.id.toString())) ?: run {
            ChatUtils.chat("Something went wrong :3")
            return@getOrPut CacheEntry(Result.failure(RuntimeException("Something went wrong :3")))
        }

        return@getOrPut CacheEntry(Result.success(GardenProfile.fromJson(result.getAsJsonObject("garden"))))
    }.takeIf { System.currentTimeMillis() - it.timestamp < CACHE_TIME }?.data ?: run {
        cache.remove(profile.id.id)
        getGardenData(profile)
    }

    fun clearCache() {
        cache.clear()
    }
}
