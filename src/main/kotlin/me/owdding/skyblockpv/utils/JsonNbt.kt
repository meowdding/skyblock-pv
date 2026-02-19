package me.owdding.skyblockpv.utils

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtAccounter
import net.minecraft.nbt.NbtIo
import org.intellij.lang.annotations.Language
import tech.thatgravyboat.skyblockapi.utils.extentions.*
import tech.thatgravyboat.skyblockapi.utils.json.Json.toJson
import tech.thatgravyboat.skyblockapi.utils.json.getPath
import java.io.ByteArrayInputStream
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.reflect.KProperty
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

fun JsonObject.getNbt(): CompoundTag = this.asString.getNbt()
fun JsonElement.getNbt(): CompoundTag = this.asString.getNbt()

@OptIn(ExperimentalEncodingApi::class)
fun String.getNbt(): CompoundTag {
    return NbtIo.readCompressed(ByteArrayInputStream(Base64.decode(this)), NbtAccounter.unlimitedHeap())
}

fun JsonObject.getNbtJson(): JsonObject? {
    return this.getNbt().toJson(CompoundTag.CODEC)?.asJsonObject
}

fun JsonElement.getNbtJson(): JsonObject? {
    return this.getNbt().toJson(CompoundTag.CODEC)?.asJsonObject
}

interface DelegateProvider<T> {
    operator fun <This> provideDelegate(thisRef: This, property: KProperty<*>): Lazy<T>
    fun <Target> map(mapper: (T) -> Target) = MappedDelegateProvider(this, mapper)
}

data class MappedDelegateProvider<Source, Target>(
    private val parent: DelegateProvider<Source>,
    private val mapper: (Source) -> Target,
) : DelegateProvider<Target> {
    override fun <This> provideDelegate(thisRef: This, property: KProperty<*>): Lazy<Target> = lazyOf(mapper(parent.provideDelegate(thisRef, property).value))
}

class JsonDelegate<T>(
    private val json: JsonObject,
    private val key: String,
    private val transform: (JsonElement?) -> T,
) : DelegateProvider<T> {
    override operator fun <This> provideDelegate(thisRef: This, property: KProperty<*>) = lazy {
        val lookupKey = key.ifEmpty { property.name }
        transform(json.getPath(lookupKey))
    }

    override fun <Target> map(mapper: (T) -> Target) = MappedDelegateProvider(this, mapper)
}

interface ParseHelper {
    val json: JsonObject

    fun <T> parse(@Language("JSONPath") key: String? = null, transform: (JsonElement?) -> T): JsonDelegate<T> = JsonDelegate(json, key.orEmpty(), transform)

    fun int(@Language("JSONPath") key: String? = null, default: Int = 0): JsonDelegate<Int> = parse(key) { it.asInt(default) }
    fun long(@Language("JSONPath") key: String? = null, default: Long = 0L): JsonDelegate<Long> = parse(key) { it.asLong(default) }
    fun string(@Language("JSONPath") key: String? = null, default: String = ""): JsonDelegate<String> = parse(key) { it.asString(default) }
    fun boolean(@Language("JSONPath") key: String? = null, default: Boolean = false): JsonDelegate<Boolean> = parse(key) { it.asBoolean(default) }

    fun duration(@Language("JSONPath") key: String? = null, default: Duration = Duration.ZERO): JsonDelegate<Duration> = parse(key) {
        it?.asLong?.milliseconds ?: default
    }

    fun <T> list(@Language("JSONPath") key: String? = null, transform: (JsonElement) -> T): JsonDelegate<List<T>> = parse(key) { j ->
        j?.asJsonArray?.map(transform) ?: emptyList()
    }

    fun stringList(@Language("JSONPath") key: String? = null): JsonDelegate<List<String>> = parse(key) { j ->
        j?.asJsonArray?.mapNotNull { it.asString } ?: emptyList()
    }

    fun <K, V> map(@Language("JSONPath") key: String? = null, transform: (id: String, obj: JsonElement) -> Pair<K, V>): JsonDelegate<Map<K, V>> =
        parse(key) { it?.asJsonObject.asMap(transform) }

    fun stringIntMap(@Language("JSONPath") key: String? = null): JsonDelegate<Map<String, Int>> = map(key) { id, obj -> id to obj.asInt(0) }
    fun stringLongMap(@Language("JSONPath") key: String? = null): JsonDelegate<Map<String, Long>> = map(key) { id, obj -> id to obj.asLong(0) }
}
