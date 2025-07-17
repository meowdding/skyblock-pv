package me.owdding.skyblockpv.data.api

import com.google.gson.JsonObject
import tech.thatgravyboat.skyblockapi.utils.extentions.asInt
import tech.thatgravyboat.skyblockapi.utils.extentions.asMap
import tech.thatgravyboat.skyblockapi.utils.extentions.asString
import tech.thatgravyboat.skyblockapi.utils.json.getPath

data class Maxwell(
    val tunings: Map<String, Int>,
    val selectedPower: String,
    val highestMp: Int,
    val bagUpgrades: Int,
) {
    companion object {
        fun fromJson(json: JsonObject): Maxwell {
            return Maxwell(
                tunings = json.getPath("tuning.slot_0").asMap { id, points -> id to points.asInt },
                selectedPower = json["selected_power"].asString(""),
                highestMp = json["highest_magical_power"].asInt(0),
                bagUpgrades = json["bag_upgrades_purchased"].asInt(0),
            )
        }
    }
}
