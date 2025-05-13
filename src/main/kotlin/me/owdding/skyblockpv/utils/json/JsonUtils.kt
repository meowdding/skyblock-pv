package me.owdding.skyblockpv.utils.json

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import org.intellij.lang.annotations.Language
import tech.thatgravyboat.skyblockapi.utils.extentions.*
import tech.thatgravyboat.skyblockapi.utils.json.getPath
import java.lang.Long
import java.util.*
import java.lang.Boolean as JavaBoolean
import java.lang.Double as JavaDouble
import java.lang.Float as JavaFloat
import java.lang.Integer as JavaInteger
import java.lang.Long as JavaLong
import java.lang.Short as JavaShort
import java.lang.String as JavaString

inline fun <reified T> JsonElement.to(): T? {
    val clazz = T::class.java
    when {
        clazz == JsonObject::class.java || clazz == JsonArray::class.java || clazz == JsonElement::class.java -> return this as? T
        clazz == String || clazz == JavaString::class.java -> return this.asJsonPrimitive?.asString() as? T
        clazz == Int::class.java || clazz == JavaInteger::class.java -> return this.asJsonPrimitive?.asInt(0) as? T
        clazz == Double::class.java || clazz == JavaDouble::class.java -> return this.asJsonPrimitive?.asDouble(0.0) as? T
        clazz == Long::class.java || clazz == JavaLong::class.java -> return this.asJsonPrimitive?.asLong(0) as? T
        clazz == Boolean::class.java || clazz == JavaBoolean::class.java -> return this.asJsonPrimitive?.asBoolean(false) as? T
        clazz == Short::class.java || clazz == JavaShort::class.java -> return this.asJsonPrimitive?.asShort(0) as? T
        clazz == Float::class.java || clazz == JavaFloat::class.java -> return this.asJsonPrimitive?.asDouble(0.0)?.toFloat() as? T
        clazz == UUID::class.java -> return this.asJsonPrimitive?.asUUID() as? T
        clazz.isEnum -> {
            return clazz.enumConstants.find { (it as Enum<*>).name.equals(this.asString(), true) }
        }

        else -> {
            IllegalArgumentException("No thingy found for ${T::class.java}").printStackTrace()
            return null
        }
    }
}

inline fun <reified T> JsonObject?.getAs(key: String): T? = this?.get(key)?.to<T>()
inline fun <reified T> JsonObject?.getAs(key: String, fallback: T): T = this.getAs(key) ?: fallback
inline fun <reified T> JsonObject?.getPathAs(@Language("JSONPath") path: String): T? = this?.getPath(path)?.to<T>()
inline fun <reified T> JsonObject?.getPathAs(@Language("JSONPath") path: String, fallback: T): T = this.getPathAs(path) ?: fallback

