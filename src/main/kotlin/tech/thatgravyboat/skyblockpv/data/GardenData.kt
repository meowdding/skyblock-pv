package tech.thatgravyboat.skyblockpv.data

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.util.StringRepresentable
import org.joml.Vector2i
import tech.thatgravyboat.skyblockapi.api.data.SkyBlockRarity
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockpv.api.ItemAPI
import tech.thatgravyboat.skyblockpv.utils.*
import java.util.*

data class ProfileSpecificGardenData(
    val copper: Int,
    val larvaConsumed: Int,
)

data class GardenData(
    val unlockedPlots: List<Plot>,
    val commissionData: CommissionData,
    val composterData: ComposterData,
    val activeCommissions: List<ActiveCommission>,
    val gardenExperience: Long,
    val resourcesCollected: Map<GardenResource, Long>,
    val selectedBarnSkin: StaticBarnSkin,
    val cropUpgradeLevels: Map<GardenResource, Short>,
    val unlockedBarnSkins: List<StaticBarnSkin>,
) {
    companion object {
        private fun <T> JsonObject.asGardenResourceMap(mapper: (JsonElement?) -> T): Map<GardenResource, T> {
            return this.getAsJsonObject("resources_collected")
                .asMap { s, jsonElement -> GardenResource.getByApiId(s) to mapper(jsonElement) }
        }

        fun fromJson(result: JsonObject): GardenData {
            return GardenData(
                unlockedPlots = Plot.parseListFromJson(result.getAsJsonArray("unlocked_plots_ids")),
                commissionData = CommissionData.fromJson(result.getAsJsonObject("commission_data")),
                composterData = ComposterData.fromJson(result.getAsJsonObject("composter_data")),
                activeCommissions = ActiveCommission.parseListFromJson(result.getAsJsonObject("active_commissions")),
                gardenExperience = result.get("garden_experience").asLong(0),
                resourcesCollected = result.getAsJsonObject("resources_collected").asGardenResourceMap { it.asLong(0) },
                selectedBarnSkin = result.get("selected_barn_skin").asString.let {
                    StaticGardenData.barnSkins[it] ?: StaticBarnSkin.UNKNOWN
                },
                cropUpgradeLevels = result.getAsJsonObject("crop_upgrade_levels")
                    .asGardenResourceMap { it?.asShort ?: 0 },
                unlockedBarnSkins = result.getAsJsonArray("unlocked_barn_skins").map { it.asString("") }
                    .filterNot(String::isNullOrEmpty).map { StaticGardenData.barnSkins[it] ?: StaticBarnSkin.UNKNOWN },
            )
        }
    }
}

data class CommissionData(
    val visits: Map<VisitorId, Int>,
    val completed: Map<VisitorId, Int>,
    val totalCompleted: Int,
    val uniqueVisitorsServed: Int,
) {
    companion object {
        fun fromJson(data: JsonObject?): CommissionData {
            data ?: return CommissionData(emptyMap(), emptyMap(), 0, 0)

            return CommissionData(
                visits = data.getAsJsonObject("visits").asVisitorMap(),
                completed = data.getAsJsonObject("completed").asVisitorMap(),
                totalCompleted = data.get("total_completed").asInt(0),
                uniqueVisitorsServed = data.get("unique_npcs_served").asInt(0),
            )
        }

        private fun JsonObject?.asVisitorMap(): Map<VisitorId, Int> {
            if (this == null) return emptyMap()

            return this.asMap { id, value -> id to value.asInt }
        }
    }
}

data class Plot(val type: PlotType, val id: Int) {
    val data: StaticPlotData? = StaticGardenData.plots.find { it.id == "${type.name.lowercase()}_$id" }
    val location = data?.location ?: Vector2i(0, 0)
    val name = data?.getName()?.also { it.color = TextColor.GREEN } ?: Text.multiline(
        "Couldn't find name!",
        "Type: $type; id: $id",
        "",
        "Please report this on our discord!"
    ) // todo add red formatting

    enum class PlotType {
        BEGINNER, INTERMEDIATE, ADVANCED, EXPERT
    }

