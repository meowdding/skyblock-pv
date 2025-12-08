package me.owdding.skyblockpv.api

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.config.DevConfig
import me.owdding.skyblockpv.utils.ChatUtils.sendWithPrefix
import me.owdding.skyblockpv.utils.Utils.hash
import me.owdding.skyblockpv.utils.Utils.unaryPlus
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.json.Json.toPrettyString
import java.nio.file.StandardOpenOption
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText


const val CACHE_TIME = 5 * 60 * 1000L // 5 minutes
const val TIMEOUT_TIME = 30 * 1000L // 30 seconds

abstract class CachedApi<D, V, K>(val maxCache: Long = CACHE_TIME) {

    private val cache: MutableMap<K, CacheEntry<Result<V>>> = mutableMapOf()
    private val requests: MutableMap<K, OutgoingRequest<V>> = mutableMapOf()

    /**
     * Gets the cached data if it exists and is not expired.
     */
    fun getCached(data: D): V? = cache[getKey(data)]
        ?.takeIf { System.currentTimeMillis() - it.timestamp < maxCache }
        ?.data
        ?.getOrNull()

    /**
     * Gets from the cache or fetches data from the API if not cached or expired.
     */
    suspend fun getData(data: D, intent: String? = null): Result<V> = cache.getOrPut(getKey(data)) {
        val path = path(data)
        val hash = path.hash()
        val cachedFile = SkyBlockPv.configDir.resolve("cache").resolve(hash)
        if (DevConfig.offlineMode && cachedFile.exists()) {
            return@getOrPut CacheEntry(
                runCatching {
                    decode(JsonParser.parseString(cachedFile.readText()).asJsonObject!!, data)!!
                },
            )
        }

        val (result, expire) = PvAPI.get(path, intent) ?: run {
            (+"messages.api.something_went_wrong").sendWithPrefix()
            return@getOrPut CacheEntry(Result.failure(RuntimeException("Something went wrong for $path")))
        }

        if (SkyBlockPv.isDevMode) {
            SkyBlockPv.info("Set to expire in ${(expire.toEpochMilliseconds() - System.currentTimeMillis()) / 1000}s")
        }

        if (DevConfig.offlineMode) {
            cachedFile.parent.createDirectories()
            cachedFile.writeText(result.toPrettyString(), Charsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)
            SkyBlockPv.info("Wrote cache file $hash")
        }

        return@getOrPut CacheEntry(
            runCatching {
                val output: V?
                if (SkyBlockPv.isDevMode) {
                    val startTime = System.currentTimeMillis()
                    output = decode(result, data) ?: throw RuntimeException("Failed to decode data: $result")
                    SkyBlockPv.info("Finished parsing '$path' in ${System.currentTimeMillis() - startTime}ms")
                } else {
                    output = decode(result, data) ?: throw RuntimeException("Failed to decode data: $result")
                }

                output
            },
            expire.toEpochMilliseconds() - maxCache,
        )
    }.takeUnless { it.isExpired() }?.data ?: run {
        cache.remove(getKey(data))
        getData(data)
    }

    fun getDataAsync(data: D, intent: String? = null, handler: (Result<V>) -> Unit) {
        val cached = getCached(data)
        if (cached != null) {
            handler(Result.success(cached))
        } else {
            val entry = requests[getKey(data)]
            if (entry != null && !entry.completed.get() && !entry.isExpired()) {
                entry.handlers.add(handler)
            } else {
                entry?.handlers?.clear()

                val request = OutgoingRequest<V>()
                request.handlers.add(handler)

                CoroutineScope(Dispatchers.IO).launch {
                    val result = getData(data, intent)
                    request.completed.set(true)
                    McClient.runNextTick {
                        request.handlers.forEach { it.invoke(result) }
                        request.handlers.clear()
                    }
                }
                requests[getKey(data)] = request
            }
        }
    }

    abstract fun path(data: D): String
    abstract fun getKey(data: D): K
    abstract fun decode(data: JsonObject, originalData: D): V?

    fun clearCache() {
        cache.clear()
    }

    internal inner class CacheEntry<T>(
        val data: T,
        val timestamp: Long = System.currentTimeMillis(),
    ) {
        fun isExpired() = System.currentTimeMillis() - timestamp >= maxCache
    }

    internal class OutgoingRequest<T>(
        val handlers: MutableList<(Result<T>) -> Unit> = CopyOnWriteArrayList(),
        val completed: AtomicBoolean = AtomicBoolean(false),
        val timestamp: Long = System.currentTimeMillis(),
    ) {
        fun isExpired() = System.currentTimeMillis() - timestamp >= TIMEOUT_TIME
    }
}
