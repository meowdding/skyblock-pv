package me.owdding.skyblockpv.data.repo

import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import com.notkamui.keval.keval
import eu.pb4.placeholders.api.ParserContext
import eu.pb4.placeholders.api.parsers.TagParser
import me.owdding.ktcodecs.FieldName
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktcodecs.IncludedCodec
import me.owdding.ktcodecs.NamedCodec
import me.owdding.lib.extensions.ItemUtils.createSkull
import me.owdding.lib.extensions.round
import me.owdding.skyblockpv.data.api.skills.farming.ComposterUpgrade
import me.owdding.skyblockpv.utils.Utils
import me.owdding.skyblockpv.utils.codecs.CodecUtils
import me.owdding.skyblockpv.utils.codecs.DefaultedData
import me.owdding.skyblockpv.utils.codecs.ExtraData
import me.owdding.skyblockpv.utils.codecs.LoadData
import me.owdding.skyblockpv.utils.theme.PvColors
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.util.StringRepresentable
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import org.joml.Vector2i
import tech.thatgravyboat.skyblockapi.api.data.SkyBlockRarity
import tech.thatgravyboat.skyblockapi.api.remote.RepoItemsAPI
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

enum class GreenhouseUpgrade {
    GROWTH_SPEED,
    YIELD,
    PLOT_LIMIT,
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
    MOONFLOWER,
    SUNFLOWER("DOUBLE_PLANT"),
    WILD_ROSE,
    UNKNOWN;

    override fun getSerializedName() = internalName

    val internalName: String = internalName ?: name
    val itemId: String = itemId ?: this.internalName

    fun getItem() = RepoItemsAPI.getItem(itemId.replace(":", "-"))

    companion object {
        fun getByApiId(s: String) = entries.find { it.internalName == s } ?: UNKNOWN

        val actualValues = GardenResource.entries.filterNot { it == UNKNOWN }

        @IncludedCodec(keyable = true)
        val CODEC: Codec<GardenResource> = StringRepresentable.fromEnum { entries.toTypedArray() }
    }
}

object GardenCodecs {

    @IncludedCodec
    val PLOT_COST: Codec<StaticPlotCost> =
        Codec.either(
            Codec.INT.xmap({ StaticPlotCost(it, false) }, { it.amount }),
            RecordCodecBuilder.create {
                it.group(
                    Codec.INT.fieldOf("amount").forGetter(StaticPlotCost::amount),
                    Codec.BOOL.optionalFieldOf("bundle", false).forGetter(StaticPlotCost::bundle),
                ).apply(it, ::StaticPlotCost)
            },
        ).xmap({ Either.unwrap(it) }, { if (it.bundle) Either.right(it) else Either.left(it) })
}

