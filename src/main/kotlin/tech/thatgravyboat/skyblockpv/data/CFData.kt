package tech.thatgravyboat.skyblockpv.data

import com.google.gson.JsonObject
import tech.thatgravyboat.skyblockapi.utils.extentions.asInt
import tech.thatgravyboat.skyblockapi.utils.extentions.asLong
import tech.thatgravyboat.skyblockapi.utils.extentions.asMap

data class CFData(
    val chocolate: Long,
    val totalChocolate: Long,
    val chocolateSincePrestige: Long,
    val employees: Map<String, Int>,
    val rabbits: Map<String, Int>, // todo: also contains eggs collected & island stuff, needs custom handling
    val barnCapacity: Int,
    val prestigeLevel: Int,
    val clickUpgrades: Int,
    val chocolateMultiplierUpgrades: Int,
    val rabbitRarityUpgrades: Int,
    val timeTower: TimeTower?,
    val hitman: Hitman?,
) {
    companion object {
        fun fromJson(json: JsonObject): CFData {
            return CFData(
                chocolate = json["chocolate"].asLong(0),
                totalChocolate = json["total_chocolate"].asLong(0),
                chocolateSincePrestige = json["chocolate_since_prestige"].asLong(0),
                employees = json["employees"].asMap { k, v ->
                    if (v !is JsonObject) k to v.asInt(0)
                    else k to -1
                }.filterValues { it != -1 },
                rabbits = json["rabbits"].asMap { k, v -> k to v.asInt(0) },
                barnCapacity = json["barn_capacity"].asInt(0),
                prestigeLevel = json["prestige_level"].asInt(0),
                clickUpgrades = json["click_upgrades"].asInt(0),
                chocolateMultiplierUpgrades = json["chocolate_multiplier_upgrades"].asInt(0),
                rabbitRarityUpgrades = json["rabbit_rarity_upgrades"].asInt(0),
                timeTower = json.getAsJsonObject("time_tower")?.let { TimeTower.fromJson(it) },
                hitman = json.getAsJsonObject("rabbit_hitmen")?.let { Hitman.fromJson(it) },
            )
        }
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
