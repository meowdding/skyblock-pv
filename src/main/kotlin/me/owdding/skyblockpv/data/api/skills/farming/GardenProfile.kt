package me.owdding.skyblockpv.data.api.skills.farming

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import me.owdding.lib.utils.FeatureName
import me.owdding.lib.utils.MeowddingLogger
import me.owdding.lib.utils.MeowddingLogger.Companion.featureLogger
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.api.data.parseInvData
import me.owdding.skyblockpv.api.data.parseItemFromCustomData
import me.owdding.skyblockpv.config.Config
import me.owdding.skyblockpv.data.api.skills.farming.ToolkitEntry.Companion.fromJson
import me.owdding.skyblockpv.data.repo.DefaultBarnSkin
import me.owdding.skyblockpv.data.repo.GardenResource
import me.owdding.skyblockpv.data.repo.GreenhouseUpgrade
import me.owdding.skyblockpv.data.repo.StaticGardenData
import me.owdding.skyblockpv.data.repo.StaticPlotData
import me.owdding.skyblockpv.data.repo.StaticVisitorData
import me.owdding.skyblockpv.utils.json.getAs
import net.minecraft.world.item.ItemStack
import org.joml.Vector2i
import tech.thatgravyboat.skyblockapi.api.repo.LazyItemStack
import tech.thatgravyboat.skyblockapi.api.repo.apis.SkyBlockItemsRepo
import tech.thatgravyboat.skyblockapi.api.repo.v2.ExperimentalRepo
import tech.thatgravyboat.skyblockapi.utils.extentions.asDouble
import tech.thatgravyboat.skyblockapi.utils.extentions.asInt
import tech.thatgravyboat.skyblockapi.utils.extentions.asLong
import tech.thatgravyboat.skyblockapi.utils.extentions.asMap
import tech.thatgravyboat.skyblockapi.utils.extentions.asShort
import tech.thatgravyboat.skyblockapi.utils.extentions.filterKeysNotNull
import java.util.concurrent.CompletableFuture

data class GardenData(
    val copper: Int,
    val larvaConsumed: Int,
    val glowingMushroomBroken: Int,
    val analyzedCrops: Set<String>,
    val discoveredCrops: Set<String>,
) {
    companion object {
        val EMPTY = GardenData(0, 0, 0, emptySet(), emptySet())
    }
}

data class ToolkitEntry(
    val item: ItemStack?,
    val inUse: Boolean,
) {
    companion object : MeowddingLogger by SkyBlockPv.featureLogger("ToolkitEntry") {
        fun JsonObject.fromJson(id: String, type: Int = 0): CompletableFuture<ToolkitEntry> {
            if (!Config.useExperimentalRepo) return COMPLETED_EMPTY

            val inUse = this.getAs<Boolean>("IN_USE.$id.$type") ?: false

            val idEntries = this.getAs<JsonArray>(id) ?: return COMPLETED_EMPTY
            val entry = idEntries.filterIsInstance<JsonObject>().getOrNull(type) ?: return COMPLETED_EMPTY

            @OptIn(ExperimentalRepo::class)
            val itemData = entry.parseItemFromCustomData().firstOrNull() ?: return COMPLETED_EMPTY
            return itemData.thenApply {
                ToolkitEntry(it, inUse)
            }.exceptionallyCompose {
                CompletableFuture.failedStage(RuntimeException(id, it))
            }
        }

        val EMPTY = ToolkitEntry(null, false)
        val COMPLETED_EMPTY: CompletableFuture<ToolkitEntry> = CompletableFuture.completedFuture(EMPTY)
    }
}

