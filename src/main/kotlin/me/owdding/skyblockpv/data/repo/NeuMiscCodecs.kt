package me.owdding.skyblockpv.data.repo

import me.owdding.ktcodecs.FieldName
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.skyblockpv.utils.Utils
import me.owdding.skyblockpv.utils.codecs.DefaultedData
import me.owdding.skyblockpv.utils.codecs.ExtraData
import me.owdding.skyblockpv.utils.codecs.LoadData
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId

@LoadData
object NeuMiscCodecs : DefaultedData {
    private val defaultData = NeuMiscRepoData(
        emptyMap()
    )
    private var _data: NeuMiscRepoData? = null
    val data: NeuMiscRepoData get() = _data ?: defaultData

    override suspend fun load() {
        _data = Utils.loadRemoteRepoData<NeuMiscRepoData>("neu/misc")
    }

    @GenerateCodec
    data class NeuMiscRepoData(
        @FieldName("talisman_upgrades") val talismanUpgrades: Map<SkyBlockId, List<SkyBlockId>>,
    ) {
        fun hasHigherTier(id: SkyBlockId, otherIds: List<SkyBlockId>) = talismanUpgrades[id]?.any { it in otherIds } == true
    }

}
