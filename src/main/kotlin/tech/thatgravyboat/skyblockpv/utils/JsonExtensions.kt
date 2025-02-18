package tech.thatgravyboat.skyblockpv.utils

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import java.util.*

private fun <T> JsonElement?.parse(default: T, mapper: (JsonPrimitive) -> T): T =
    takeIf { it is JsonPrimitive }?.runCatching { mapper(this as JsonPrimitive) }?.getOrNull() ?: default

fun JsonElement?.asBoolean(default: Boolean): Boolean = parse(default) { it.asBoolean }
fun JsonElement?.asLong(default: Long): Long = parse(default) { it.asLong }


fun JsonElement?.asUUID(default: UUID): UUID = parse(default) { UUID.fromString(it.asString) }
fun JsonElement?.asString(default: String): String = parse(default) { it.asString }
fun <K, V> JsonElement?.asMap(mapper: (String, JsonElement) -> Pair<K, V>): Map<K, V> = parse(emptyMap<K, V>()) { it.asJsonObject.entrySet().associate { mapper(it.key, it.value) } }
