package me.owdding.skyblockpv.data.repo

import me.owdding.ktcodecs.FieldName
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.skyblockpv.data.api.skills.GlaciteData
import me.owdding.skyblockpv.utils.Utils
import me.owdding.skyblockpv.utils.codecs.DefaultedData
import me.owdding.skyblockpv.utils.codecs.LoadData

@LoadData
object CorpseMilestoneData: DefaultedData {
    private var _data: CorpseMilestoneRepoData? = null
    val data: CorpseMilestoneRepoData get() = _data ?: CorpseMilestoneRepoData(0, emptyList())

    override suspend fun load() {
        _data = Utils.loadRemoteRepoData<CorpseMilestoneRepoData>("pv/corpse_milestones")
    }

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
