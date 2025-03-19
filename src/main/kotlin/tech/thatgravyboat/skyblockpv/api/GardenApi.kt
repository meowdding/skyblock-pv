package tech.thatgravyboat.skyblockpv.api

import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.data.GardenData
import tech.thatgravyboat.skyblockpv.utils.ChatUtils
import java.util.*
import java.util.concurrent.CompletableFuture

private const val PATH = "v2/skyblock/garden"
private const val CACHE_TIME = 10 * 60 * 1000 // 10 minutes

object GardenApi {

    private val cache: MutableMap<UUID, CacheEntry<CompletableFuture<GardenData>>> = mutableMapOf()

    suspend fun getGardenData(profile: SkyBlockProfile): CompletableFuture<GardenData> = cache.getOrPut(profile.id.id) {
        val result = HypixelAPI.get(PATH, mapOf("profile" to profile.id.id.toString())) ?: run {
            ChatUtils.chat("Something went wrong :3")
            return@getOrPut CacheEntry(CompletableFuture.failedFuture(RuntimeException("Something went wrong :3")))
        }

        return@getOrPut CacheEntry(CompletableFuture.completedFuture(GardenData.fromJson(result.getAsJsonObject("garden"))))
    }.takeIf { System.currentTimeMillis() - it.timestamp < CACHE_TIME }?.data ?: run {
        cache.remove(profile.id.id)
        getGardenData(profile)
    }

    fun clearCache() {
        cache.clear()
    }
}
