package tech.thatgravyboat.skyblockpv.data.repo

import com.google.gson.JsonObject
import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import com.mojang.serialization.codecs.RecordCodecBuilder
import com.notkamui.keval.keval
import eu.pb4.placeholders.api.ParserContext
import eu.pb4.placeholders.api.parsers.TagParser
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.util.ExtraCodecs
import net.minecraft.util.StringRepresentable
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import org.joml.Vector2i
import tech.thatgravyboat.skyblockapi.api.data.SkyBlockRarity
import tech.thatgravyboat.skyblockapi.api.remote.RepoItemsAPI
import tech.thatgravyboat.skyblockapi.utils.codecs.EnumCodec
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockpv.api.ItemAPI
import tech.thatgravyboat.skyblockpv.data.api.skills.farming.ComposterUpgrade
import tech.thatgravyboat.skyblockpv.utils.Utils
import tech.thatgravyboat.skyblockpv.utils.Utils.round
import tech.thatgravyboat.skyblockpv.utils.codecs.CodecUtils
import tech.thatgravyboat.skyblockpv.utils.createSkull
import java.util.*

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

    override fun getSerializedName() = internalName

    val internalName: String = internalName ?: name
    val itemId: String = itemId ?: this.internalName

    fun getItem() = ItemAPI.getItem(itemId.replace(":", "-"))

    companion object {
        fun getByApiId(s: String) = entries.find { it.internalName == s } ?: UNKNOWN

        val actualValues = GardenResource.entries.filterNot { it == UNKNOWN }

        val CODEC = StringRepresentable.fromEnum { entries.toTypedArray() }
    }
}

private object GardenCodecs {
    val COMPOSTER_UPGRADE = EnumCodec.of(ComposterUpgrade.entries.toTypedArray())
    val BARN_SKIN = RecordCodecBuilder.create {
        it.group(
            CodecUtils.COMPONENT_TAG.fieldOf("displayname").forGetter(StaticBarnSkin::displayName),
            Codec.STRING.fieldOf("item").forGetter(StaticBarnSkin::item),
        ).apply(it, ::StaticBarnSkin)
    }

    val COMPOST_NUMBER_CODEC = EnumCodec.of(CompostNumberFormat.entries.toTypedArray())

    val COMPOSTER_DATA = RecordCodecBuilder.create {
        it.group(
            Codec.STRING.fieldOf("reward_formula").forGetter(StaticComposterData::rewardFormula),
            COMPOST_NUMBER_CODEC.fieldOf("format").forGetter(StaticComposterData::format),
            Codec.STRING.fieldOf("tooltip").forGetter(StaticComposterData::tooltip),
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(StaticComposterData::item),
            CodecUtils.COMPONENT_TAG.fieldOf("name").forGetter(StaticComposterData::name),
            CodecUtils.CUMULATIVE_STRING_INT_MAP.fieldOf("upgrades").forGetter(StaticComposterData::upgrade),
        ).apply(it, ::StaticComposterData)
    }

    val MISC_DATA = RecordCodecBuilder.create {
        it.group(
            CodecUtils.CUMULATIVE_INT_LIST.fieldOf("garden_level").forGetter(StaticMiscData::gardenLevelBrackets),
            Codec.INT.listOf().fieldOf("crop_upgrade_cost").forGetter(StaticMiscData::cropUpgradeCost),
            Codec.STRING.fieldOf("crop_upgrade_reward_formula").forGetter(StaticMiscData::cropRewardFormula),
            Codec.unboundedMap(GardenResource.CODEC, Codec.INT).fieldOf("crop_requirements").forGetter(StaticMiscData::cropRequirements),
            Codec.INT.listOf().fieldOf("offers_accepted_milestone")
                .forGetter(StaticMiscData::offersAcceptedMilestones),
            Codec.INT.listOf().fieldOf("unique_visitors_served_milestone")
                .forGetter(StaticMiscData::uniqueVisitorsAcceptedMilestone),
            Codec.STRING.fieldOf("plot_farming_fortune_reward_formula")
                .forGetter(StaticMiscData::plotFarmingFortuneReward),
            Codec.INT.fieldOf("max_larva_consumed").forGetter(StaticMiscData::maxLarvaConsumed),
            Codec.unboundedMap(GardenResource.CODEC, Codec.INT).fieldOf("personal_bests").forGetter(StaticMiscData::cropRequirements),
            CodecUtils.CUMULATIVE_STRING_INT_MAP.fieldOf("farming_level_cap").forGetter(StaticMiscData::farmingLevelCap),
            CodecUtils.CUMULATIVE_STRING_INT_MAP.fieldOf("extra_farming_fortune").forGetter(StaticMiscData::bonusDrops),
        ).apply(it, ::StaticMiscData)
    }