    companion object {
        fun parseListFromJson(serialized: JsonArray?): List<Plot> {
            serialized ?: return listOf()

            return serialized.asList().mapNotNull(::fromJson)
        }

        fun fromJson(serialized: JsonElement): Plot? {
            if (serialized.isJsonNull || !serialized.isJsonPrimitive || !serialized.asJsonPrimitive.isString) {
                return null
            }
            val (type, id) = serialized.asString.split("_")
            return Plot(PlotType.valueOf(type.uppercase()), id.toInt())
        }
    }
}

data class ComposterData(
    val organicMatter: Double,
    val fuel: Double,
    val compostUnits: Double,
    val compostItems: Int,
    val conversionTicks: Int,
    val lastUpdateTimestamp: Long,
    val upgrades: Map<ComposterUpgrade, Int>,
) {
    companion object {
        fun fromJson(data: JsonObject?): ComposterData {
            data ?: return ComposterData(0.0, 0.0, 0.0, 0, 0, 0L, emptyMap())

            return ComposterData(
                organicMatter = data.get("organic_matter").asDouble(0.0),
                fuel = data.get("fuel_units").asDouble(0.0),
                compostUnits = data.get("compost_units").asDouble(0.0),
                compostItems = data.get("compost_items").asInt(0),
                conversionTicks = data.get("conversion_ticks").asInt(0),
                lastUpdateTimestamp = data.get("last_save").asLong(0),
                upgrades = data.getAsJsonObject("upgrades").asComposterUpgrades()
            )
        }

        private fun JsonObject?.asComposterUpgrades(): Map<ComposterUpgrade, Int> {
            if (this == null) return emptyMap()

            val map =
                this.asMap { key, value -> ComposterUpgrade.valueOf(key.uppercase()) to value.asInt(0) }.toMutableMap()
            ComposterUpgrade.entries.forEach {
                map.putIfAbsent(it, 0)
            }
            return map
        }
    }
}

enum class ComposterUpgrade {
    SPEED, MULTI_DROP, FUEL_CAP, ORGANIC_MATTER_CAP, COST_REDUCTION
}

data class ActiveCommission(
    val visitorId: VisitorId,
    val status: ActiveCommissionStatus,
    val position: Int,
    val requirement: List<ActiveCommissionRequirement>,
) {
    companion object {
        fun parseListFromJson(data: JsonObject?): List<ActiveCommission> {
            data ?: return listOf()

            return data.asMap().toList().mapNotNull { (key, value) -> fromJson(key, value) }
        }

        private fun fromJson(id: String, data: JsonElement): ActiveCommission? {
            if (data !is JsonObject) return null

            return ActiveCommission(
                visitorId = id,
                status = ActiveCommissionStatus.valueOf(data.get("status").asString.uppercase()),
                position = data.get("position").asInt(-1),
                requirement = ActiveCommissionRequirement.parseListFromJson(data.getAsJsonArray("requirement")),
            )
        }
    }
}

data class ActiveCommissionRequirement(
    val originalItem: SkyblockItemId?,
    val originalAmount: Int?,
    val item: SkyblockItemId,
    val amount: Int,
) {
    companion object {
        fun parseListFromJson(data: JsonArray?): List<ActiveCommissionRequirement> {
            data ?: return listOf()

            return data.toList().mapNotNull(::fromJson)
        }

        private fun fromJson(data: JsonElement?): ActiveCommissionRequirement? {
            data ?: return null

            if (data !is JsonObject) return null
            return ActiveCommissionRequirement(
                originalItem = data.get("original_item")?.asString?.uppercase(),
                originalAmount = data.get("original_amount")?.asInt,
                item = data.get("item").asString("").uppercase(),
                amount = data.get("amount").asInt(0),
            )
        }
    }
}

enum class ActiveCommissionStatus {
    STARTED, NOT_STARTED
}

enum class GardenResource(internalName: String? = null, itemId: String? = null) : StringRepresentable {
    WHEAT,
    PUMPKIN,
    POTATO("POTATO_ITEM"),
    SUGAR_CANE,
    MELON,
    CARROT("CARROT_ITEM"),
    COCOA_BEANS("INK_SACK:3"),
    NETHER_WART("NETHER_STALK"),
    CACTUS,
    MUSHROOM("MUSHROOM_COLLECTION", "RED_MUSHROOM"),
    UNKNOWN;

    override fun getSerializedName() = name

    val internalName: String
    val itemId: String

