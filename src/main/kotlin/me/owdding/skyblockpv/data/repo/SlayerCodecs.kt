package me.owdding.skyblockpv.data.repo

import me.owdding.ktcodecs.FieldName
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.skyblockpv.utils.Utils
import me.owdding.skyblockpv.utils.codecs.CodecUtils
import me.owdding.skyblockpv.utils.codecs.DefaultedData
import me.owdding.skyblockpv.utils.codecs.ExtraData
import me.owdding.skyblockpv.utils.codecs.LoadData

@LoadData
object SlayerCodecs : DefaultedData {
    private var _data: Map<String, Slayer>? = null
    val data: Map<String, Slayer> get() = _data ?: emptyMap()

    override suspend fun load() {
        _data = Utils.loadRemoteRepoData("pv/slayer", CodecUtils.map())
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

        fun getLevel(xp: Long) = leveling.indexOfLast { it <= xp } + 1
    }
}
