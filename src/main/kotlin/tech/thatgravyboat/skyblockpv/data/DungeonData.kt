package tech.thatgravyboat.skyblockpv.data

data class DungeonData(
    val dungeonTypes: Map<String, DungeonTypeData>,
    val classExperience: Map<String, Long>,
    val secrets: Long,
)

data class DungeonTypeData(
    val experience: Long,
    val timesPlayed: Map<String, Long>,
    val tierCompletions: Map<String, Long>,
    val fastestTime: Map<String, Long>,
    val bestScore: Map<String, Long>,
)
