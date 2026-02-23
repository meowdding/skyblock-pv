package me.owdding.skyblockpv.data.repo

import com.mojang.serialization.Codec
import me.owdding.skyblockpv.generated.SkyBlockPvCodecs
import me.owdding.skyblockpv.utils.Utils
import me.owdding.skyblockpv.utils.codecs.CodecUtils
import me.owdding.skyblockpv.utils.codecs.DefaultedData
import me.owdding.skyblockpv.utils.codecs.LoadData
import tech.thatgravyboat.skyblockapi.api.data.SkyBlockRarity

@LoadData
data object AttributesData : DefaultedData {
    val common = mutableListOf<Int>()
    val uncommon = mutableListOf<Int>()
    val rare = mutableListOf<Int>()
    val epic = mutableListOf<Int>()
    val legendary = mutableListOf<Int>()

    operator fun get(rarity: SkyBlockRarity) = when (rarity) {
        SkyBlockRarity.COMMON -> common
        SkyBlockRarity.UNCOMMON -> uncommon
        SkyBlockRarity.RARE -> rare
        SkyBlockRarity.EPIC -> epic
        SkyBlockRarity.LEGENDARY -> legendary
        else -> emptyList()
    }

    private operator fun set(rarity: SkyBlockRarity, value: List<Int>) {
        when (rarity) {
            SkyBlockRarity.COMMON -> common
            SkyBlockRarity.UNCOMMON -> uncommon
            SkyBlockRarity.RARE -> rare
            SkyBlockRarity.EPIC -> epic
            SkyBlockRarity.LEGENDARY -> legendary
            else -> return
        }.apply {
            clear()
            addAll(value)
        }
    }

    override suspend fun load() {
        Utils.loadRemoteRepoData(
            "pv/attributes",
            Codec.unboundedMap(
                SkyBlockPvCodecs.getCodec<SkyBlockRarity>(),
                CodecUtils.CUMULATIVE_INT_LIST,
            ),
        ).forEach { (rarity, ints) ->
            this[rarity] = ints
        }
    }
}
