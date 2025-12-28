package me.owdding.skyblockpv.data.api.skills.farming

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import me.owdding.lib.utils.FeatureName
import me.owdding.lib.utils.MeowddingLogger
import me.owdding.lib.utils.MeowddingLogger.Companion.featureLogger
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.data.repo.*
import me.owdding.skyblockpv.utils.json.getAs
import tech.thatgravyboat.skyblockapi.utils.extentions.*

data class GardenData(
    val copper: Int,
    val larvaConsumed: Int,
    val glowingMushroomBroken: Int,
) {
    companion object {
        val EMPTY = GardenData(0, 0, 0)
    }
}

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
    @FeatureName("GardenProfileParser")
    companion object : MeowddingLogger by SkyBlockPv.featureLogger() {
        private fun <T> JsonObject?.asGardenResourceMap(mapper: (JsonElement?) -> T): Map<GardenResource, T> {
            if (this == null) return emptyMap()

            return this.asMap { s, jsonElement -> GardenResource.getByApiId(s) to mapper(jsonElement) }
        }

        fun fromJson(result: JsonObject): GardenProfile {
            return GardenProfile(
                unlockedPlots = result.getAs<JsonArray>("unlocked_plots_ids")?.mapNotNull {
                    StaticGardenData.plots.find { plot -> it.asString == plot.id }
                } ?: emptyList(),
                selectedBarnSkin = result.get("selected_barn_skin").toBarnSkin(),
                commissionData = result.getAs<JsonObject>("commission_data").toCommissionData(),
                gardenExperience = result.get("garden_experience").asLong(0),
                unlockedBarnSkins = result.getAs<JsonArray>("unlocked_barn_skins")?.map { it.toBarnSkin() } ?: emptyList(),
                composterData = result.getAs<JsonObject>("composter_data").toComposterData(),
                resourcesCollected = result.getAs<JsonObject>("resources_collected").asGardenResourceMap { it.asLong(0) },
                cropUpgradeLevels = result.getAs<JsonObject>("crop_upgrade_levels").asGardenResourceMap { it.asShort(0) },
            )
        }

        private fun JsonObject?.toComposterData(): ComposterData {
            val upgrades = this?.getAs<JsonObject>("upgrades")
                .asMap { key, value -> ComposterUpgrade.valueOf(key.uppercase()) to value.asInt(0) }.toMutableMap()

            ComposterUpgrade.entries.forEach {
                upgrades.putIfAbsent(it, 0)
            }

            return ComposterData(
                organicMatter = this?.get("organic_matter").asDouble(0.0),
                fuel = this?.get("fuel_units").asDouble(0.0),
                compostUnits = this?.get("compost_units").asDouble(0.0),
                compostItems = this?.get("compost_items").asInt(0),
                conversionTicks = this?.get("conversion_ticks").asInt(0),
                lastUpdateTimestamp = this?.get("last_save").asLong(0),
                upgrades = upgrades,
            )
        }

        private fun JsonElement?.toBarnSkin(): StaticBarnSkin {
            return this?.asString?.let { StaticGardenData.barnSkins[it] } ?: StaticBarnSkin.UNKNOWN
        }

        private fun JsonObject?.toCommissionData(): CommissionData {
            val visits = this?.getAs<JsonObject>("visits").asMap { id, value -> id to value.asInt }
            val completed = this?.getAs<JsonObject>("completed")
            val commissions = visits.mapNotNull { (id, visit) ->
                id.let { StaticGardenData.visitors.find { it.id == id } }
                    .let {
                        if (it == null) {
                            warn("Unknown visitor with id '$id'")
                        }
                        Commission(it, visit, completed?.get(id).asInt(0), id)
                    }
            }


            return CommissionData(
                commissions = commissions,
                totalCompleted = this?.get("total_completed").asInt(0),
                uniqueVisitorsServed = this?.get("unique_npcs_served").asInt(0),
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
    val visitor: StaticVisitorData?,
    val total: Int,
    val accepted: Int,
    val visitorId: String,
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
