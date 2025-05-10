package me.owdding.skyblockpv.data.repo

import me.owdding.ktcodecs.GenerateCodec
import me.owdding.skyblockpv.utils.Utils
import me.owdding.skyblockpv.utils.codecs.ExtraData
import me.owdding.skyblockpv.utils.codecs.LoadData
import tech.thatgravyboat.skyblockapi.api.remote.RepoItemsAPI

@LoadData
object RiftCodecs : ExtraData {
    lateinit var data: RiftRepoData
        private set

    override suspend fun load() {
        data = Utils.loadRepoData("rift")
    }

    @GenerateCodec
    data class RiftRepoData(
        val trophies: List<TrophyRepo>,
        val montezuma: List<String>,
        val eyes: List<String>,
    )

    @GenerateCodec
    data class TrophyRepo(
        val id: String,
        val name: String,
    ) {
        val hypixelId = "RIFT_TROPHY_${id.uppercase()}"
        val item by lazy { RepoItemsAPI.getItem(hypixelId) }
    }
}