    val PLOT_COST =
        Codec.either(
            Codec.INT.xmap({ StaticPlotCost(it, false) }, { it.amount }),
            RecordCodecBuilder.create {
                it.group(
                    Codec.INT.fieldOf("amount").forGetter(StaticPlotCost::amount),
                    Codec.BOOL.optionalFieldOf("bundle", false).forGetter(StaticPlotCost::bundle),
                ).apply(it, ::StaticPlotCost)
            },
        ).xmap({ Either.unwrap(it) }, { if (it.bundle) Either.right(it) else Either.left(it) })

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
            Codec.STRING.optionalFieldOf("item", "player_skull").forGetter(StaticVisitorData::item),
            Codec.STRING.optionalFieldOf("skin").forGetter { Optional.ofNullable(null) },
        ).apply(it, StaticVisitorData.Companion::create)
    }

    val TOOL_INFO = RecordCodecBuilder.create {
        it.group(
            ToolType.CODEC.fieldOf("type").forGetter(StaticToolInfo::type),
            ExtraCodecs.compactListCodec(Codec.STRING).fieldOf("id").forGetter(StaticToolInfo::ids),
            CodecUtils.COMPONENT_TAG.fieldOf("displayname").forGetter(StaticToolInfo::displayName),
        ).apply(it, ::StaticToolInfo)
    }
}

data object StaticGardenData {
    var barnSkins: Map<String, StaticBarnSkin> = emptyMap()
        private set
    var composterData: Map<ComposterUpgrade, StaticComposterData> = emptyMap()
        private set
    var cropMilestones: Map<GardenResource, List<Int>> = emptyMap()
        private set
    var miscData: StaticMiscData =
        StaticMiscData(emptyList(), emptyList(), "0", emptyMap(), emptyList(), emptyList(), "0", 0, emptyMap(), emptyList(), emptyList())
        private set
    var plotCost: Map<String, List<StaticPlotCost>> = emptyMap()
        private set
    var plots: List<StaticPlotData> = emptyList()
        private set
    var visitors: List<StaticVisitorData> = emptyList()
        private set
    var tools: Map<GardenResource, StaticToolInfo> = emptyMap()
    val RARE_CROPS = listOf("CROPIE", "SQUASH", "FERMENTO", "CONDENSED_FERMENTO")
    const val COPPER = "copper"

    init {
        val CODEC = RecordCodecBuilder.create {
            it.group(
                Codec.unboundedMap(Codec.STRING, GardenCodecs.BARN_SKIN).fieldOf("barn_skins").forGetter { barnSkins },
                Codec.unboundedMap(GardenCodecs.COMPOSTER_UPGRADE, GardenCodecs.COMPOSTER_DATA).fieldOf("composter_data").forGetter { composterData },
                Codec.unboundedMap(GardenResource.CODEC, CodecUtils.CUMULATIVE_INT_LIST).fieldOf("crop_milestones").forGetter { cropMilestones },
                GardenCodecs.MISC_DATA.fieldOf("misc").forGetter { miscData },
                Codec.unboundedMap(Codec.STRING, GardenCodecs.PLOT_COST.listOf()).fieldOf("plot_cost").forGetter { plotCost },
                GardenCodecs.PLOT_DATA.listOf().fieldOf("plots").forGetter { plots },
                GardenCodecs.VISITOR_DATA.listOf().fieldOf("visitors").forGetter { visitors },
                Codec.unboundedMap(GardenResource.CODEC, GardenCodecs.TOOL_INFO).fieldOf("tools").forGetter { tools },
            ).apply(it, StaticGardenData::init)
        }

        val gardenData = Utils.loadFromRepo<JsonObject>("garden_data") ?: JsonObject()

        CODEC.parse(JsonOps.INSTANCE, gardenData).let {
            if (it.isError) {
                throw RuntimeException(it.error().get().message())
            }
        }
    }

