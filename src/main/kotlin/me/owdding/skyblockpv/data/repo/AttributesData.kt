package me.owdding.skyblockpv.data.repo

import me.owdding.ktmodules.Module
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.generated.CodecUtils
import me.owdding.skyblockpv.generated.SkyBlockPvCodecs
import me.owdding.skyblockpv.utils.codecs.IncludedCodecs
import tech.thatgravyboat.skyblockapi.api.data.SkyBlockRarity
import tech.thatgravyboat.skyblockapi.utils.extentions.emptyEnumMap
import java.util.EnumMap

@Module
data object AttributesData {
    operator fun get(rarity: SkyBlockRarity): List<Int> = data[rarity].orEmpty()

    private val CODEC = CodecUtils.enumMap(SkyBlockPvCodecs.getCodec<SkyBlockRarity>(), IncludedCodecs.CUMULATIVE_INT_LIST)

    private val data: EnumMap<SkyBlockRarity, List<Int>> by SkyBlockPv.repoDelegate("pv/attributes", emptyEnumMap(), CODEC)

}
