package tech.thatgravyboat.skyblockpv.data

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import tech.thatgravyboat.skyblockpv.utils.asDouble
import tech.thatgravyboat.skyblockpv.utils.asInt
import tech.thatgravyboat.skyblockpv.utils.asLong
import tech.thatgravyboat.skyblockpv.utils.asMap
import tech.thatgravyboat.skyblockpv.utils.asMap as asMap1

data class GardenData(
    val copper: Int,
    val larvaConsumed: Int,
)

typealias GardenExperience = Long

fun GardenExperience.toLevel(): Int {
    val cumulativeList = StaticGardenData.miscData.gardenLevelBrackets.runningFold(0, Int::plus)
    return cumulativeList.find { it <= this }?.let { cumulativeList.indexOf(it) } ?: 0
}

data class GardenProfile(
    val unlockedPlots: List<StaticPlotData>,
    val selectedBarnSkin: StaticBarnSkin,
    val commissionData: CommissionData,
    val gardenExperience: GardenExperience,
    val unlockedBarnSkins: List<StaticBarnSkin>,
    val composterData: ComposterData,
) {
    companion object {
        fun fromJson(result: JsonObject): GardenProfile {
            return GardenProfile(
                unlockedPlots = result.getAsJsonArray("unlocked_plots_ids")
                    .mapNotNull { StaticGardenData.plots.find { plot -> it.asString == plot.id } },
                selectedBarnSkin = result.get("selected_barn_skin").toBarnSkin(),
                commissionData = result.getAsJsonObject("commission_data").toCommissionData(),
                gardenExperience = result.get("garden_experience").asLong(0),
                unlockedBarnSkins = result.getAsJsonArray("unlocked_barn_skins")?.map { it.toBarnSkin() }?: emptyList(),
                composterData = result.getAsJsonObject("composter_data").toComposterData()
            )
        }

        private fun JsonObject.toComposterData(): ComposterData {
            val upgrades = this.getAsJsonObject("upgrades")
                .asMap { key, value -> ComposterUpgrade.valueOf(key.uppercase()) to value.asInt(0) }.toMutableMap()

            ComposterUpgrade.entries.forEach {
                upgrades.putIfAbsent(it, 0)
            }

            return ComposterData(
                organicMatter = this.get("organic_matter").asDouble(0.0),
                fuel = this.get("fuel_units").asDouble(0.0),
                compostUnits = this.get("compost_units").asDouble(0.0),
                compostItems = this.get("compost_items").asInt(0),
                conversionTicks = this.get("conversion_ticks").asInt(0),
                lastUpdateTimestamp = this.get("last_save").asLong(0),
                upgrades = upgrades,
            )
        }

        private fun JsonElement?.toBarnSkin(): StaticBarnSkin {
            return this?.asString?.let { StaticGardenData.barnSkins[it] } ?: StaticBarnSkin.UNKNOWN
        }

        private fun JsonObject.toCommissionData(): CommissionData {
            val visits = this.getAsJsonObject("visits").asMap1 { id, value -> id to value.asInt }
            val completed = this.getAsJsonObject("completed")
            val commissions = visits.mapNotNull { (id, visit) ->
                id.let { StaticGardenData.visitors.find { it.id == id } }
                    ?.let { Commission(it, visit, completed.get(id).asInt(0)) }
            }

            return CommissionData(
                commissions = commissions,
                totalCompleted = this.get("total_completed").asInt(0),
                uniqueVisitorsServed = this.get("unique_npcs_served").asInt(0),
            )
        }
    }
}

data class CommissionData(
    val commissions: List<Commission>,
    val totalCompleted: Int,
    val uniqueVisitorsServed: Int,
)

data class Commission(
    val visitor: StaticVisitorData,
    val accepted: Int,
    val total: Int,
)

enum class ComposterUpgrade {
    SPEED,
    MULTI_DROP,
    FUEL_CAP,
    ORGANIC_MATTER_CAP,
    COST_REDUCTION
}

data class ComposterData(
    val organicMatter: Double,
    val fuel: Double,
    val compostUnits: Double,
    val compostItems: Int,
    val conversionTicks: Int,
    val lastUpdateTimestamp: Long,
    val upgrades: Map<ComposterUpgrade, Int>,
)