data class FarmingToolkit(
    val isUnlocked: Boolean,

    val cactus: ToolkitEntry = ToolkitEntry.EMPTY,
    val carrot: ToolkitEntry = ToolkitEntry.EMPTY,
    val cocoaBeans: ToolkitEntry = ToolkitEntry.EMPTY,
    val melon: ToolkitEntry = ToolkitEntry.EMPTY,
    val mushroom: ToolkitEntry = ToolkitEntry.EMPTY,
    val netherStalk: ToolkitEntry = ToolkitEntry.EMPTY,
    val potato: ToolkitEntry = ToolkitEntry.EMPTY,
    val pumpkin: ToolkitEntry = ToolkitEntry.EMPTY,
    val sugarCane: ToolkitEntry = ToolkitEntry.EMPTY,
    val sunflower: ToolkitEntry = ToolkitEntry.EMPTY,
    val wheat: ToolkitEntry = ToolkitEntry.EMPTY,
    val wildRose: ToolkitEntry = ToolkitEntry.EMPTY,
) {
    val items = listOfNotNull(
        cactus.item,
        carrot.item,
        cocoaBeans.item,
        melon.item,
        mushroom.item,
        netherStalk.item,
        potato.item,
        pumpkin.item,
        sugarCane.item,
        sunflower.item,
        wheat.item,
        wildRose.item,
    ).takeUnless { it.isEmpty() }

    companion object {
        fun fromJson(json: JsonObject): CompletableFuture<FarmingToolkit> = with(json) {
            fun create(id: String, type: Int = 0) = this.fromJson(id, type)

            val cactus = create("CACTUS")
            val carrot = create("CARROT")
            val cacoaBeans = create("COCOA_BEANS")
            val melon = create("MELON")
            val mushroom = create("MUSHROOM")
            val netherStalk = create("NETHER_STALK")
            val potato = create("POTATO")
            val pumpkin = create("PUMPKIN")
            val sugarCane = create("SUGAR_CANE")
            val sunflower = create("SUNFLOWER")
            val wheat = create("WHEAT")
            val wildRose = create("WILD_ROSE")

            return CompletableFuture.allOf(cactus, carrot, cacoaBeans, melon, mushroom, netherStalk, potato, pumpkin, sugarCane, sunflower, wheat, wildRose)
                .thenApply {
                    FarmingToolkit(
                        this.getAs("IS_UNLOCKED", false),
                        cactus.get(),
                        carrot.get(),
                        cacoaBeans.get(),
                        melon.get(),
                        mushroom.get(),
                        netherStalk.get(),
                        potato.get(),
                        pumpkin.get(),
                        sugarCane.get(),
                        sunflower.get(),
                        wheat.get(),
                        wildRose.get(),
                    )
                }
        }

        val EMPTY = FarmingToolkit(false)
    }
}

data class GardenProfile(
    val unlockedPlots: List<StaticPlotData>,
    val selectedBarnSkin: ItemStack,
    val commissionData: CommissionData,
    val gardenExperience: Long,
    val unlockedBarnSkins: List<ItemStack>,
    val composterData: ComposterData,
    val resourcesCollected: Map<GardenResource, Long>,
    val cropUpgradeLevels: Map<GardenResource, Short>,
    val greenhouseSlots: List<Vector2i>,
    val greenhouseUpgrades: Map<GreenhouseUpgrade, Int>,
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
                selectedBarnSkin = result.get("selected_barn_skin").toBarnSkin().create(),
                commissionData = result.getAs<JsonObject>("commission_data").toCommissionData(),
                gardenExperience = result.get("garden_experience").asLong(0),
                unlockedBarnSkins = result.getAs<JsonArray>("unlocked_barn_skins")?.map { it.toBarnSkin().create() } ?: emptyList(),
                composterData = result.getAs<JsonObject>("composter_data").toComposterData(),
                resourcesCollected = result.getAs<JsonObject>("resources_collected").asGardenResourceMap { it.asLong(0) },
                cropUpgradeLevels = result.getAs<JsonObject>("crop_upgrade_levels").asGardenResourceMap { it.asShort(0) },
                greenhouseSlots = result.getAs<JsonArray>("greenhouse_slots")?.filterIsInstance<JsonObject>()?.map {
                    Vector2i(it.get("x").asInt(0), it.get("z").asInt(0))
                } ?: emptyList(),
                greenhouseUpgrades = result.getAs<JsonObject>("garden_upgrades").asMap { string, element ->
                    runCatching { GreenhouseUpgrade.valueOf(string) }.getOrNull() to element.asInt(0)
                }.filterKeysNotNull(),
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

        private fun JsonElement?.toBarnSkin(): LazyItemStack {
            val string = this?.asString ?: return DefaultBarnSkin.UNKNOWN.getItem()
            return StaticGardenData.barnSkins[string]?.getItem() ?: SkyBlockItemsRepo.getLazyItemStack("${string}_BARN_SKIN") ?: DefaultBarnSkin.UNKNOWN.getItem()
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
