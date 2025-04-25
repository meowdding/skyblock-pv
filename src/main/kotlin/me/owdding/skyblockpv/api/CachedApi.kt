package me.owdding.skyblockpv.api

import com.google.gson.JsonObject
import me.owdding.skyblockpv.utils.ChatUtils


private const val CACHE_TIME = 10 * 60 * 1000 // 10 minutes

abstract class CachedApi<D, V, K> {

    private val cache: MutableMap<K, CacheEntry<Result<V>>> = mutableMapOf()

    suspend fun getData(data: D): Result<V> = cache.getOrPut(getKey(data)) {
        val result = PvAPI.get(path(data)) ?: run {
            ChatUtils.chat("Something went wrong fetching the status from Hypixel. Report this on the Discord!")
            return@getOrPut CacheEntry(Result.failure(RuntimeException("Something went wrong")))
        }

        return@getOrPut CacheEntry(decode(result, data)?.let { Result.success(it) } ?: run { Result.failure(RuntimeException("Something went wrong!")) })
    }.takeIf { System.currentTimeMillis() - it.timestamp < CACHE_TIME }?.data ?: run {
        cache.remove(getKey(data))
        getData(data)
    }

    abstract fun path(data: D): String
    abstract fun getKey(data: D): K
    abstract fun decode(data: JsonObject, originalData: D): V?

    fun clearCache() {
        cache.clear()
    }

    internal class CacheEntry<T>(
        val data: T,
        val timestamp: Long = System.currentTimeMillis(),
    )
}
