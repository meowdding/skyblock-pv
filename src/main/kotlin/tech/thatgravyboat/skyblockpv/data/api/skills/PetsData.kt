package tech.thatgravyboat.skyblockpv.data.api.skills

import com.google.gson.JsonNull
import com.google.gson.JsonObject
import tech.thatgravyboat.skyblockapi.api.data.SkyBlockRarity
import tech.thatgravyboat.skyblockapi.api.remote.PetQuery
import tech.thatgravyboat.skyblockapi.api.remote.RepoPetsAPI
import tech.thatgravyboat.skyblockapi.utils.extentions.asBoolean
import tech.thatgravyboat.skyblockapi.utils.extentions.asInt
import tech.thatgravyboat.skyblockapi.utils.extentions.asLong
import tech.thatgravyboat.skyblockpv.data.repo.PetCodecs
import java.util.*

data class Pet(
    val uuid: UUID?, // Item UUID (can be null if it wasn't an item before)
    val uniqueId: UUID?, // Pet UUID (can be null if montezuma)
    val type: String,
    val exp: Long,
    val active: Boolean,
    val tier: String,
    val heldItem: String?,
    val candyUsed: Int,
    val skin: String?,
) {
    val rarity = SkyBlockRarity.entries.find { it.name == tier } ?: SkyBlockRarity.COMMON
    val data = PetCodecs.getData(type)
    val cumulativeLevels = data.getCurveForRarity(rarity).toMutableList().also { it.addFirst(0) }

    val itemStack by lazy { RepoPetsAPI.getPetAsItem(PetQuery(type, rarity, level, skin, heldItem)) }

    val level = cumulativeLevels.let { lvls ->
        lvls.findLast { it <= exp }?.let { lvls.indexOf(it) }?.plus(1) ?: 1
    }

    val progressToNextLevel: Float = run {
        if (level == data.levelCap) return@run 1f

        val currXp = cumulativeLevels[(level - 1).coerceAtLeast(0)]
        ((exp.toFloat() - currXp) / (cumulativeLevels[level] - currXp))
    }
    val progressToMax: Float = (exp.toFloat() / cumulativeLevels.last()).coerceAtMost(1f)

    companion object {
        fun fromJson(obj: JsonObject) = Pet(
            uuid = obj["uuid"]?.takeIf { it !is JsonNull }?.asString?.let { UUID.fromString(it) },
            uniqueId = obj["uniqueId"]?.takeIf { it !is JsonNull }?.asString?.let { UUID.fromString(it) },
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
