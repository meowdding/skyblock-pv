package tech.thatgravyboat.skyblockpv.data

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import tech.thatgravyboat.skyblockapi.utils.extentions.*

data class GardenData(
    val copper: Int,
    val larvaConsumed: Int,
    val glowingMushroomBroken: Int,
)

data class GardenProfile(
    val unlockedPlots: List<StaticPlotData>,
    val selectedBarnSkin: StaticBarnSkin,
    val commissionData: CommissionData,
    val gardenExperience: Long,
    val unlockedBarnSkins: List<StaticBarnSkin>,
    val composterData: ComposterData,
    val resourcesCollected: Map<GardenResource, Long>,
    val cropUpgradeLevels: Map<GardenResource, Short>,
) {
    companion object {
        private fun <T> JsonObject?.asGardenResourceMap(mapper: (JsonElement?) -> T): Map<GardenResource, T> {
            if (this == null) return emptyMap()

            return this.asMap { s, jsonElement -> GardenResource.getByApiId(s) to mapper(jsonElement) }
        }

        fun fromJson(result: JsonObject): GardenProfile {
            return GardenProfile(
                unlockedPlots = result.getAsJsonArray("unlocked_plots_ids")
                    .mapNotNull { StaticGardenData.plots.find { plot -> it.asString == plot.id } },
                selectedBarnSkin = result.get("selected_barn_skin").toBarnSkin(),
                commissionData = result.getAsJsonObject("commission_data").toCommissionData(),
                gardenExperience = result.get("garden_experience").asLong(0),
                unlockedBarnSkins = result.getAsJsonArray("unlocked_barn_skins")?.map { it.toBarnSkin() } ?: emptyList(),
                composterData = result.getAsJsonObject("composter_data").toComposterData(),
                resourcesCollected = result.getAsJsonObject("resources_collected").asGardenResourceMap { it.asLong(0) },
                cropUpgradeLevels = result.getAsJsonObject("crop_upgrade_levels").asGardenResourceMap { it.asShort(0) },
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
            val visits = this.getAsJsonObject("visits").asMap { id, value -> id to value.asInt }
            val completed = this.getAsJsonObject("completed")
            val commissions = visits.mapNotNull { (id, visit) ->
                id.let { StaticGardenData.visitors.find { it.id == id } }
                    ?.let { Commission(it, visit, completed.get(id).asInt(0)) }.let {
                        if (it == null) {
                            println("failed to create visitor, $this")
                        }
                        it
                    }
            }


            return CommissionData(
                commissions = commissions,
                totalCompleted = this.get("total_completed").asInt(0),
                uniqueVisitorsServed = this.get("unique_npcs_served").asInt(0),
            )
        }
    }

    fun getGardenLevel(): Int = StaticGardenData.miscData.getLevelForExperience(gardenExperience)
}

data class CommissionData(
    val commissions: List<Commission>,
    val totalCompleted: Int,
    val uniqueVisitorsServed: Int,
)

data class Commission(
    val visitor: StaticVisitorData,
    val total: Int,
    val accepted: Int,
)

enum class ComposterUpgrade {
    SPEED,
    MULTI_DROP,
    FUEL_CAP,
    ORGANIC_MATTER_CAP,
    COST_REDUCTION,
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
