package me.owdding.skyblockpv.data.repo

import me.owdding.ktcodecs.FieldName
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktcodecs.NamedCodec
import me.owdding.ktcodecs.OptionalNullable
import me.owdding.ktmodules.Module
import me.owdding.lib.utils.MeowddingLogger
import me.owdding.lib.utils.MeowddingLogger.Companion.featureLogger
import me.owdding.skyblockpv.SkyBlockPv
import tech.thatgravyboat.skyblockapi.api.data.SkyBlockRarity

@Module
object PetCodecs : MeowddingLogger by SkyBlockPv.featureLogger() {
    private val petsData: Data by SkyBlockPv.repoDelegate("pv/pets", Data())

    private val rarityOffsetsMap: Map<String, Int>
        get() = petsData.rarityOffsetsMap
    private val defaultXpCurve: List<Int>
        get() = petsData.xpCurve
    private val overwrites: Map<String, PetData>
        get() = petsData.overwrites

    private val defaultPetData = PetData()

    fun getData(pet: String): PetData {
        return overwrites[pet] ?: defaultPetData
    }

    @NamedCodec("PetsData")
    @GenerateCodec
    data class Data(
        @FieldName("rarity_offsets_map") val rarityOffsetsMap: Map<String, Int> = emptyMap(),
        @FieldName("xp_curve") val xpCurve: List<Int> = emptyList(),
        val overwrites: Map<String, PetData> = emptyMap(),
    )

    @GenerateCodec
    data class PetData(
        @FieldName("xp_curve") @OptionalNullable internal val overrideXpCurve: List<Int>? = null,
        @FieldName("rarity_offsets_map") @OptionalNullable val rarityOffsetsMap: Map<String, Int>? = null,
        @FieldName("disable_rarity_offsets") val disableRarityOffsets: Boolean = false,
        @FieldName("level_cap") val levelCap: Int = 100,
    ) {
        val xpCurve: List<Int>
            get() = overrideXpCurve ?: this@PetCodecs.defaultXpCurve
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
