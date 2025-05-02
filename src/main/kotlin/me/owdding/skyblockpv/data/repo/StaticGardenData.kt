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
import me.owdding.ktmodules.Module
import me.owdding.lib.extensions.ItemUtils.createSkull
import me.owdding.lib.extensions.round
import me.owdding.skyblockpv.data.api.skills.farming.ComposterUpgrade
import me.owdding.skyblockpv.utils.Utils
import me.owdding.skyblockpv.utils.codecs.CodecUtils
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
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

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

@Module
data object StaticGardenData {
    lateinit var barnSkins: Map<String, StaticBarnSkin>
        private set
    lateinit var composterData: Map<ComposterUpgrade, StaticComposterData>
        private set
    lateinit var cropMilestones: Map<GardenResource, List<Int>>
        private set
    lateinit var miscData: StaticMiscData
        private set
    lateinit var plotCost: Map<String, List<StaticPlotCost>>
        private set
    lateinit var plots: List<StaticPlotData>
        private set
    lateinit var visitors: List<StaticVisitorData>
        private set
    lateinit var tools: Map<GardenResource, StaticToolInfo>
    val RARE_CROPS = listOf("CROPIE", "SQUASH", "FERMENTO", "CONDENSED_FERMENTO")
    const val COPPER = "copper"

    @IncludedCodec(named = "garden§crop_milestones")
    val CODEC: Codec<Map<GardenResource, List<Int>>> = Codec.unboundedMap(GardenResource.CODEC, CodecUtils.CUMULATIVE_INT_LIST)

    @GenerateCodec
    data class GardenData(
        @FieldName("barn_skins") val barnSkins: Map<String, StaticBarnSkin>,
        @FieldName("composter_data") val composterData: Map<ComposterUpgrade, StaticComposterData>,
        @NamedCodec("garden§crop_milestones") @FieldName("crop_milestones") val cropMilestones: Map<GardenResource, List<Int>>,
        @FieldName("misc") val miscData: StaticMiscData,
        @FieldName("plot_cost") val plotCost: Map<String, List<StaticPlotCost>>,
        val plots: List<StaticPlotData>,
        val visitors: List<StaticVisitorData>,
        val tools: Map<GardenResource, StaticToolInfo>,
    )

    init {
        init(Utils.loadRepoData<GardenData>("garden_data"))
    }

    fun init(data: GardenData) {
        barnSkins = data.barnSkins
        composterData = data.composterData
        cropMilestones = data.cropMilestones
        miscData = data.miscData
        plotCost = data.plotCost
        plots = data.plots
        visitors = data.visitors
        tools = data.tools
    }
}

@GenerateCodec
data class StaticBarnSkin(
    @NamedCodec("component_tag") @FieldName("displayname") val displayName: Component,
    val item: String,
) {
    fun getItem() = RepoItemsAPI.getItem(item)

    companion object {
        val UNKNOWN = StaticBarnSkin(Text.of("Unknown") { this.color = TextColor.RED }, "barrier")
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
        this.color = TextColor.YELLOW
        append(Text.of(" - ") { this.color = TextColor.GRAY })
        append(Text.of("$number") { this.color = TextColor.AQUA })
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

@GenerateCodec
data class StaticToolInfo(
    val type: ToolType,
    @NamedCodec("compact_string_list") @FieldName("id") val ids: List<String>,
    @NamedCodec("component_tag") @FieldName("displayname") val displayName: Component,
)
