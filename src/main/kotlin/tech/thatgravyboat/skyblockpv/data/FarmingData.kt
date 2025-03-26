package tech.thatgravyboat.skyblockpv.data

import com.google.gson.JsonObject
import tech.thatgravyboat.skyblockapi.utils.extentions.asBoolean
import tech.thatgravyboat.skyblockapi.utils.extentions.asInt
import tech.thatgravyboat.skyblockapi.utils.extentions.asMap
import tech.thatgravyboat.skyblockapi.utils.extentions.asString
import tech.thatgravyboat.skyblockapi.utils.text.TextColor

data class FarmingData(
    val medalInventory: Map<MedalType, Int>,
    val perks: FarmingPerks,
    val contest: List<Contest>,
    val uniqueBrackets: Map<MedalType, List<GardenResource>>,
    val personalBests: Map<GardenResource, Int>,
) {
    companion object {
        fun fromJson(data: JsonObject?) = FarmingData(
            medalInventory = data?.getAsJsonObject("medals_inv").asMap { key, element ->
                MedalType.valueOf(key.uppercase()) to element.asInt(0)
            },
            perks = FarmingPerks.fromJson(data?.getAsJsonObject("perks")),
            contest = data?.get("contests").asMap { key, element ->
                val data = element.asJsonObject
                key to Contest(
                    key,
                    data?.get("collected").asInt(0),
                    data?.get("claimed_rewards")?.asBoolean(false),
                    data?.get("claimed_position")?.asInt(0),
                    data?.get("claimed_participants")?.asInt(0),
                    data?.get("claimed_medal")?.asString("")?.takeIf { it.isNotBlank() }
                )
            }.values.toList(),
            uniqueBrackets = data?.get("unique_brackets").asMap { key, element ->
                MedalType.valueOf(key.uppercase()) to element.asJsonArray.map { GardenResource.getByApiId(it.asString) }
            },
            personalBests = data?.get("personal_bests").asMap { key, element -> GardenResource.getByApiId(key) to element.asInt(0) },
        )
    }
}

data class FarmingPerks(val farmingLevelCap: Int, val doubleDrops: Int, val personalBests: Boolean) {
    companion object {
        fun fromJson(data: JsonObject?) = FarmingPerks(
            data?.get("farming_level_cap").asInt(0),
            data?.get("double_drops").asInt(0),
            data?.get("personal_bests").asBoolean(false)
        )
    }
}

data class Contest(val id: String, val collected: Int, val claimedRewards: Boolean?, val position: Int?, val claimedParticipates: Int?, val claimedMedal: String?) {
    fun isOfType(type: GardenResource): Boolean {
        return id.endsWith(type.internalName)
    }
}

enum class MedalType(val color: Int) {
    BRONZE(TextColor.RED),
    SILVER(TextColor.GRAY),
    GOLD(TextColor.GOLD),
    PLATINUM(TextColor.DARK_AQUA),
    DIAMOND(TextColor.AQUA);

    companion object {
        val actualMedals = listOf(BRONZE, SILVER, GOLD)
    }
}
