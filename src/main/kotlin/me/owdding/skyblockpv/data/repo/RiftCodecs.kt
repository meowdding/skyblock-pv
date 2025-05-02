package me.owdding.skyblockpv.data.repo

import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktmodules.Module
import me.owdding.skyblockpv.utils.Utils
import tech.thatgravyboat.skyblockapi.api.remote.RepoItemsAPI

@Module
object RiftCodecs {
    var data: RiftRepoData
        private set

    init {
        data = Utils.loadRepoData<RiftRepoData>("rift")
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
