package me.owdding.skyblockpv.data.repo

import me.owdding.ktcodecs.GenerateCodec
import me.owdding.skyblockpv.utils.Utils
import me.owdding.skyblockpv.utils.codecs.DefaultedData
import me.owdding.skyblockpv.utils.codecs.ExtraData
import me.owdding.skyblockpv.utils.codecs.LoadData
import tech.thatgravyboat.skyblockapi.api.remote.RepoItemsAPI

@LoadData
object RiftCodecs : DefaultedData {
    private val defaultData = RiftRepoData(
    emptyList(),
    emptyList(),
    emptyList(),
    )
    private var _data: RiftRepoData? = null
    val data: RiftRepoData get() = _data ?: defaultData

    override suspend fun load() {
        _data = Utils.loadRemoteRepoData("pv/rift")
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
        val item by RepoItemsAPI.getItemLazy(hypixelId)
    }
}