    fun init(
        barnSkins: Map<String, StaticBarnSkin>,
        composterData: Map<ComposterUpgrade, StaticComposterData>,
        cropMilestones: Map<GardenResource, List<Int>>,
        miscData: StaticMiscData,
        plotCost: Map<String, List<StaticPlotCost>>,
        plots: List<StaticPlotData>,
        visitors: List<StaticVisitorData>,
        tools: Map<GardenResource, StaticToolInfo>,
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
    val item: String,
) {
    fun getItem() = ItemAPI.getItem(item)

    companion object {
        val UNKNOWN = StaticBarnSkin(Text.of("Unknown") { this.color = TextColor.RED }, "barrier")
    }
}

enum class CompostNumberFormat(val formatting: (Int) -> String) {
    PERCENTAGE({ it.round() }),
    INTEGER({ it.toFormattedString() });

    fun format(number: Int) = formatting(number)
}

data class StaticComposterData(
    val rewardFormula: String,
    val format: CompostNumberFormat,
    val tooltip: String,
    val item: Item,
    val name: Component,
    val upgrade: List<Map<String, Int>>,
) {
    fun getRewardForLevel(level: Int): Int {
        return rewardFormula.keval {
            includeDefault()
            constant {
                name = "level"
                value = level.toDouble()
            }
        }.toInt()
    }

    fun getTooltipForLevel(level: Int): Component {
        return TagParser.QUICK_TEXT_SAFE.parseText(tooltip.replace("%reward%", format.format(getRewardForLevel(level))), ParserContext.of())
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
    val maxLarvaConsumed: Int,
    val personalBests: Map<GardenResource, Int>,
    val farmingLevelCap: List<Map<String, Int>>,
    val bonusDrops: List<Map<String, Int>>,
) {
    fun getXpRequired(gardenLevel: Int): Int {
        if (gardenLevel >= gardenLevelBrackets.size - 1) {
            return 0
        }

        return gardenLevelBrackets[gardenLevel] - gardenLevelBrackets[(gardenLevel - 1).coerceAtLeast(0)]
    }

    fun getLevelForExperience(gardenExperience: Long): Int {
        return StaticGardenData.miscData.gardenLevelBrackets.let { data ->
            (data.findLast { it <= gardenExperience } ?: 0).let { data.indexOf(it) + 1 }
        }
    }
}

data class StaticPlotCost(
    val amount: Int,
    val bundle: Boolean,
) {
    fun getDisplay() = RepoItemsAPI.getItemName("COMPOST".takeUnless { bundle } ?: "ENCHANTED_COMPOST")
}

data class StaticPlotData(
    val id: String,
    val location: Vector2i,
    val number: Int,
) {
    val type: String = id.substringBefore("_")
    fun getName(): MutableComponent = Text.of("Plot") {
        this.color = TextColor.YELLOW
        append(Text.of(" - ") { this.color = TextColor.GRAY })
        append(Text.of("$number") { this.color = TextColor.AQUA })
    }
}

data class StaticVisitorData(
    val rarity: SkyBlockRarity,
    val name: String,
    val id: String,
    val item: String,
    val skin: String?,
) {
    companion object {
        fun create(
            rarity: SkyBlockRarity,
            name: String,
            id: String,
            item: String,
            skin: Optional<String>,
        ): StaticVisitorData {
            return StaticVisitorData(rarity, name, id, item, skin.orElse(null))
        }
    }

    val itemStack: ItemStack by lazy {
        skin?.let { createSkull(it) } ?: ItemAPI.getItem(item).takeUnless { it.item == Items.BARRIER } ?: Utils.getMinecraftItem(item)
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

enum class FarmingGear {
    ARMOR,
    BELTS,
    CLOAKS,
    NECKLACES,
    GLOVES,
    VACUUM,
    PETS;

    var list: List<String> = emptyList()
        private set

    companion object {
        init {
            Utils.loadFromRepo<Map<String, List<String>>>("gear/farming")?.forEach { (key, value) ->
                runCatching { valueOf(key.uppercase()).list = value }.onFailure { it.printStackTrace() }
            }
        }

        val cloaks = CLOAKS.list
        val gloves = GLOVES.list
        val necklaces = NECKLACES.list
        val belts = BELTS.list
        val armor = ARMOR.list
        val vacuum = VACUUM.list
        val pets = PETS.list
    }
}

data class StaticToolInfo(
    val type: ToolType,
    val ids: List<String>,
    val displayName: Component,
)
