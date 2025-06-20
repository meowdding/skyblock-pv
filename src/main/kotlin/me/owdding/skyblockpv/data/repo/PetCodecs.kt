package me.owdding.skyblockpv.data.repo

import me.owdding.ktcodecs.FieldName
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktcodecs.NamedCodec
import me.owdding.skyblockpv.utils.Utils
import me.owdding.skyblockpv.utils.codecs.ExtraData
import me.owdding.skyblockpv.utils.codecs.LoadData
import tech.thatgravyboat.skyblockapi.api.data.SkyBlockRarity

@LoadData
object PetCodecs : ExtraData {
    private val rarityOffsets: MutableList<Int> = mutableListOf()
    private val xpCurve: MutableList<Int> = mutableListOf()
    private val overwrites: MutableMap<String, Data> = mutableMapOf()
    private val defaultData = Data()


    override suspend fun load() {
        Utils.loadRepoData<PetData>("pets").let {
            this.rarityOffsets.addAll(it.rarityOffsets)
            this.xpCurve.addAll(it.xpCurve)
            this.overwrites.putAll(it.overwrites.mapKeys { (key, _) -> key.uppercase() })
        }
    }

    fun getData(pet: String): Data {
        return overwrites[pet] ?: defaultData
    }

    @GenerateCodec
    data class PetData(
        @FieldName("rarity_offsets") val rarityOffsets: List<Int>,
        @FieldName("xp_curve") val xpCurve: List<Int>,
        val overwrites: Map<String, Data>,
    )

    @GenerateCodec
    @NamedCodec("PetsData")
    data class Data(
        @FieldName("xp_curve") val xpCurve: List<Int> = PetCodecs.xpCurve,
        @FieldName("rarity_offsets") val rarityOffsets: List<Int> = PetCodecs.rarityOffsets,
        @FieldName("level_cap") val levelCap: Int = 100,
    ) {
        fun getOffset(rarity: SkyBlockRarity): Int {
            val ordinal = rarity.ordinal.coerceIn(0, rarityOffsets.size - 1)
            return rarityOffsets[ordinal]
        }

        fun getCurveForRarity(rarity: SkyBlockRarity): List<Int> {
            return xpCurve.drop(getOffset(rarity)).take(levelCap - 1)
        }
    }
}