@GenerateCodec
data class GreenhouseUpgradeData(
    @FieldName("reward_formula") val rewardFormula: String,
    val format: CompostNumberFormat,
    @NamedCodec("component_tag") val name: Component,
    val tooltip: String,
    @NamedCodec("cum_string_int_map") @FieldName("upgrades") val upgrade: List<Map<String, Int>>,
) {
    fun getRewardForLevel(level: Int): Int {
        return rewardFormula.keval {
            includeDefault()
            function {
                name = "clamp"
                arity = 3
                implementation = { (value, min, max) -> Math.clamp(value, min, max) }
            }
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

@LoadData
data object StaticGardenData : DefaultedData {
    private var data: GardenData? = null

    val barnSkins: Map<String, DefaultBarnSkin> get() = data?.barnSkins ?: emptyMap()
    val composterData: Map<ComposterUpgrade, StaticComposterData> get() = data?.composterData ?: emptyMap()
    val cropMilestones: Map<GardenResource, List<Int>> get() = data?.cropMilestones ?: emptyMap()
    val miscData: StaticMiscData get() = data?.miscData ?: StaticMiscData.DEFAULT
    val plotCost: Map<String, List<StaticPlotCost>> get() = data?.plotCost ?: emptyMap()
    val plots: List<StaticPlotData> get() = data?.plots ?: emptyList()
    val visitors: List<StaticVisitorData> get() = data?.visitors ?: emptyList()
    val chips: List<Int> get() = data?.chips ?: emptyList()
    val mutations: List<MutationData> get() = data?.mutations ?: emptyList()
    val tools: Map<GardenResource, StaticToolInfo> get() = data?.tools ?: emptyMap()
    val greenhouseUpgrades: Map<GreenhouseUpgrade, GreenhouseUpgradeData> get() = data?.greenhouseUpgrades ?: emptyMap()

    val RARE_CROPS = listOf("CROPIE", "SQUASH", "FERMENTO", "CONDENSED_FERMENTO")
    const val COPPER = "copper"

    @IncludedCodec(named = "garden§crop_milestones")
    val CODEC: Codec<Map<GardenResource, List<Int>>> = Codec.unboundedMap(GardenResource.CODEC, CodecUtils.CUMULATIVE_INT_LIST)

    @GenerateCodec
    data class GardenData(
        @FieldName("barn_skins") val barnSkins: Map<String, DefaultBarnSkin>,
        @FieldName("composter_data") val composterData: Map<ComposterUpgrade, StaticComposterData>,
        @NamedCodec("garden§crop_milestones") @FieldName("crop_milestones") val cropMilestones: Map<GardenResource, List<Int>>,
        @FieldName("misc") val miscData: StaticMiscData,
        @NamedCodec("cum_int_list") val chips: List<Int>,
        @FieldName("plot_cost") val plotCost: Map<String, List<StaticPlotCost>>,
        val mutations: List<MutationData>,
        val plots: List<StaticPlotData>,
        val visitors: List<StaticVisitorData>,
        val tools: Map<GardenResource, StaticToolInfo>,
        @FieldName("greenhouse_upgrades") val greenhouseUpgrades: Map<GreenhouseUpgrade, GreenhouseUpgradeData>,
    )

    override suspend fun load() {
        init(Utils.loadRemoteRepoData<GardenData>("pv/garden_data"))
    }

    fun init(data: GardenData) {
        this.data = data
    }
}

@GenerateCodec
data class DefaultBarnSkin(
    @NamedCodec("component_tag") @FieldName("displayname") val displayName: Component,
    val item: String,
) {
    fun getItem(): ItemStack = RepoItemsAPI.getItem(item).copy().apply {
        set(DataComponents.CUSTOM_NAME, this@DefaultBarnSkin.displayName)
    }

    companion object {
        val UNKNOWN = DefaultBarnSkin(Text.of("Unknown") { this.color = PvColors.RED }, "barrier")
    }
}

enum class CompostNumberFormat(val formatting: (Int) -> String) {
    PERCENTAGE({ it.round() }),
    INTEGER({ it.toFormattedString() }),
    ;

    fun format(number: Int) = formatting(number)
}

@GenerateCodec
data class StaticComposterData(
    @FieldName("reward_formula") val rewardFormula: String,
    val format: CompostNumberFormat,
    val tooltip: String,
    @NamedCodec("item") val item: Item,
    @NamedCodec("component_tag") val name: Component,
    @NamedCodec("cum_string_int_map") @FieldName("upgrades") val upgrade: List<Map<String, Int>>,
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

@GenerateCodec
data class MutationData(
    val id: String,
    val name: String,
    val rarity: SkyBlockRarity,
    val analyzable: Boolean = true,
)

@GenerateCodec
data class StaticMiscData(
    @NamedCodec("cum_int_list") @FieldName("garden_level") val gardenLevelBrackets: List<Int>,
    @FieldName("crop_upgrade_cost") val cropUpgradeCost: List<Int>,
    @FieldName("crop_upgrade_reward_formula") val cropRewardFormula: String,
    @FieldName("crop_requirements") val cropRequirements: Map<GardenResource, Int>,
    @FieldName("offers_accepted_milestone") val offersAcceptedMilestones: List<Int>,
    @FieldName("unique_visitors_served_milestone") val uniqueVisitorsAcceptedMilestone: List<Int>,
    @FieldName("plot_farming_fortune_reward_formula") val plotFarmingFortuneReward: String,
    @FieldName("max_larva_consumed") val maxLarvaConsumed: Int,
    @FieldName("personal_bests") val personalBests: Map<GardenResource, Int>,
    @NamedCodec("cum_string_int_map") @FieldName("farming_level_cap") val farmingLevelCap: List<Map<String, Int>>,
    @NamedCodec("cum_string_int_map") @FieldName("extra_farming_fortune") val bonusDrops: List<Map<String, Int>>,
) {
    companion object {
        val DEFAULT = StaticMiscData(
            emptyList(),
            emptyList(),
            "level",
            emptyMap(),
            emptyList(),
            emptyList(),
            "level",
            5,
            emptyMap(),
            emptyList(),
            emptyList(),
        )
    }

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

@GenerateCodec
data class StaticPlotData(
    val id: String,
    @NamedCodec("vec_2i") val location: Vector2i,
    val number: Int,
) {
    val type: String = id.substringBefore("_")
    fun getName(): MutableComponent = Text.of("Plot") {
        this.color = PvColors.YELLOW
        append(Text.of(" - ") { this.color = PvColors.GRAY })
        append(Text.of("$number") { this.color = PvColors.AQUA })
    }
}

@GenerateCodec
data class StaticVisitorData(
    val rarity: SkyBlockRarity,
    val name: String,
    val id: String,
    val item: String = "player_head",
    val skin: String?,
) {

    val itemStack: ItemStack by lazy {
        skin?.let { createSkull(it) } ?: RepoItemsAPI.getItem(item).takeUnless { it.item == Items.BARRIER } ?: Utils.getMinecraftItem(item)
    }
}

enum class ToolType(val id: String) {
    HOE("icon/slot/hoe"),
    AXE("icon/slot/axe"),
    ;
}

enum class FarmingGear {
    ARMOR,
    BELTS,
    CLOAKS,
    NECKLACES,
    GLOVES,
    VACUUM,
    WATERING_CAN,
    PETS,
    ;

    var list: List<String> = emptyList()
        private set

    companion object {
        init {
            Utils.loadFromRemoteRepo<Map<String, List<String>>>("pv/gear/farming")?.forEach { (key, value) ->
                runCatching { valueOf(key.uppercase()).list = value }.onFailure { it.printStackTrace() }
            }
        }

        val cloaks = CLOAKS.list
        val gloves = GLOVES.list
        val necklaces = NECKLACES.list
        val belts = BELTS.list
        val armor = ARMOR.list
        val vacuum = VACUUM.list
        val watering_can = WATERING_CAN.list
        val pets = PETS.list
    }
}

@GenerateCodec
data class StaticToolInfo(
    val type: ToolType,
    @NamedCodec("compact_string_list") @FieldName("id") val ids: List<String>,
    @NamedCodec("component_tag") @FieldName("displayname") val displayName: Component,
)
