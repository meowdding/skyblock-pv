package me.owdding.skyblockpv.utils.json

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import org.intellij.lang.annotations.Language
import tech.thatgravyboat.skyblockapi.utils.extentions.*
import tech.thatgravyboat.skyblockapi.utils.json.getPath
import java.util.*

inline fun <reified T> JsonElement.to(): T? {
    val clazz = T::class.java
    when {
        clazz == JsonObject::class.java || clazz == JsonArray::class.java -> return this as? T
        clazz == String -> return this.asJsonPrimitive?.asString() as? T
        clazz == Int::class.java -> return this.asJsonPrimitive?.asInt(0) as? T
        clazz == Double::class.java -> return this.asJsonPrimitive?.asDouble(0.0) as? T
        clazz == Long::class.java -> return this.asJsonPrimitive?.asLong(0) as? T
        clazz == Boolean::class.java -> return this.asJsonPrimitive?.asBoolean(false) as? T
        clazz == Short::class.java -> return this.asJsonPrimitive?.asShort(0) as? T
        clazz == Float::class.java -> return this.asJsonPrimitive?.asDouble(0.0)?.toFloat() as? T
        clazz == UUID::class.java -> return this.asJsonPrimitive?.asUUID() as? T
        clazz.isEnum -> {
            return clazz.enumConstants.find { (it as Enum<*>).name.equals(this.asString(), true) }
        }

        else -> {
            IllegalArgumentException("No thingy found for ${T::class.simpleName}").printStackTrace()
            return null
        }
    }
}

inline fun <reified T> JsonObject?.getAs(key: String): T? = this?.get(key)?.to<T>()
inline fun <reified T> JsonObject?.getPathAs(@Language("JSONPath") path: String): T? = this?.getPath(path)?.to<T>()

