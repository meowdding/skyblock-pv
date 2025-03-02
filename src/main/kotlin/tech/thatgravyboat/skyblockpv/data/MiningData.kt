package tech.thatgravyboat.skyblockpv.data

data class MiningCore(
    val nodes: Map<String, Int>,
    val crystals: Map<String, Crystal>,
    val experience: Long,
    val powderMithril: Int,
    val powderSpentMithril: Int,
    val powderGemstone: Int,
    val powderSpentGemstone: Int,
    val powderGlacite: Int,
    val powderSpentGlacite: Int,
)

data class Crystal(
    val state: String,
    val totalPlaced: Int,
    val totalFound: Int,
)
