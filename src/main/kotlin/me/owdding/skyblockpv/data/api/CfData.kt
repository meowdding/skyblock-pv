package me.owdding.skyblockpv.data.api

import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import me.owdding.skyblockpv.utils.json.getAs
import me.owdding.skyblockpv.utils.json.getPathAs
import me.owdding.skyblockpv.utils.theme.PvColors
import tech.thatgravyboat.skyblockapi.api.datetime.SkyBlockInstant
import tech.thatgravyboat.skyblockapi.utils.extentions.asEnum
import tech.thatgravyboat.skyblockapi.utils.extentions.asInt
import tech.thatgravyboat.skyblockapi.utils.extentions.asList
import tech.thatgravyboat.skyblockapi.utils.extentions.asLong
import tech.thatgravyboat.skyblockapi.utils.extentions.asMap
import tech.thatgravyboat.skyblockapi.utils.json.getPath

data class CfData(
    val chocolate: Long,
    val totalChocolate: Long,
    val chocolateSincePrestige: Long,
    val employees: List<RabbitEmployee>,
    val rabbits: Map<String, Int>, // todo: also contains eggs collected & island stuff, needs custom handling
    val barnCapacityLevel: Int,
    val prestigeLevel: Int,
    val clickUpgrades: Int,
    val chocolateMultiplierUpgrades: Int,
    val rabbitRarityUpgrades: Int,
    val timeTower: TimeTower?,
    val hitman: Hitman?,
    val lastUpdate: Long,
    val faction: CfFaction?,
    val factionLevel: Int,
    val chocobits: List<ChocoBit>,
    val chocobitsFound: Int,
) {
    val barnCapacity = barnCapacityLevel * 2 + 18

    companion object {
        fun fromJson(json: JsonObject): CfData {
            return CfData(
                chocolate = json["chocolate"].asLong(0),
                totalChocolate = json["total_chocolate"].asLong(0),
                chocolateSincePrestige = json["chocolate_since_prestige"].asLong(0),
                employees = json["employees"].asMap { k, v -> k to v.asInt(0) }
                    .map { RabbitEmployee(it.key, it.value) },
                rabbits = json["rabbits"].asMap { k, v ->
                    if (v is JsonPrimitive && v.isNumber) k to v.asInt
                    else k to -1
                }.filterValues { it != -1 },
                barnCapacityLevel = json["rabbit_barn_capacity_level"].asInt(0),
                prestigeLevel = json["chocolate_level"].asInt(0),
                clickUpgrades = json["click_upgrades"].asInt(0),
                chocolateMultiplierUpgrades = json["chocolate_multiplier_upgrades"].asInt(0),
                rabbitRarityUpgrades = json["rabbit_rarity_upgrades"].asInt(0),
                timeTower = json.getAs<JsonObject>("time_tower")?.let { TimeTower.fromJson(it) },
                hitman = json.getAs<JsonObject>("rabbit_hitmen")?.let { Hitman.fromJson(it) },
                lastUpdate = json["last_viewed_chocolate_factory"].asLong(0),
                faction = json.getPathAs("rabbits.selected_faction"),
                factionLevel = json.getPathAs("rabbits.faction_level", 0),
                chocobits = json.getPath("chocobits.owned").asList { ChocoBit.fromJson(it.asJsonObject) }.filterNotNull(),
                chocobitsFound = json.getPath("chocobits.total_found").asInt(0),
            )
        }
    }
}

data class ChocoBit(
    val id: Int,
    val ownedYear: Int,
    val expiryYear: Int,
) {

    companion object {
        fun fromJson(json: JsonObject): ChocoBit? = ChocoBit(
            id = json.get("id").asInt(-1).takeIf { it > 0 } ?: return null,
            ownedYear = json.get("owned_year").asInt(SkyBlockInstant.now().year),
            expiryYear = json.get("expiry_year").asInt(SkyBlockInstant.now().year),
        )
    }
}

enum class CfFaction {
    CITY,
    MOUNTAIN,
    COUNTRY,
    BEACH,
}

data class RabbitEmployee(
    val id: String,
    val level: Int,
) {
    val color = when (level) {
        in (0..9) -> PvColors.WHITE
        in (10..74) -> PvColors.GREEN
        in (75..124) -> PvColors.BLUE
        in (125..174) -> PvColors.DARK_PURPLE
        in (175..199) -> PvColors.GOLD
        in (200..219) -> PvColors.LIGHT_PURPLE
        in (220..225) -> PvColors.AQUA
        in (225..235) -> PvColors.RED
        else -> PvColors.GRAY
    }
}

data class TimeTower(
    val charges: Int,
    val level: Int,
    val activationTime: Long,
) {
    companion object {
        fun fromJson(json: JsonObject): TimeTower {
            return TimeTower(
                charges = json["charges"].asInt(0),
                level = json["level"].asInt(0),
                activationTime = json["activation_time"].asLong(0),
            )
        }
    }
}

data class Hitman(
    val slots: Int,
    val uncollectedEggs: Int,
) {
    companion object {
        fun fromJson(json: JsonObject): Hitman {
            return Hitman(
                slots = json["rabbit_hitmen_slots"].asInt(0),
                uncollectedEggs = json["missed_uncollected_eggs"].asInt(0),
            )
        }
    }
}
