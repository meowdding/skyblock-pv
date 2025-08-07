package me.owdding.skyblockpv.data.repo

import me.owdding.ktcodecs.FieldName
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.skyblockpv.utils.Utils
import me.owdding.skyblockpv.utils.codecs.ExtraData
import me.owdding.skyblockpv.utils.codecs.LoadData

@LoadData
object CatacombsCodecs : ExtraData {
    lateinit var data: CatacombsRepoData
        private set

    override suspend fun load() {
        data = Utils.loadRepoData<CatacombsRepoData>("catacombs")
    }

    @GenerateCodec
    data class CatacombsRepoData(
        val experience: List<Long>,
        @FieldName("experience_per_overflow") val experiencePerOverflow: Long,
    )

    fun getLevelAndProgress(xp: Long, withOverflow: Boolean = false): Pair<Int, Float> {
        for (i in data.experience.indices) {
            val currentLevelXp = data.experience[i]
            if (xp < currentLevelXp) {
                val previousXp = if (i == 0) 0L else data.experience[i - 1]
                val progress = (xp - previousXp).toFloat() / (currentLevelXp - previousXp)
                return i to progress.coerceIn(0f, 1f)
            }
        }

        // xp >= experience.last() -> at or past max level
        val maxLevel = data.experience.size
        val baseXp = data.experience.last()

        if (!withOverflow || data.experiencePerOverflow <= 0L) {
            return maxLevel to 1f
        }

        val overflowXp = xp - baseXp
        val overflowLevel = (overflowXp / data.experiencePerOverflow).toInt()
        val overflowProgress = (overflowXp % data.experiencePerOverflow).toFloat() / data.experiencePerOverflow

        return (maxLevel + overflowLevel) to overflowProgress.coerceIn(0f, 1f)
    }

}
