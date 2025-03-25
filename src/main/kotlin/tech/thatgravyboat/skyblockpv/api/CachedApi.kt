package tech.thatgravyboat.skyblockpv.api

import com.google.gson.JsonObject
import tech.thatgravyboat.skyblockpv.utils.ChatUtils


private const val CACHE_TIME = 10 * 60 * 1000 // 10 minutes

abstract class CachedApi<D, V, K> {

    private val cache: MutableMap<K, CacheEntry<Result<V>>> = mutableMapOf()

    suspend fun getData(data: D): Result<V> = cache.getOrPut(getKey(data)) {
        val result = HypixelAPI.get(path(), variables(data)) ?: run {
            ChatUtils.chat("Something went wrong :3")
            return@getOrPut CacheEntry(Result.failure(RuntimeException("Something went wrong :3")))
        }

        return@getOrPut CacheEntry(Result.success(decode(result, data)))
    }.takeIf { System.currentTimeMillis() - it.timestamp < CACHE_TIME }?.data ?: run {
        cache.remove(getKey(data))
        getData(data)
    }

    abstract fun path(): String
    abstract fun variables(data: D): Map<String, String>
    abstract fun getKey(data: D): K
    abstract fun decode(data: JsonObject, originalData: D): V

    fun clearCache() {
        cache.clear()
    }

    internal class CacheEntry<T>(
        val data: T,
        val timestamp: Long = System.currentTimeMillis(),
    )
}
