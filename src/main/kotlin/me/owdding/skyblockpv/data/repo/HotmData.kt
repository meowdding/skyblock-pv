package me.owdding.skyblockpv.data.repo

import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import com.notkamui.keval.KevalBuilder
import com.notkamui.keval.keval
import eu.pb4.placeholders.api.ParserContext
import eu.pb4.placeholders.api.parsers.TagParser
import me.owdding.ktcodecs.*
import me.owdding.skyblockpv.data.api.skills.PowderType
import me.owdding.skyblockpv.generated.DispatchHelper
import me.owdding.skyblockpv.utils.Utils
import me.owdding.skyblockpv.utils.codecs.CodecUtils
import me.owdding.skyblockpv.utils.codecs.ExtraData
import me.owdding.skyblockpv.utils.codecs.LoadData
import net.minecraft.network.chat.Component
import org.joml.Vector2i
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import kotlin.reflect.KClass

@GenerateDispatchCodec(MiningNode::class)
enum class MiningNodes(override val type: KClass<out MiningNode>) : DispatchHelper<MiningNode> {
    PERK(LevelingMiningNode::class),
    UNLEVELABLE(UnlevelableMiningNode::class),
    ABILITY(AbilityMiningNode::class),
    CORE(CoreMiningNode::class),
    TIER(TierNode::class),
    SPACER(SpacerNode::class),
    ;

    @LoadData
    companion object : ExtraData {
        @IncludedCodec(named = "hotm§reward_formula")
        val rewardFormulaCodec: Codec<Map<String, String>> = Codec.either(
            Codec.STRING.xmap({ mapOf("reward" to it) }, { it["reward"] }),
            Codec.unboundedMap(Codec.STRING, Codec.STRING) as Codec<Map<String, String>>,
        ).xmap(
            { Either.unwrap(it) },
            { if (it.size == 1) Either.left(it) else Either.right(it) },
        )
        lateinit var miningNodes: List<MiningNode>

        fun getType(id: String) = valueOf(id.uppercase())

        override fun load() {
            miningNodes = Utils.loadRepoData("hotm", CodecUtils.list())
        }
    }
}

data class Context(val hotmLevel: Int = -1, val perkLevel: Int = -1) {
    fun configure(kevalBuilder: KevalBuilder) = with(kevalBuilder) {
        function {
            name = "min"
            arity = 2
            implementation = DoubleArray::min
        }
        constant {
            name = "level"
            value = perkLevel.coerceAtLeast(1).toDouble()
        }
        constant {
            name = "hotmLevel"
            value = hotmLevel.coerceAtLeast(1).toDouble()
        }
        constant {
            name = "effectiveLevel"
            value = (perkLevel - 1).coerceAtLeast(0).toDouble()
        }
    }
}

abstract class MiningNode(val type: MiningNodes) {
    abstract val name: String
    abstract val id: String
    abstract val location: Vector2i

    abstract fun tooltip(context: Context): List<Component>
    abstract fun isMaxed(level: Int): Boolean
}

abstract class LevelableTooltipNode(type: MiningNodes) : MiningNode(type) {
    abstract val rewards: Map<String, String>
    abstract val tooltip: List<String>

    fun evaluate(context: Context): Map<String, String> {
        return rewards.mapValues {
            it.value.keval {
                includeDefault()
                context.configure(this)
            }.toFormattedString()
        }
    }

    override fun tooltip(context: Context): List<Component> {
        val replacement = evaluate(context)

        return tooltip.map {
            TagParser.QUICK_TEXT_SAFE.parseText(
                it.let {
                    var text = it
                    replacement.forEach { entry ->
                        text = text.replace("%${entry.key}%", entry.value)
                    }
                    text
                },
                ParserContext.of(),
            )
        }
    }
}


abstract class LevelableMiningNode(type: MiningNodes) : LevelableTooltipNode(type) {
    abstract val maxLevel: Int

    abstract fun costForLevel(level: Int): Pair<PowderType, Int>
    abstract fun getPowderType(level: Int): PowderType
}

