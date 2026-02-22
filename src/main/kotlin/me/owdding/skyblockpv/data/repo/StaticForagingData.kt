package me.owdding.skyblockpv.data.repo

import me.owdding.ktcodecs.FieldName
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktcodecs.NamedCodec
import me.owdding.skyblockpv.utils.Utils
import me.owdding.skyblockpv.utils.codecs.ExtraData
import me.owdding.skyblockpv.utils.codecs.LoadData

@GenerateCodec
data class TreeGifts(
    @NamedCodec("cum_int_list_alt") val fig: List<Int>,
    @NamedCodec("cum_int_list_alt") val mangrove: List<Int>,
)

@GenerateCodec
data class MiscForagingData(
    @FieldName("mangrove_reward_formula") val mangroveRewardFormula: String,
    @FieldName("mangrove_personal_best") val mangrovePersonalBest: Int,
    @FieldName("mangrove_fortune") val mangroveFortune: Int,
    @FieldName("fig_reward_formula") val figRewardFormula: String,
    @FieldName("fig_personal_best") val figPersonalBest: Int,
    @FieldName("fig_fortune") val figFortune: Int,
    @FieldName("agatha_power") val agathaPower: Int,
)

@LoadData
data object StaticForagingData : ExtraData {
    lateinit var treeGifts: TreeGifts
        private set
    lateinit var misc: MiscForagingData
        private set

    @GenerateCodec
    data class ForagingData(
        @FieldName("tree_gifts") val treeGifts: TreeGifts,
        val misc: MiscForagingData
    )

    override suspend fun load() {
        init(Utils.loadRepoData<ForagingData>("foraging"))
    }

    fun init(data: ForagingData) {
        treeGifts = data.treeGifts
        misc = data.misc
    }
}
