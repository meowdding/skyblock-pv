package me.owdding.skyblockpv.api

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.config.DevConfig
import me.owdding.skyblockpv.utils.ChatUtils.sendWithPrefix
import me.owdding.skyblockpv.utils.Utils.hash
import me.owdding.skyblockpv.utils.Utils.unaryPlus
import tech.thatgravyboat.skyblockapi.utils.json.Json.toPrettyString
import java.nio.file.StandardOpenOption
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText


private const val CACHE_TIME = 10 * 60 * 1000 // 10 minutes

abstract class CachedApi<D, V, K> {

    private val cache: MutableMap<K, CacheEntry<Result<V>>> = mutableMapOf()

    suspend fun getData(data: D): Result<V> = cache.getOrPut(getKey(data)) {
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

        val result = PvAPI.get(path) ?: run {
            (+"messages.api.something_went_wrong").sendWithPrefix()
            return@getOrPut CacheEntry(Result.failure(RuntimeException("Something went wrong for $path")))
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
        )
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
