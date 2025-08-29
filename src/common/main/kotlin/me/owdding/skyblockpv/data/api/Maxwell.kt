package me.owdding.skyblockpv.data.api

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import me.owdding.skyblockpv.utils.json.getPathAs
import tech.thatgravyboat.skyblockapi.utils.extentions.asInt
import tech.thatgravyboat.skyblockapi.utils.extentions.asMap
import tech.thatgravyboat.skyblockapi.utils.extentions.asString
import tech.thatgravyboat.skyblockapi.utils.json.getPath

data class Maxwell(
    val tunings: Map<String, Int>,
    val selectedPower: String,
    val highestMp: Int,
    val bagUpgrades: Int,
    val consumedRiftPrism: Boolean,
    val abiphoneContacts: Int,
) {
    companion object {
        fun fromJson(member: JsonObject, maxwell: JsonObject): Maxwell {
            return Maxwell(
                tunings = maxwell.getPath("tuning.slot_0").asMap { id, points -> id to points.asInt },
                selectedPower = maxwell["selected_power"].asString(""),
                highestMp = maxwell["highest_magical_power"].asInt(0),
                bagUpgrades = maxwell["bag_upgrades_purchased"].asInt(0),
                consumedRiftPrism = member.getPathAs<Boolean>("rift.access.consumed_prism", false),
                abiphoneContacts = member.getPathAs<JsonArray>("nether_island_player_data.abiphone.active_contacts")?.size() ?: 0,
            )
        }
    }
}
