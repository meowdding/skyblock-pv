package tech.thatgravyboat.skyblockpv.data

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
import tech.thatgravyboat.skyblockpv.utils.CodecUtils
import tech.thatgravyboat.skyblockpv.utils.CodecUtils.eitherList
import tech.thatgravyboat.skyblockpv.utils.Utils
import java.util.*


typealias VisitorId = String
typealias SkyblockItemId = String

fun SkyblockItemId.asItemStack() = ItemAPI.getItem(this)

enum class GardenResource(internalName: String? = null, itemId: SkyblockItemId? = null) : StringRepresentable {
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
    val itemId: SkyblockItemId

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

private object GardenCodecs {
    val BARN_SKIN = RecordCodecBuilder.create {
        it.group(
            CodecUtils.COMPONENT_TAG.fieldOf("displayname").forGetter(StaticBarnSkin::displayName),
            Codec.STRING.fieldOf("item").forGetter(StaticBarnSkin::item),
        ).apply(it, ::StaticBarnSkin)
    }

    val COMPOSTER_DATA = RecordCodecBuilder.create {
        it.group(
            Codec.STRING.fieldOf("reward_formula").forGetter(StaticComposterData::rewardFormula),
            Codec.STRING.fieldOf("tooltip").forGetter(StaticComposterData::tooltip),
            Codec.unboundedMap(Codec.STRING, Codec.INT).fieldOf("upgrades").forGetter(StaticComposterData::upgrade),
        ).apply(it, ::StaticComposterData)
    }

    val MISC_DATA = RecordCodecBuilder.create {
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
                .forGetter(StaticMiscData::plotFarmingFortuneReward),
        ).apply(it, ::StaticMiscData)
    }

    val PLOT_COST = RecordCodecBuilder.create {
        it.group(
            Codec.INT.fieldOf("amount").forGetter(StaticPlotCost::amount),
            Codec.BOOL.optionalFieldOf("bundle", false).forGetter(StaticPlotCost::bundle),
        ).apply(it, ::StaticPlotCost)
    }

    val PLOT_DATA = RecordCodecBuilder.create {
        it.group(
            Codec.STRING.fieldOf("id").forGetter(StaticPlotData::id),
            Codec.INT.listOf(2, 2).xmap({ Vector2i(it[0], it[1]) }, { listOf(it.x, it.y) }).fieldOf("location")
                .forGetter(StaticPlotData::location),
            Codec.INT.fieldOf("number").forGetter(StaticPlotData::number),
        ).apply(it, ::StaticPlotData)
    }

    val VISITOR_DATA = RecordCodecBuilder.create {
        it.group(
            Codec.STRING.xmap({ SkyBlockRarity.valueOf(it) }, SkyBlockRarity::name).fieldOf("rarity")
                .forGetter(StaticVisitorData::rarity),
            Codec.STRING.fieldOf("name").forGetter(StaticVisitorData::name),
            Codec.STRING.fieldOf("id").forGetter(StaticVisitorData::id),
            Codec.STRING.optionalFieldOf("item", "player_skull").fieldOf("item").forGetter(StaticVisitorData::item),
            Codec.STRING.optionalFieldOf("skin").forGetter { Optional.ofNullable(null) },
        ).apply(it, StaticVisitorData.Companion::create)
    }

