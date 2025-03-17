package tech.thatgravyboat.skyblockpv.data

import com.google.gson.JsonNull
import com.google.gson.JsonObject
import tech.thatgravyboat.skyblockpv.utils.asBoolean
import tech.thatgravyboat.skyblockpv.utils.asInt
import tech.thatgravyboat.skyblockpv.utils.asLong
import java.util.*

data class Pet(
    val uuid: UUID?,
    val uniqueId: UUID?,
    val type: String,
    val exp: Long,
    val active: Boolean,
    val tier: String,
    val heldItem: String?,
    val candyUsed: Int,
    val skin: String?,
) {
    companion object {
        fun fromJson(obj: JsonObject) = Pet(
            uuid = obj["uuid"]?.takeIf { it !is JsonNull }?.asString?.let { UUID.fromString(it) },
            uniqueId = obj["uuid"]?.takeIf { it !is JsonNull }?.asString?.let { UUID.fromString(it) },
            type = obj["type"].asString,
            exp = obj["exp"]?.asLong(0) ?: 0,
            active = obj["active"].asBoolean(false),
            tier = obj["tier"].asString,
            heldItem = obj["heldItem"]?.takeIf { it !is JsonNull }?.asString,
            candyUsed = obj["candyUsed"].asInt(0),
            skin = obj["skin"]?.takeIf { it !is JsonNull }?.asString,
        )
    }
}
