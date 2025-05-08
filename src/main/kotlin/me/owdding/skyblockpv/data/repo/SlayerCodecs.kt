package me.owdding.skyblockpv.data.repo

import me.owdding.ktcodecs.FieldName
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.skyblockpv.utils.Utils
import me.owdding.skyblockpv.utils.codecs.CodecUtils
import me.owdding.skyblockpv.utils.codecs.ExtraData
import me.owdding.skyblockpv.utils.codecs.LoadData

@LoadData
object SlayerCodecs : ExtraData {
    lateinit var data: Map<String, Slayer>
        private set

    override fun load() {
        data = Utils.loadRepoData("slayer", CodecUtils.map())
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
