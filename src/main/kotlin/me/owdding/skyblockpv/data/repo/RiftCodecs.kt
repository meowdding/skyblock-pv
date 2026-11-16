package me.owdding.skyblockpv.data.repo

import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktmodules.Module
import me.owdding.skyblockpv.SkyBlockPv
import tech.thatgravyboat.skyblockapi.api.repo.apis.SkyBlockItemsRepo

@Module
object RiftCodecs {
    val data: RiftRepoData by SkyBlockPv.repoDelegate("pv/rift", RiftRepoData())

    @GenerateCodec
    data class RiftRepoData(
        val trophies: List<TrophyRepo> = emptyList(),
        val montezuma: List<String> = emptyList(),
        val eyes: List<String> = emptyList(),
    )

    @GenerateCodec
    data class TrophyRepo(
        val id: String,
        val name: String,
    ) {
        val hypixelId = "RIFT_TROPHY_${id.uppercase()}"
        val item by lazy { SkyBlockItemsRepo.getItemStackOrDefault(hypixelId) }
    }
}
