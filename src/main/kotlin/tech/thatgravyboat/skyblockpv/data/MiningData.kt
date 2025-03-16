package tech.thatgravyboat.skyblockpv.data

import com.google.gson.JsonObject
import tech.thatgravyboat.skyblockpv.utils.asBoolean
import tech.thatgravyboat.skyblockpv.utils.asLong
import tech.thatgravyboat.skyblockpv.utils.asMap
import tech.thatgravyboat.skyblockpv.utils.asString

data class MiningCore(
    val nodes: Map<String, Int>,
    val crystals: Map<String, Crystal>,
    val experience: Long,
    val powderMithril: Int,
    val powderSpentMithril: Int,
    val powderGemstone: Int,
    val powderSpentGemstone: Int,
    val powderGlacite: Int,
    val powderSpentGlacite: Int,
)

data class Crystal(
    val state: String,
    val totalPlaced: Int,
    val totalFound: Int,
)

data class Forge(
    val slots: Map<Int, ForgeSlot>,
) {
    companion object {
        fun fromJson(json: JsonObject): Forge {
            val forge = json.getAsJsonObject("forge_processes")?.getAsJsonObject("forge_1") ?: JsonObject()

            return forge.asMap { key, value ->
                val obj = value.asJsonObject
                val slot = ForgeSlot(
                    type = obj["type"].asString(""),
                    id = obj["id"].asString(""),
                    startTime = obj["startTime"].asLong(0),
                    notified = obj["notified"].asBoolean(false),
                )

                key.toInt() to slot
            }.let { Forge(it) }
        }
    }
}

data class ForgeSlot(
    val type: String,
    val id: String,
    val startTime: Long,
    val notified: Boolean,
)
