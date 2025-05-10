package me.owdding.skyblockpv.data.repo

import me.owdding.ktcodecs.GenerateCodec
import me.owdding.skyblockpv.utils.Utils
import me.owdding.skyblockpv.utils.codecs.CodecUtils
import me.owdding.skyblockpv.utils.codecs.ExtraData
import me.owdding.skyblockpv.utils.codecs.LoadData
import tech.thatgravyboat.skyblockapi.api.remote.RepoItemsAPI

@LoadData
object SackCodecs : ExtraData {
    lateinit var data: List<Sack>
        private set


    override suspend fun load() {
        data = Utils.loadRepoData("sacks", CodecUtils.list())
    }

    @GenerateCodec
    data class Sack(
        val sack: String,
        val items: List<String>,
    ) {
        val item by lazy { RepoItemsAPI.getItem(sack) }
    }
}