    init {
        if (internalName == null) {
            this.internalName = name
        } else {
            this.internalName = internalName
        }
        if (itemId == null) {
            this.itemId = this.internalName
        } else {
            this.itemId = itemId
        }
    }

    companion object {
        fun getByApiId(s: String): GardenResource {
            return entries.find { it.internalName == s } ?: UNKNOWN
        }

        val CODEC = StringRepresentable.fromEnum { entries.toTypedArray() }
    }
}

typealias VisitorId = String
typealias SkyblockItemId = String

fun SkyblockItemId.asItemStack() = ItemAPI.getItem(this)

data object StaticGardenData {
    var barnSkins: Map<String, StaticBarnSkin> = emptyMap()
        private set
    var composterData: Map<String, StaticComposterData> = emptyMap()
        private set
    var cropMilestones: Map<GardenResource, List<Int>> = emptyMap()
        private set
    var miscData: StaticMiscData =
        StaticMiscData(emptyList(), emptyList(), "0", emptyMap(), emptyList(), emptyList(), "0")
        private set
    var plotCost: Map<String, List<StaticPlotCost>> = emptyMap()
        private set
    var plots: List<StaticPlotData> = emptyList()
        private set
    var visitors: List<StaticVisitorData> = emptyList()
        private set

    init {
        val CODEC = RecordCodecBuilder.create {
            it.group(
                Codec.unboundedMap(Codec.STRING, StaticBarnSkin.CODEC).fieldOf("barn_skins").forGetter { barnSkins },
                Codec.unboundedMap(Codec.STRING, StaticComposterData.CODEC).fieldOf("composter_data")
                    .forGetter { composterData },
                Codec.unboundedMap(GardenResource.CODEC, Codec.INT.listOf()).fieldOf("crop_milestones")
                    .forGetter { cropMilestones },
                StaticMiscData.CODEC.fieldOf("misc").forGetter { miscData },
                Codec.unboundedMap(Codec.STRING, StaticPlotCost.CODEC.listOf()).fieldOf("plot_cost")
                    .forGetter { plotCost },
                StaticPlotData.CODEC.listOf().fieldOf("plots").forGetter { plots },
                StaticVisitorData.CODEC.listOf().fieldOf("visitors").forGetter { visitors }
            ).apply(it, ::init)
        }

        val gardenData = Utils.loadFromRepo<JsonObject>("garden_data") ?: JsonObject()

        CODEC.parse(JsonOps.INSTANCE, gardenData)
    }

    fun init(
        barnSkins: Map<String, StaticBarnSkin>,
        composterData: Map<String, StaticComposterData>,
        cropMilestones: Map<GardenResource, List<Int>>,
        miscData: StaticMiscData,
        plotCost: Map<String, List<StaticPlotCost>>,
        plots: List<StaticPlotData>,
        visitors: List<StaticVisitorData>,
    ) {
        StaticGardenData.barnSkins = barnSkins
        StaticGardenData.composterData = composterData
        StaticGardenData.cropMilestones = cropMilestones
        StaticGardenData.miscData = miscData
        StaticGardenData.plotCost = plotCost
        StaticGardenData.plots = plots
        StaticGardenData.visitors = visitors
    }
}

data class StaticBarnSkin(
    val displayName: Component,
    val item: SkyblockItemId,
) {
    companion object {
        val CODEC = RecordCodecBuilder.create {
            it.group(
                CodecUtils.COMPONENT_TAG.fieldOf("displayname").forGetter(StaticBarnSkin::displayName),
                Codec.STRING.fieldOf("item").forGetter(StaticBarnSkin::item)
            ).apply(it, ::StaticBarnSkin)
        }

        val UNKNOWN = StaticBarnSkin(Text.of("Unknown") { this.color = TextColor.RED }, "barrier")
    }
}

data class StaticComposterData(
    val rewardFormula: String,
    val tooltip: String,
    val upgrade: Map<String, Int>,
) {
    companion object {
        val CODEC = RecordCodecBuilder.create {
            it.group(
                Codec.STRING.fieldOf("reward_formula").forGetter(StaticComposterData::rewardFormula),
                Codec.STRING.fieldOf("tooltip").forGetter(StaticComposterData::tooltip),
                Codec.unboundedMap(Codec.STRING, Codec.INT).fieldOf("upgrades").forGetter(StaticComposterData::upgrade)
            ).apply(it, ::StaticComposterData)
        }
    }
}

