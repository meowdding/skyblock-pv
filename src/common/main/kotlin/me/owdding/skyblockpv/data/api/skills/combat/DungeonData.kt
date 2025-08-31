package me.owdding.skyblockpv.data.api.skills.combat

import com.google.gson.JsonObject
import me.owdding.skyblockpv.utils.json.getAs
import tech.thatgravyboat.skyblockapi.utils.extentions.asLong
import tech.thatgravyboat.skyblockapi.utils.extentions.asMap

data class DungeonData(
    val dungeonTypes: Map<String, DungeonTypeData?>,
    val classExperience: Map<String, Long>,
    val selectedClass: String,
    val secrets: Long,
) {
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
                timesPlayed = timesPlayed,
                tierCompletions = tierCompletions,
                fastestTime = fastestTime,
                bestScore = bestScore,
                experience = experience,
            )
        }
    }
}

data class DungeonTypeData(
    val experience: Long,
    val timesPlayed: Map<String, Long>,
    val tierCompletions: Map<String, Long>,
    val fastestTime: Map<String, Long>,
    val bestScore: Map<String, Long>,
)
