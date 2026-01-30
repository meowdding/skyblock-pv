package me.owdding.skyblockpv.api.data.shared.types

import com.mojang.serialization.Codec
import me.owdding.ktcodecs.FieldName
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktcodecs.OptionalIfEmpty
import me.owdding.lib.repo.AbilityTreeNode
import me.owdding.lib.repo.TreeRepoData
import me.owdding.skyblockpv.api.SharedData
import me.owdding.skyblockpv.api.data.abstraction.HotmDataGetter
import me.owdding.skyblockpv.api.data.shared.SharedDataProvider
import me.owdding.skyblockpv.generated.SkyBlockPvCodecs
import tech.thatgravyboat.skyblockapi.api.profile.hotm.HotmAPI
import kotlin.collections.component1
import kotlin.collections.component2

@SharedData
object HotmDataProvider : SharedDataProvider<HotmData> {
    override val endpoint: String = "hotm"
    override val codec: Codec<HotmData> = SkyBlockPvCodecs.HotmDataCodec.codec()

    override fun create(): HotmData = HotmData(
        experience = 0,
        level = HotmAPI.tier,
        miningNodes = HotmAPI.unlockedPerks.mapNotNull { (name, perk) ->
            SkillTreeNode(
                TreeRepoData.hotm.find { node -> node.name == name }?.id ?: return@mapNotNull null,
                disabled = perk.disabled,
                level = perk.level
            )
        }
    )
}

@GenerateCodec
data class HotmData(
    override val experience: Long,
    val level: Int,
    @FieldName("nodes") @OptionalIfEmpty val miningNodes: List<SkillTreeNode>,
) : HotmDataGetter {
    override val nodes: Map<String, Int> get() = miningNodes.map { it.id to it.level }.toMap()
    override val toggledNodes: List<String> get() = miningNodes.filter { it.disabled }.map { it.id }
    override val miningAbility: String? = miningNodes.find { TreeRepoData.hotm.find { node -> node.id == it.id } is AbilityTreeNode && !it.disabled }?.id

    override fun getHotmLevel(): Int {
        return level
    }

    companion object {
        val DEFAULT = HotmData(0, 0, emptyList())
    }
}
