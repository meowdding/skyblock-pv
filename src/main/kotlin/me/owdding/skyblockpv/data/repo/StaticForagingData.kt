package me.owdding.skyblockpv.data.repo

import me.owdding.ktcodecs.FieldName
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktcodecs.NamedCodec
import me.owdding.skyblockpv.utils.Utils
import me.owdding.skyblockpv.utils.codecs.DefaultedData
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
data object StaticForagingData : DefaultedData {
    val treeDataDefault = TreeGifts(
        emptyList(),
        emptyList(),
    )

    val miscDataDefault = MiscForagingData(
        "level",
        100000,
        50,
        "level",
        100000,
        50,
        5,
    )

    private var treeData: TreeGifts? = null
    private var miscData: MiscForagingData? = null

    val treeGifts: TreeGifts get() = treeData ?: treeDataDefault
    val misc: MiscForagingData get() = miscData ?: miscDataDefault

    @GenerateCodec
    data class ForagingData(
        @FieldName("tree_gifts") val treeGifts: TreeGifts,
        val misc: MiscForagingData,
    )

    override suspend fun load() {
        init(Utils.loadRemoteRepoData<ForagingData>("pv/foraging"))
    }

    fun init(data: ForagingData) {
        treeData = data.treeGifts
        miscData = data.misc
    }
}
