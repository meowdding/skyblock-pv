package me.owdding.skyblockpv.data.api

import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import tech.thatgravyboat.skyblockapi.utils.extentions.asInt
import tech.thatgravyboat.skyblockapi.utils.extentions.asLong
import tech.thatgravyboat.skyblockapi.utils.extentions.asMap
import tech.thatgravyboat.skyblockapi.utils.text.TextColor

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
                    if (v is JsonPrimitive) k to v.asInt
                    else k to -1
                }.filterValues { it != -1 },
                barnCapacityLevel = json["rabbit_barn_capacity_level"].asInt(0),
                prestigeLevel = json["chocolate_level"].asInt(0),
                clickUpgrades = json["click_upgrades"].asInt(0),
                chocolateMultiplierUpgrades = json["chocolate_multiplier_upgrades"].asInt(0),
                rabbitRarityUpgrades = json["rabbit_rarity_upgrades"].asInt(0),
                timeTower = json.getAsJsonObject("time_tower")?.let { TimeTower.fromJson(it) },
                hitman = json.getAsJsonObject("rabbit_hitmen")?.let { Hitman.fromJson(it) },
                lastUpdate = json["last_viewed_chocolate_factory"].asLong(0),
            )
        }
    }
}

data class RabbitEmployee(
    val id: String,
    val level: Int,
) {
    val color = when (level) {
        in (0..9) -> TextColor.WHITE
        in (10..74) -> TextColor.GREEN
        in (75..124) -> TextColor.BLUE
        in (125..174) -> TextColor.DARK_PURPLE
        in (175..199) -> TextColor.GOLD
        in (200..219) -> TextColor.LIGHT_PURPLE
        in (220..225) -> TextColor.AQUA
        else -> TextColor.GRAY
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
