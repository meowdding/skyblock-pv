package me.owdding.skyblockpv.data.repo

import me.owdding.ktcodecs.FieldName
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.skyblockpv.utils.Utils
import me.owdding.skyblockpv.utils.codecs.ExtraData
import me.owdding.skyblockpv.utils.codecs.LoadData
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId

@LoadData
object NeuMiscCodecs : ExtraData {
    lateinit var data: NeuMiscRepoData
        private set

    override suspend fun load() {
        data = Utils.loadRemoteRepoData<NeuMiscRepoData>("neu/misc")
    }

    override fun loadFallback(): Result<Unit> = runCatching {
        data = NeuMiscRepoData(
            emptyMap()
        )
    }

    @GenerateCodec
    data class NeuMiscRepoData(
        @FieldName("talisman_upgrades") val talismanUpgrades: Map<SkyBlockId, List<SkyBlockId>>,
    ) {
        fun hasHigherTier(id: SkyBlockId, otherIds: List<SkyBlockId>) = talismanUpgrades[id]?.any { it in otherIds } == true
    }

}
