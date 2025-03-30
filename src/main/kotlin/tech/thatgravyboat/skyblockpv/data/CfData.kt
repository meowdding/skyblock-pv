package tech.thatgravyboat.skyblockpv.data

import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import com.mojang.serialization.codecs.RecordCodecBuilder
import com.notkamui.keval.keval
import tech.thatgravyboat.skyblockapi.api.data.SkyBlockRarity
import tech.thatgravyboat.skyblockapi.utils.extentions.asInt
import tech.thatgravyboat.skyblockapi.utils.extentions.asLong
import tech.thatgravyboat.skyblockapi.utils.extentions.asMap
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockpv.utils.CodecUtils
import tech.thatgravyboat.skyblockpv.utils.Utils
import tech.thatgravyboat.skyblockpv.utils.createSkull

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
) {
    val barnCapacity = barnCapacityLevel * 2 + 18

    companion object {
        fun fromJson(json: JsonObject): CfData {
            return CfData(
                chocolate = json["chocolate"].asLong(0),
                totalChocolate = json["total_chocolate"].asLong(0),
                chocolateSincePrestige = json["chocolate_since_prestige"].asLong(0),
                employees = json["employees"].asMap { k, v -> k to v.asInt(0) }.map { RabbitEmployee(it.key, it.value) },
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

object CfCodecs {
    var data: CfRepoData? = null
        private set

    private val CfTextureCodec = Codec.unboundedMap(Codec.STRING, Codec.STRING).xmap(
        { it.entries.map { (key, value) -> CfTextureRepo(key, value) } },
        { emptyMap() },
    )

    private val CfEmployeeCodec = RecordCodecBuilder.create {
        it.group(
            Codec.STRING.fieldOf("id").forGetter(CfEmployeeRepo::id),
            Codec.STRING.fieldOf("name").forGetter(CfEmployeeRepo::name),
            Codec.STRING.fieldOf("reward").forGetter(CfEmployeeRepo::rewardFormula),
        ).apply(it, ::CfEmployeeRepo)
    }

    private val CfMiscCodec = RecordCodecBuilder.create {
        it.group(
            CodecUtils.INT_LONG_MAP.fieldOf("chocolate_prestige").forGetter(CfMiscRepo::chocolatePerPrestige),
        ).apply(it, ::CfMiscRepo)
    }

    private val CfRabbitRaritiesCodec = Codec.unboundedMap(CodecUtils.SKYBLOCK_RARITY_CODEC, Codec.STRING.listOf())

    init {
        val CODEC = RecordCodecBuilder.create {
            it.group(
                CfTextureCodec.fieldOf("textures").forGetter(CfRepoData::textures),
                CfEmployeeCodec.listOf().fieldOf("employees").forGetter(CfRepoData::employees),
                CfRabbitRaritiesCodec.fieldOf("rabbits").forGetter(CfRepoData::rabbits),
                CfMiscCodec.fieldOf("misc").forGetter(CfRepoData::misc),
            ).apply(it, ::CfRepoData)
        }

        val cfData = Utils.loadFromRepo<JsonObject>("chocolate_factory") ?: JsonObject()

        CODEC.parse(JsonOps.INSTANCE, cfData).let {
            if (it.isError) {
                throw RuntimeException(it.error().get().message())
            }
            data = it.getOrThrow()
        }
    }

    data class CfRepoData(
        val textures: List<CfTextureRepo>,
        val employees: List<CfEmployeeRepo>,
        val rabbits: Map<SkyBlockRarity, List<String>>,
        val misc: CfMiscRepo,
    )

    data class CfEmployeeRepo(
        val id: String,
        val name: String,
        val rewardFormula: String,
    ) {
        fun getReward(level: Int) = rewardFormula.keval {
            includeDefault()
            constant {
                name = "level"
                value = level.toDouble()
            }
        }.toInt()
    }

    data class CfTextureRepo(
        val id: String,
        val texture: String,
    ) {
        fun createSkull() = createSkull(texture)
    }

    data class CfMiscRepo(
        val chocolatePerPrestige: Map<Int, Long>,
    ) {
        val maxPrestigeLevel = chocolatePerPrestige.size + 1
    }
}