data class StaticMiscData(
    val gardenLevelBrackets: List<Int>,
    val cropUpgradeCost: List<Int>,
    val cropRewardFormula: String,
    val cropRequirements: Map<GardenResource, Int>,
    val offersAcceptedMilestones: List<Int>,
    val uniqueVisitorsAcceptedMilestone: List<Int>,
    val plotFarmingFortuneReward: String,
) {
    companion object {
        val CODEC = RecordCodecBuilder.create {
            it.group(
                Codec.INT.listOf().fieldOf("garden_level").forGetter(StaticMiscData::gardenLevelBrackets),
                Codec.INT.listOf().fieldOf("crop_upgrade_cost").forGetter(StaticMiscData::cropUpgradeCost),
                Codec.STRING.fieldOf("crop_upgrade_reward_formula").forGetter(StaticMiscData::cropRewardFormula),
                Codec.unboundedMap(GardenResource.CODEC, Codec.INT).fieldOf("crop_requirements")
                    .forGetter(StaticMiscData::cropRequirements),
                Codec.INT.listOf().fieldOf("offers_accepted_milestone")
                    .forGetter(StaticMiscData::offersAcceptedMilestones),
                Codec.INT.listOf().fieldOf("unique_visitors_served_milestone")
                    .forGetter(StaticMiscData::uniqueVisitorsAcceptedMilestone),
                Codec.STRING.fieldOf("plot_farming_fortune_reward_formula")
                    .forGetter(StaticMiscData::plotFarmingFortuneReward)
            ).apply(it, ::StaticMiscData)
        }
    }
}

data class StaticPlotCost(
    val amount: Int,
    val bundle: Boolean,
) {
    companion object {
        val CODEC = RecordCodecBuilder.create {
            it.group(
                Codec.INT.fieldOf("amount").forGetter(StaticPlotCost::amount),
                Codec.BOOL.optionalFieldOf("bundle", false).forGetter(StaticPlotCost::bundle)
            ).apply(it, ::StaticPlotCost)
        }
    }
}

data class StaticPlotData(
    val id: String,
    val location: Vector2i,
    val number: Int,
) {
    companion object {
        val CODEC = RecordCodecBuilder.create {
            it.group(
                Codec.STRING.fieldOf("id").forGetter(StaticPlotData::id),
                Codec.INT.listOf(2, 2).xmap({ Vector2i(it[0], it[1]) }, { listOf(it.x, it.y) }).fieldOf("location")
                    .forGetter(StaticPlotData::location),
                Codec.INT.fieldOf("number").forGetter(StaticPlotData::number),
            ).apply(it, ::StaticPlotData)
        }
    }

    fun getName(): MutableComponent = Text.of("Plot") {
        this.color = TextColor.YELLOW
        append(Text.of(" - ") { this.color = TextColor.GRAY })
        append(Text.of("$number") { this.color = TextColor.AQUA })
    }
}

data class StaticVisitorData(
    val rarity: SkyBlockRarity,
    val name: String,
    val id: VisitorId,
    val item: SkyblockItemId,
    val skin: String?,
) {
    companion object {
        val CODEC = RecordCodecBuilder.create {
            it.group(
                Codec.STRING.xmap({ SkyBlockRarity.valueOf(it) }, SkyBlockRarity::name).fieldOf("rarity")
                    .forGetter(StaticVisitorData::rarity),
                Codec.STRING.fieldOf("name").forGetter(StaticVisitorData::name),
                Codec.STRING.fieldOf("id").forGetter(StaticVisitorData::id),
                Codec.STRING.optionalFieldOf("item", "player_skull").fieldOf("item").forGetter(StaticVisitorData::item),
                Codec.STRING.optionalFieldOf("skin").forGetter { Optional.ofNullable(null) },
            ).apply(it, ::create)
        }

        fun create(
            rarity: SkyBlockRarity,
            name: String,
            id: VisitorId,
            item: SkyblockItemId,
            skin: Optional<String>,
        ): StaticVisitorData {
            return StaticVisitorData(rarity, name, id, item, skin.orElse(null))
        }
    }
}
