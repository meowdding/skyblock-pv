package me.owdding.skyblockpv.api.data.shared.types

import com.mojang.serialization.Codec
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktcodecs.OptionalIfEmpty
import me.owdding.lib.repo.TreeRepoData
import me.owdding.skyblockpv.api.SharedData
import me.owdding.skyblockpv.api.data.shared.SharedDataProvider
import me.owdding.skyblockpv.generated.SkyBlockPvCodecs
import tech.thatgravyboat.skyblockapi.api.profile.hotf.HotfAPI
import tech.thatgravyboat.skyblockapi.api.profile.hotf.WhispersAPI

@SharedData
object HotfDataProvider : SharedDataProvider<HotfData> {
    override val endpoint: String = "hotf"
    override val codec: Codec<HotfData> = SkyBlockPvCodecs.HotfDataCodec.codec()

    override fun create(): HotfData = HotfData(
        forestWhispers = WhispersAPI.forestTotal,
        experience = 0f,
        level = HotfAPI.tier,
        nodes = HotfAPI.unlockedPerks.mapNotNull { (name, perk) ->
            SkillTreeNode(
                TreeRepoData.hotf.find { node -> node.name == name }?.id ?: return@mapNotNull null,
                disabled = perk.disabled,
                level = perk.level
            )
        }
    )

}

@GenerateCodec
data class HotfData(
    val forestWhispers: Long,
    val experience: Float,
    val level: Int,
    @OptionalIfEmpty val nodes: List<SkillTreeNode>,
)


