package tech.thatgravyboat.skyblockpv.data.api.skills.combat

import com.google.gson.JsonObject
import tech.thatgravyboat.skyblockapi.utils.extentions.asLong
import tech.thatgravyboat.skyblockapi.utils.extentions.asMap

data class DungeonData(
    val dungeonTypes: Map<String, DungeonTypeData?>,
    val classExperience: Map<String, Long>,
    val secrets: Long,
) {
    companion object {
        fun fromJson(json: JsonObject): DungeonData {
            val dungeonsTypes = json.getAsJsonObject("dungeon_types")
            val catacombs = dungeonsTypes?.getAsJsonObject("catacombs")?.parseDungeonType()
            val catacombsMaster = dungeonsTypes?.getAsJsonObject("master_catacombs")?.parseDungeonType()
            val classExperience = json.getAsJsonObject("player_classes").parseClassExperience()
            val secrets = json["secrets"].asLong(0)

            return DungeonData(
                dungeonTypes = mapOf(
                    "catacombs" to catacombs,
                    "master_catacombs" to catacombsMaster,
                ),
                classExperience = classExperience,
                secrets = secrets,
            )
        }

        private fun JsonObject.parseClassExperience() = this.asMap { id, data ->
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
