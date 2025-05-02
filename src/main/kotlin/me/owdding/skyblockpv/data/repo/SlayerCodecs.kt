package me.owdding.skyblockpv.data.repo

import com.mojang.serialization.Codec
import me.owdding.ktcodecs.FieldName
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktmodules.Module
import me.owdding.skyblockpv.utils.Utils

@Module
object SlayerCodecs {
    var data: Map<String, Slayer> = emptyMap()
        private set

    init {
        data = Utils.loadRepoData<Slayer, Map<String, Slayer>>("slayer") { Codec.unboundedMap(Codec.STRING, it) }
    }

    @GenerateCodec
    data class Slayer(
        val name: String,
        val id: String,
        val leveling: List<Long>,
        @FieldName("xp") val bossXp: List<Int>,
    ) {
        val maxBossTier = bossXp.size
        val maxLevel = leveling.size

        fun getLevel(xp: Long) = leveling.indexOfLast { it < xp } + 1
    }
}
