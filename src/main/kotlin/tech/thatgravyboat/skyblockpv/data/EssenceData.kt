package tech.thatgravyboat.skyblockpv.data

import com.google.gson.annotations.SerializedName
import tech.thatgravyboat.skyblockpv.utils.Utils

data class RepoEssenceData(
    val shops: Map<String, Map<String, RepoEssencePerk>>,
)

data class RepoEssencePerk(
    val name: String,
    @SerializedName("max_level") val maxLevel: Int,
)

object EssenceData {
    val repoEssenceData = Utils.loadFromRepo<RepoEssenceData>("essence_perks") ?: RepoEssenceData(emptyMap())
    val allPerks = repoEssenceData.shops.values.toList()
}
