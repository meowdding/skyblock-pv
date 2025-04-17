package tech.thatgravyboat.skyblockpv.data.api.skills.combat

import com.google.gson.JsonObject
import tech.thatgravyboat.skyblockapi.utils.extentions.asInt
import tech.thatgravyboat.skyblockpv.data.repo.CrimsonIsleCodecs
import tech.thatgravyboat.skyblockpv.utils.asEnum

//mini daily, kuudra daily
// nether_island_player_data
data class CrimsonIsleData(
    val kuudraStats: List<KuudraEntry>,
    val dojoStats: List<DojoEntry>,
    val selectedFaction: Faction?,
    val factionReputation: Map<Faction, Int>,
) {
    companion object {
        fun fromJson(ciData: JsonObject?): CrimsonIsleData {
            val data = ciData?: JsonObject()
            val reputation = Faction.entries.associateWith {
                data.get("${it.id}_reputation").asInt(0)
            }
            val selectedFaction = data.get("selected_faction").asEnum<Faction> { it.id }
            val kuudraObject = data.getAsJsonObject("kuudra_completed_tiers")?: JsonObject()
            val kuudraStats = CrimsonIsleCodecs.KuudraCodecs.ids.map {
                KuudraEntry(
                    highestWave = kuudraObject["highest_wave_$it"].asInt(0),
                    completions = kuudraObject[it].asInt(0),
                    id = it,
                )
            }

            val dojoObject = data.getAsJsonObject("dojo")?: JsonObject()
            val dojoStats = CrimsonIsleCodecs.DojoCodecs.ids.map {
                DojoEntry(
                    time = dojoObject["dojo_time_$it"].asInt(-1),
                    points = dojoObject["dojo_points_$it"].asInt(-1),
                    id = it,
                )
            }

            return CrimsonIsleData(
                factionReputation = reputation,
                selectedFaction = selectedFaction,
                kuudraStats = kuudraStats,
                dojoStats = dojoStats
            )
        }
    }

}

enum class Faction(val id: String) {
    MAGE("mages"),
    BARBARIAN("barbarians")
}

data class DojoEntry(val points: Int, val id: String, val time: Int)
data class KuudraEntry(val highestWave: Int, val completions: Int, val id: String)
