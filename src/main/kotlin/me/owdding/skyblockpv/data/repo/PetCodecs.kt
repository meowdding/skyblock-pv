package me.owdding.skyblockpv.data.repo

import com.google.gson.JsonObject
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import com.teamresourceful.resourcefullib.common.codecs.CodecExtras
import me.owdding.skyblockpv.utils.Utils
import me.owdding.skyblockpv.utils.codecs.CodecUtils
import tech.thatgravyboat.skyblockapi.api.data.SkyBlockRarity
import tech.thatgravyboat.skyblockapi.utils.json.Json.toData
import kotlin.jvm.optionals.getOrNull

object PetCodecs {
    private val rarityOffsets: MutableList<Int> = mutableListOf()
    private val xpCurve: MutableList<Int> = mutableListOf()
    private val overwrites: MutableMap<String, Data> = mutableMapOf()
    private val defaultData = Data(xpCurve, rarityOffsets, 100)

    private val DATA_CODEC = RecordCodecBuilder.create {
        it.group(
            CodecUtils.CUMULATIVE_INT_LIST_ALT.optionalFieldOf("xp_curve").forGetter(CodecExtras.optionalFor(Data::xpCurve)),
            Codec.INT.listOf().optionalFieldOf("rarity_offsets").forGetter(CodecExtras.optionalFor(Data::rarityOffsets)),
            Codec.INT.optionalFieldOf("level_cap", 100).forGetter(Data::levelCap)
        ).apply(it) { xpCurve, rarityOffsets, levelCap ->
            Data(xpCurve.getOrNull() ?: this.xpCurve, rarityOffsets.getOrNull() ?: this.rarityOffsets, levelCap)
        }
    }

    private val CODEC = RecordCodecBuilder.create {
        it.group(
            Codec.INT.listOf().fieldOf("rarity_offsets").forGetter { rarityOffsets },
            CodecUtils.CUMULATIVE_INT_LIST_ALT.fieldOf("xp_curve").forGetter { xpCurve },
            Codec.unboundedMap(Codec.STRING, DATA_CODEC).fieldOf("overwrites").forGetter { overwrites}
        ).apply(it) { rarityOffsets, xpCurve, overwrites ->
            this.rarityOffsets.addAll(rarityOffsets)
            this.xpCurve.addAll(xpCurve)
            this.overwrites.putAll(overwrites.mapKeys { it.key.uppercase() })
        }
    }

    init {
        Utils.loadFromRepo<JsonObject>("pets").toData(CODEC).let {
            if (it == null) {
                throw IllegalStateException("Failed to load pet data!")
            }
        }
    }

    fun getData(pet: String): Data {
        return overwrites[pet] ?: defaultData
    }

    data class Data(val xpCurve: List<Int>, val rarityOffsets: List<Int>, val levelCap: Int) {
        fun getOffset(rarity: SkyBlockRarity): Int {
            val ordinal = rarity.ordinal.coerceIn(0, rarityOffsets.size - 1)
            return rarityOffsets[ordinal]
        }

        fun getCurveForRarity(rarity: SkyBlockRarity): List<Int> {
            return xpCurve.drop(getOffset(rarity)).take(levelCap - 1)
        }
    }



}
