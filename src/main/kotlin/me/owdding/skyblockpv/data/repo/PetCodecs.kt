package me.owdding.skyblockpv.data.repo

import me.owdding.ktcodecs.FieldName
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktcodecs.NamedCodec
import me.owdding.ktcodecs.OptionalNullable
import me.owdding.lib.utils.MeowddingLogger
import me.owdding.lib.utils.MeowddingLogger.Companion.featureLogger
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.utils.Utils
import me.owdding.skyblockpv.utils.codecs.DefaultedData
import me.owdding.skyblockpv.utils.codecs.LoadData
import tech.thatgravyboat.skyblockapi.api.data.SkyBlockRarity

@LoadData
object PetCodecs : DefaultedData, MeowddingLogger by SkyBlockPv.featureLogger() {
    private val rarityOffsetsMap: MutableMap<String, Int> = mutableMapOf()
    private val xpCurve: MutableList<Int> = mutableListOf()
    private val overwrites: MutableMap<String, Data> = mutableMapOf()
    private val defaultData = Data()


    override suspend fun load() {
        Utils.loadRemoteRepoData<PetData>("pv/pets").let {
            this.rarityOffsetsMap.putAll(it.rarityOffsetsMap)
            this.xpCurve.addAll(it.xpCurve)
            this.overwrites.putAll(it.overwrites.mapKeys { (key, _) -> key.uppercase() })
        }
    }

    fun getData(pet: String): Data {
        return overwrites[pet] ?: defaultData
    }

    @GenerateCodec
    data class PetData(
        @FieldName("rarity_offsets_map") val rarityOffsetsMap: Map<String, Int>,
        @FieldName("xp_curve") val xpCurve: List<Int>,
        val overwrites: Map<String, Data>,
    )

    @GenerateCodec
    @NamedCodec("PetsData")
    data class Data(
        @FieldName("xp_curve") val xpCurve: List<Int> = PetCodecs.xpCurve,
        @FieldName("rarity_offsets_map") @OptionalNullable val rarityOffsetsMap: Map<String, Int>? = null,
        @FieldName("disable_rarity_offsets") val disableRarityOffsets: Boolean = false,
        @FieldName("level_cap") val levelCap: Int = 100,
    ) {
        fun getOffset(rarity: SkyBlockRarity): Int = runCatching {
            if (disableRarityOffsets) return@runCatching 0

            if (!rarityOffsetsMap.isNullOrEmpty()) {
                return@runCatching rarityOffsetsMap[rarity.name] ?: 0
            }
            if (PetCodecs.rarityOffsetsMap.isNotEmpty()) {
                return@runCatching PetCodecs.rarityOffsetsMap[rarity.name] ?: 0
            }
            return@runCatching 0
        }.getOrElse {
            warn("Failed to get offset for $this", it)
            0
        }

        fun getCurveForRarity(rarity: SkyBlockRarity): List<Int> {
            return xpCurve.drop(getOffset(rarity)).take(levelCap - 1)
        }
    }
}
