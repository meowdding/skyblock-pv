package me.owdding.skyblockpv.data.api.skills.combat

import com.google.gson.JsonObject
import me.owdding.skyblockpv.config.Config
import me.owdding.skyblockpv.data.repo.CatacombsCodecs
import me.owdding.skyblockpv.utils.json.getAs
import tech.thatgravyboat.skyblockapi.utils.extentions.asLong
import tech.thatgravyboat.skyblockapi.utils.extentions.asMap
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

data class DungeonData(
    val dungeonTypes: Map<String, DungeonTypeData?>,
    val classExperience: Map<String, Long>,
    val selectedClass: String,
    val secrets: Long,
) {
    val classToLevel = classExperience.map { (name, xp) ->
        name to CatacombsCodecs.getLevelAndProgress(xp, Config.skillOverflow)
    }.toMap()

    companion object {
        fun fromJson(json: JsonObject): DungeonData {
            val dungeonsTypes = json.getAs<JsonObject>("dungeon_types")
            val catacombs = dungeonsTypes.getAs<JsonObject>("catacombs")?.parseDungeonType()
            val catacombsMaster = dungeonsTypes.getAs<JsonObject>("master_catacombs")?.parseDungeonType()
            val classExperience = json.getAs<JsonObject>("player_classes").parseClassExperience()
            val secrets = json["secrets"].asLong(0)

            return DungeonData(
                dungeonTypes = mapOf(
                    "catacombs" to catacombs,
                    "master_catacombs" to catacombsMaster,
                ),
                classExperience = classExperience,
                selectedClass = json.getAs("selected_dungeon_class", ""),
                secrets = secrets,
            )
        }

        private fun JsonObject?.parseClassExperience() = this.asMap { id, data ->
            id to data.asJsonObject["experience"].asLong(0)
        }

        private fun JsonObject.parseDungeonType(): DungeonTypeData {
            val timesPlayed = this["times_played"].asMap { id, amount -> id to amount.asLong(0) }
            val tierCompletions = this["tier_completions"].asMap { id, amount -> id to amount.asLong(0) }
            val fastestTime = this["fastest_time"].asMap { id, amount -> id to amount.asLong(0) }
            val bestScore = this["best_score"].asMap { id, amount -> id to amount.asLong(0) }
            val experience = this["experience"].asLong(0)

            return DungeonTypeData(
                experience = experience,
                floors = timesPlayed.keys.associateWith { floor ->
                    DungeonFloor(
                        timesPlayed = timesPlayed[floor] ?: 0,
                        completions = tierCompletions[floor] ?: 0,
                        fastestTime = (fastestTime[floor] ?: 0).milliseconds,
                        bestScore = bestScore[floor] ?: 0,
                    )
                },
            )
        }
    }
}

data class DungeonTypeData(
    val experience: Long,
    val floors: Map<String, DungeonFloor>,
) {
    val completions = floors.mapValues { it.value.completions }
}

data class DungeonFloor(
    val timesPlayed: Long,
    val completions: Long,
    val fastestTime: Duration,
    val bestScore: Long,
) {
    companion object {
        val EMPTY = DungeonFloor(0, 0, Duration.ZERO, 0)
    }
}
