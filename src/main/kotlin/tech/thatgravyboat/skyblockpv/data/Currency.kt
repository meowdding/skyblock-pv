package tech.thatgravyboat.skyblockpv.data

import com.google.gson.JsonObject
import tech.thatgravyboat.skyblockapi.utils.extentions.asBoolean
import tech.thatgravyboat.skyblockapi.utils.extentions.asLong
import tech.thatgravyboat.skyblockapi.utils.extentions.asMap
import tech.thatgravyboat.skyblockpv.data.SortedEntry.Companion.sortToEssenceOrder

data class Currency(
    val purse: Long,
    val mainBank: Long,
    val soloBank: Long = 0,
    val motes: Long,
    val cookieBuffActive: Boolean,
    val essence: Map<String, Long>,
) {
    companion object {
        fun fromJson(json: JsonObject) = Currency(
            purse = json["coin_purse"].asLong(0),
            motes = json["motes_purse"].asLong(0),
            mainBank = json["banking"].asLong(0),
            soloBank = json.getAsJsonObject("banking")?.get("balance").asLong(0),
            cookieBuffActive = json["cookie_buff_active"].asBoolean(false),
            // todo: add missing essences if not unlocked
            essence = json["essence"].asMap { id, obj -> id to obj.asJsonObject["current"].asLong(0) }.sortToEssenceOrder(),
        )
    }
}
