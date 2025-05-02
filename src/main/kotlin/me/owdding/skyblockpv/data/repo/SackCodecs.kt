package me.owdding.skyblockpv.data.repo

import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktmodules.Module
import me.owdding.skyblockpv.utils.Utils
import tech.thatgravyboat.skyblockapi.api.remote.RepoItemsAPI

@Module
object SackCodecs {
    var data: List<Sack>
        private set

    init {
        data = Utils.loadRepoData<Sack, List<Sack>>("sacks") { it.listOf() }
    }

    @GenerateCodec
    data class Sack(
        val sack: String,
        val items: List<String>,
    ) {
        val item by lazy { RepoItemsAPI.getItem(sack) }
    }
}