@GenerateCodec
data class UnlevelableMiningNode(
    override val id: String,
    override val name: String,
    @NamedCodec("vec_2i") override val location: Vector2i,
    @FieldName("reward_formula") val rewardFormula: String = "0",
    val tooltip: List<String>,
) : MiningNode(MiningNodes.UNLEVELABLE) {
    private fun evaluate(context: Context): Double? {
        if (rewardFormula.isEmpty() || rewardFormula == "0") return null

        return rewardFormula.keval {
            includeDefault()
            context.configure(this)
        }
    }

    override fun tooltip(context: Context): List<Component> {
        val replacement = evaluate(context)?.toFormattedString() ?: ""

        return tooltip.map {
            TagParser.QUICK_TEXT_SAFE.parseText(it.replace("%reward%", replacement), ParserContext.of())
        }
    }

    override fun isMaxed(level: Int) = level > 0
}

@GenerateCodec
data class LevelingMiningNode(
    override val id: String,
    override val name: String,
    @NamedCodec("vec_2i") override val location: Vector2i,
    @FieldName("max_level") override val maxLevel: Int,
    @FieldName("powder_type") val powderType: PowderType,
    @FieldName("cost_formula") val costFormula: String,
    @NamedCodec("hotm§reward_formula") @FieldName("reward_formula") override val rewards: Map<String, String>,
    override val tooltip: List<String>,
) : LevelableMiningNode(MiningNodes.PERK) {
    override fun costForLevel(level: Int): Pair<PowderType, Int> {
        return getPowderType(level) to costFormula.keval {
            includeDefault()
            constant {
                name = "level"
                value = level.toDouble()
            }
            constant {
                name = "nextLevel"
                value = (level + 1).toDouble()
            }
        }.toInt()
    }

    override fun getPowderType(level: Int): PowderType = powderType

    override fun isMaxed(level: Int) = level >= maxLevel
}

@GenerateCodec
data class AbilityMiningNode(
    override val id: String,
    override val name: String,
    @NamedCodec("vec_2i") override val location: Vector2i,
    @NamedCodec("hotm§reward_formula") @FieldName("reward_formula") override val rewards: Map<String, String>,
    override val tooltip: List<String>,
) : LevelableTooltipNode(MiningNodes.ABILITY) {
    override fun isMaxed(level: Int) = level == 3
}

@GenerateCodec
data class CoreMiningNode(
    override val id: String,
    override val name: String,
    @NamedCodec("vec_2i") override val location: Vector2i,
    val level: List<CotmLevel>,
) : LevelableMiningNode(MiningNodes.CORE) {

    @GenerateCodec
    data class CotmCost(
        val type: PowderType = PowderType.MITHRIL,
        val amount: Int = 0,
    ) {
        fun toPair() = type to amount
    }

    @GenerateCodec
    data class CotmLevel(
        val cost: CotmCost,
        val include: List<Int> = emptyList(),
        @NamedCodec("compact_string_list") val reward: List<String>,
    ) {
        fun tooltip(miningNode: CoreMiningNode): List<String> {
            val tooltip = mutableListOf<String>()

            include.map { miningNode.getLevel(it) }.flatMap { it.reward }.forEach { tooltip.add(it) }
            tooltip.addAll(reward)

            return tooltip
        }
    }

    fun getLevel(level: Int): CotmLevel {
        return this.level[(level - 1).coerceAtLeast(0)]
    }

    override val rewards: Map<String, String>
        get() = emptyMap()
    override val tooltip: List<String>
        get() = emptyList()

    override fun tooltip(context: Context): List<Component> {
        return getLevel(context.perkLevel).tooltip(this).map {
            TagParser.QUICK_TEXT_SAFE.parseText(it, ParserContext.of())
        }
    }

    override fun isMaxed(level: Int) = level == maxLevel
    override val maxLevel: Int = level.size
    override fun costForLevel(level: Int) = this.level[level - 1].cost.toPair()
    override fun getPowderType(level: Int) = this.level[level - 1].cost.type
}

@GenerateCodec
data class TierNode(
    override val name: String,
    @NamedCodec("vec_2i") override val location: Vector2i,
    val rewards: List<String>,
) : MiningNode(MiningNodes.TIER) {
    override val id: String = ""
    override fun tooltip(context: Context) = rewards.map {
        TagParser.QUICK_TEXT_SAFE.parseText(it, ParserContext.of())
    }

    override fun isMaxed(level: Int) = level >= (location.y + 1)
}

@GenerateCodec
data class SpacerNode(
    @NamedCodec("vec_2i") override val location: Vector2i,
    @NamedCodec("vec_2i") val size: Vector2i,
) : MiningNode(MiningNodes.SPACER) {
    override val name: String = ""
    override val id: String = ""

    override fun tooltip(context: Context) = emptyList<Component>()
    override fun isMaxed(level: Int) = false
}
