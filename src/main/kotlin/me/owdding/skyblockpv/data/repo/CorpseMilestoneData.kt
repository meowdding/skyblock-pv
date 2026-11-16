package me.owdding.skyblockpv.data.repo

import me.owdding.ktcodecs.FieldName
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktmodules.Module
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.data.api.skills.GlaciteData

@Module
object CorpseMilestoneData {
    val data: CorpseMilestoneRepoData by SkyBlockPv.repoDelegate("pv/corpse_milestones", CorpseMilestoneRepoData(0, emptyList()))

    @GenerateCodec
    data class CorpseMilestoneRepoData(
        @FieldName("max_milestone") val maxMilestone: Int,
        val milestones: List<Map<String, Int>>,
    )

    fun getCorpseMilestone(glaciteData: GlaciteData): Int {
        val milestone = data.milestones.asReversed().firstOrNull { milestone ->
            milestone.all { (corpse, required) ->
                (glaciteData.corpsesLooted[corpse] ?: 0) >= required
            }
        }

        return if (milestone != null) {
            data.milestones.indexOf(milestone) + 1
        } else {
            0
        }
    }
}
