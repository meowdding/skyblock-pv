package tech.thatgravyboat.skyblockpv.data

import tech.thatgravyboat.skyblockpv.utils.Utils

object ForgeTimeData {
    var forgeTimes: Map<String, Long> = emptyMap()
        private set

    private val quickForgeMultiplier = mapOf(
        1 to 0.895,
        2 to 0.89,
        3 to 0.885,
        4 to 0.88,
        5 to 0.875,
        6 to 0.87,
        7 to 0.865,
        8 to 0.86,
        9 to 0.855,
        10 to 0.85,
        11 to 0.845,
        12 to 0.84,
        13 to 0.835,
        14 to 0.83,
        15 to 0.825,
        16 to 0.82,
        17 to 0.815,
        18 to 0.81,
        19 to 0.805,
        20 to 0.7,
    )

    init {
        forgeTimes = Utils.loadFromRepo<Map<String, Long>>("forge_times") ?: emptyMap()
    }

    fun getForgeTime(id: String, quickForgeLevel: Int = 0) = (forgeTimes[id] ?: 0L) * (quickForgeMultiplier[quickForgeLevel] ?: 1.0)
}
