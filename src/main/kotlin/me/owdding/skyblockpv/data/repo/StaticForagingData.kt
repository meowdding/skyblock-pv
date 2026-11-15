package me.owdding.skyblockpv.data.repo

import me.owdding.ktcodecs.FieldName
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktcodecs.NamedCodec
import me.owdding.ktmodules.Module
import me.owdding.skyblockpv.SkyBlockPv

@GenerateCodec
data class TreeGifts(
    @NamedCodec("cum_int_list_alt") val fig: List<Int> = emptyList(),
    @NamedCodec("cum_int_list_alt") val mangrove: List<Int> = emptyList(),
    @NamedCodec("cum_int_list_alt") val helix: List<Int> = emptyList(),
)

@GenerateCodec
data class MiscForagingData(
    @FieldName("mangrove_reward_formula") val mangroveRewardFormula: String,
    @FieldName("mangrove_personal_best") val mangrovePersonalBest: Int,
    @FieldName("mangrove_fortune") val mangroveFortune: Int,
    @FieldName("fig_reward_formula") val figRewardFormula: String,
    @FieldName("fig_personal_best") val figPersonalBest: Int,
    @FieldName("fig_fortune") val figFortune: Int,
    @FieldName("helix_reward_formula") val helixRewardFormula: String,
    @FieldName("helix_personal_best") val helixPersonalBest: Int,
    @FieldName("helix_fortune") val helixFortune: Int,
    @FieldName("agatha_power") val agathaPower: Int,
)

@Module
data object StaticForagingData {
    private val miscDataDefault = MiscForagingData(
        "level",
        100000,
        50,
        "level",
        100000,
        50,
        "level",
        100000,
        50,
        5,
    )
    private val data: ForagingData by SkyBlockPv.repoDelegate("pv/foraging", ForagingData())

    val treeGifts: TreeGifts
        get() = data.treeGifts
    val misc: MiscForagingData
        get() = data.misc

    @GenerateCodec
    data class ForagingData(
        @FieldName("tree_gifts") val treeGifts: TreeGifts = TreeGifts(),
        val misc: MiscForagingData = miscDataDefault,
    )

}
