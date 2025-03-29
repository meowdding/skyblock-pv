package tech.thatgravyboat.skyblockpv.data

import com.google.gson.JsonObject
import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import com.mojang.serialization.codecs.RecordCodecBuilder
import tech.thatgravyboat.skyblockapi.utils.extentions.asInt
import tech.thatgravyboat.skyblockapi.utils.extentions.asLong
import tech.thatgravyboat.skyblockapi.utils.extentions.asMap
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockpv.utils.Utils
import tech.thatgravyboat.skyblockpv.utils.createSkull

data class CFData(
    val chocolate: Long,
    val totalChocolate: Long,
    val chocolateSincePrestige: Long,
    val employees: List<RabbitEmployee>,
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
                }.filterValues { it != -1 }.map { RabbitEmployee(it.key, it.value) },
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

data class RabbitEmployee(
    val name: String,
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

object CFCodecs {
    var data: ChocolateFactoryRepoData? = null
        private set

    private val CFTextureCodec = Codec.unboundedMap(Codec.STRING, Codec.STRING).xmap(
        { it.entries.map { (key, value) -> CFTexture(key, value) } },
        { emptyMap() },
    )

    init {
        val CODEC = RecordCodecBuilder.create {
            it.group(
                CFTextureCodec.fieldOf("textures").forGetter(ChocolateFactoryRepoData::textures),
            ).apply(it, ::ChocolateFactoryRepoData)
        }

        val cfData = Utils.loadFromRepo<JsonObject>("chocolate_factory") ?: JsonObject()

        CODEC.parse(JsonOps.INSTANCE, cfData).let {
            if (it.isError) {
                throw RuntimeException(it.error().get().message())
            }
            data = it.getOrThrow()
        }
    }

    data class ChocolateFactoryRepoData(
        val textures: List<CFTexture>,
    )

    data class CFTexture(
        val id: String,
        val texture: String,
    ) {
        fun createSkull() = createSkull(texture)
    }
}