    val TOOL_INFO = RecordCodecBuilder.create {
        it.group(
            ToolType.CODEC.fieldOf("type").forGetter(StaticToolInfo::type),
            Codec.STRING.eitherList().fieldOf("id").forGetter(StaticToolInfo::ids),
            CodecUtils.COMPONENT_TAG.fieldOf("displayname").forGetter(StaticToolInfo::displayName)
        ).apply(it, ::StaticToolInfo)
    }
}

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
    var tools: List<StaticToolInfo> = emptyList()

    init {
        val CODEC = RecordCodecBuilder.create {
            it.group(
                Codec.unboundedMap(Codec.STRING, GardenCodecs.BARN_SKIN).fieldOf("barn_skins").forGetter { barnSkins },
                Codec.unboundedMap(Codec.STRING, GardenCodecs.COMPOSTER_DATA).fieldOf("composter_data")
                    .forGetter { composterData },
                Codec.unboundedMap(GardenResource.CODEC, Codec.INT.listOf()).fieldOf("crop_milestones")
                    .forGetter { cropMilestones },
                GardenCodecs.MISC_DATA.fieldOf("misc").forGetter { miscData },
                Codec.unboundedMap(Codec.STRING, GardenCodecs.PLOT_COST.listOf()).fieldOf("plot_cost")
                    .forGetter { plotCost },
                GardenCodecs.PLOT_DATA.listOf().fieldOf("plots").forGetter { plots },
                GardenCodecs.VISITOR_DATA.listOf().fieldOf("visitors").forGetter { visitors },
                GardenCodecs.TOOL_INFO.listOf().fieldOf("tools").forGetter { tools },
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
        tools: List<StaticToolInfo>,
    ) {
        StaticGardenData.barnSkins = barnSkins
        StaticGardenData.composterData = composterData
        StaticGardenData.cropMilestones = cropMilestones
        StaticGardenData.miscData = miscData
        StaticGardenData.plotCost = plotCost
        StaticGardenData.plots = plots
        StaticGardenData.visitors = visitors
        StaticGardenData.tools = tools
    }
}

data class StaticBarnSkin(
    val displayName: Component,
    val item: SkyblockItemId,
) {
    companion object {
        val UNKNOWN = StaticBarnSkin(Text.of("Unknown") { this.color = TextColor.RED }, "barrier")
    }
}

data class StaticComposterData(
    val rewardFormula: String,
    val tooltip: String,
    val upgrade: Map<String, Int>,
)

data class StaticMiscData(
    val gardenLevelBrackets: List<Int>,
    val cropUpgradeCost: List<Int>,
    val cropRewardFormula: String,
    val cropRequirements: Map<GardenResource, Int>,
    val offersAcceptedMilestones: List<Int>,
    val uniqueVisitorsAcceptedMilestone: List<Int>,
    val plotFarmingFortuneReward: String,
)

data class StaticPlotCost(
    val amount: Int,
    val bundle: Boolean,
)

data class StaticPlotData(
    val id: String,
    val location: Vector2i,
    val number: Int,
) {
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

enum class ToolType(val id: String) : StringRepresentable {
    HOE("icon/slot/hoe"),
    AXE("icon/slot/axe");

    override fun getSerializedName() = name.lowercase()

    companion object {
        val CODEC = StringRepresentable.fromEnum { entries.toTypedArray() }
    }
}

private fun toArmorSet(baseId: String) = listOf(
    "${baseId}_HELMET",
    "${baseId}_LEGGINGS",
    "${baseId}_BOOTS",
    "${baseId}_CHESTPLATE"
)

enum class FarmingEquipment(vararg ids: String) {
    ARMOR(
        *listOf(
            "FARM_SUIT",
            "FARM_ARMOR",
            "SPEEDSTER",
            "RABBIT",
            "MELON",
            "CROPIE",
            "SQUASH",
            "FERMENTO",
            "BIOHAZARD"
        ).flatMap { toArmorSet(it) }.toTypedArray(),
        "RANCHERS_BOOTS",
        "FARMER_BOOTS"
    ),
    BELTS(
        "LOTUS_BELT",
        "PESTHUNTERS_BELT"
    ),
    CLOAKS(
        "LOTUS_CLOAK",
        "ZORROS_CAPE",
        "PESTHUNTERS_CLOAK"
    ),
    NECKLACES(
        "LOTUS_NECKLACE",
        "PESTHUNTERS_NECKLACE"
    ),
    GLOVES(
        "LOTUS_BRACELET",
        "PESTHUNTERS_GLOVES"
    ),
    VACUUM(
        "SKYMART_VACUUM",
        "SKYMART_TURBO_VACUUM",
        "SKYMART_HYPER_VACUUM",
        "INFINI_VACUUM",
        "INFINI_VACUUM_HOOVERIUS"
    ),
    PETS(
        "HEDGEHOG",
        "MOOSHROOM_COW",
        "SLUG",
        "ELEPHANT"
    );

    val list = ids.toList()

    companion object {
        val cloaks = CLOAKS.list
        val gloves = GLOVES.list
        val necklaces = NECKLACES.list
        val belts = BELTS.list
        val equipment = listOf(cloaks, gloves, necklaces, belts).flatten()
        val armor = ARMOR.list
        val vaccum = VACUUM.list
        val pets = PETS.list
    }
}

data class StaticToolInfo(
    val type: ToolType,
    val ids: List<String>,
    val displayName: Component,
)
