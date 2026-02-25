package me.owdding.skyblockpv.data.repo

import me.owdding.ktcodecs.FieldName
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.lib.utils.MeowddingLogger
import me.owdding.lib.utils.MeowddingLogger.Companion.featureLogger
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.utils.Utils
import me.owdding.skyblockpv.utils.codecs.DefaultedData
import me.owdding.skyblockpv.utils.codecs.ExtraData
import me.owdding.skyblockpv.utils.codecs.LoadData

@LoadData
object CatacombsCodecs : DefaultedData, MeowddingLogger by SkyBlockPv.featureLogger() {
    private val defaultData = CatacombsRepoData(emptyList(), Long.MAX_VALUE)
    private var _data: CatacombsRepoData? = null
    val data: CatacombsRepoData get() = _data ?: defaultData

    override suspend fun load() {
        _data = Utils.loadRemoteRepoData<CatacombsRepoData>("pv/catacombs")
    }

    @GenerateCodec
    data class CatacombsRepoData(
        val experience: List<Long>,
        @FieldName("experience_per_overflow") val experiencePerOverflow: Long,
    )

    fun getLevelAndProgress(xp: Long, withOverflow: Boolean = false): Pair<Int, Float> = runCatching {
        for (i in data.experience.indices) {
            val currentLevelXp = data.experience[i]
            if (xp <= currentLevelXp) {
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
    }.getOrElse {
        warn("Encountered an error getting level and progress!", it)
        0 to 0f
    }

}
