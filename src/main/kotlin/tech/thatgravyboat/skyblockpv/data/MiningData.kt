package tech.thatgravyboat.skyblockpv.data


import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import com.notkamui.keval.KevalBuilder
import com.notkamui.keval.keval
import eu.pb4.placeholders.api.ParserContext
import eu.pb4.placeholders.api.parsers.TagParser
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.util.ExtraCodecs.LateBoundIdMapper
import net.minecraft.util.StringRepresentable
import org.joml.Vector2i
import tech.thatgravyboat.skyblockapi.utils.Logger
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockpv.utils.*

data class MiningCore(
    val nodes: Map<String, Int>,
    val crystals: Map<String, Crystal>,
    val experience: Long,
    val powderMithril: Int,
    val powderSpentMithril: Int,
    val powderGemstone: Int,
    val powderSpentGemstone: Int,
    val powderGlacite: Int,
    val powderSpentGlacite: Int,
    val toggledNodes: List<String>,
    val miningAbility: String,
) {
    val levelToExp = mapOf(
        1 to 0,
        2 to 3_000,
        3 to 12_000,
        4 to 37_000,
        5 to 97_000,
        6 to 197_000,
        7 to 347_000,
        8 to 557_000,
        9 to 847_000,
        10 to 1_247_000,
    )

    fun getHotmLevel(): Int = levelToExp.entries.findLast { it.value <= experience }?.key ?: 0
    fun getXpToNextLevel() = experience - (levelToExp[getHotmLevel()] ?: 0)
    fun getXpRequiredForNextLevel(): Int {
        val level = (getHotmLevel() + 1).coerceAtMost(10)
        return (levelToExp[level] ?: 0) - (levelToExp[level - 1] ?: 0)
    }

    fun getAbilityLevel(): Int {
        val cotmLevel = nodes["special_0"] ?: 0
        return if (cotmLevel < 1) {
            1
        } else {
            2
        }
    }
}

data class Crystal(
    val state: String,
    val totalPlaced: Int,
    val totalFound: Int,
)

data class Forge(
    val slots: Map<Int, ForgeSlot>,
) {
    companion object {
        fun fromJson(json: JsonObject): Forge {
            val forge = json.getAsJsonObject("forge_processes")?.getAsJsonObject("forge_1") ?: JsonObject()

            return forge.asMap { key, value ->
                val obj = value.asJsonObject
                val slot = ForgeSlot(
                    type = obj["type"].asString(""),
                    id = obj["id"].asString(""),
                    startTime = obj["startTime"].asLong(0),
                    notified = obj["notified"].asBoolean(false),
                )

                key.toInt() to slot
            }.let { Forge(it) }
        }
    }
}

data class ForgeSlot(
    val type: String,
    val id: String,
    val startTime: Long,
    val notified: Boolean,
)

enum class PowderType(val formatting: ChatFormatting) : StringRepresentable {
    MITHRIL(ChatFormatting.DARK_GREEN),
    GEMSTONE(ChatFormatting.LIGHT_PURPLE),
    GLACITE(ChatFormatting.AQUA);

    override fun getSerializedName() = name

    companion object {
        val CODEC: Codec<PowderType> = StringRepresentable.fromEnum { entries.toTypedArray() }
    }
}

object MiningNodes {
    val ID_MAPPER: LateBoundIdMapper<String, MapCodec<out MiningNode>> = LateBoundIdMapper()
    val CODEC: Codec<MiningNode> = ID_MAPPER.codec(Codec.STRING).dispatch({ it.type() }, { it })

    val miningNodes: List<MiningNode>

    init {
        ID_MAPPER.put("perk", LevelingMiningNode.CODEC)
        ID_MAPPER.put("unlevelable", UnlevelableMiningNode.CODEC)
        ID_MAPPER.put("ability", AbilityMiningNode.CODEC)
        ID_MAPPER.put("core", CoreMiningNode.CODEC)
        ID_MAPPER.put("tier", TierNode.CODEC)

        val hotm = Utils.loadFromRepo<JsonArray>("hotm")

        val parse = CODEC.listOf().parse(JsonOps.INSTANCE, hotm!!)
        miningNodes = if (parse.isError) {
            Logger.error(parse.error().get().messageSupplier.get())
            mutableListOf()
        } else {
            parse.partialOrThrow
        }
    }

}
// todo unknown perks in chat

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

interface MiningNode {
    val name: String
    val id: String
    val location: Vector2i

    fun type(): MapCodec<out MiningNode>
    fun tooltip(context: Context): List<Component>
    fun isMaxed(level: Int): Boolean
}

interface LevelableTooltipNode : MiningNode {
    val rewards: Map<String, String>
    val tooltip: List<String>

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


interface LevelableMiningNode : MiningNode {
    val maxLevel: Int

    fun costForLevel(level: Int): Pair<PowderType, Int>
    fun getPowderType(level: Int): PowderType
}

class UnlevelableMiningNode(
    override val id: String,
    override val name: String,
    override val location: Vector2i,
    val rewardFormula: String,
    val tooltip: List<String>,
) : MiningNode {
    companion object {
        val CODEC: MapCodec<UnlevelableMiningNode> = RecordCodecBuilder.mapCodec {
            it.group(
                Codec.STRING.fieldOf("id").forGetter(UnlevelableMiningNode::id),
                Codec.STRING.fieldOf("name").forGetter(UnlevelableMiningNode::name),
                vectorCodec.fieldOf("location").forGetter(UnlevelableMiningNode::location),
                Codec.STRING.optionalFieldOf("reward_formula", "0").forGetter(UnlevelableMiningNode::rewardFormula),
                Codec.STRING.listOf().fieldOf("tooltip").forGetter(UnlevelableMiningNode::tooltip),
            ).apply(it, ::UnlevelableMiningNode)
        }
    }

    override fun type(): MapCodec<UnlevelableMiningNode> = CODEC


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

private val rewardFormulaCodec = Codec.either(
    Codec.STRING.xmap({ mapOf("reward" to it) }, { it.get("reward") }),
    Codec.unboundedMap(Codec.STRING, Codec.STRING) as Codec<Map<String, String>>,
).xmap(
    { Either.unwrap(it) },
    { if (it.size == 1) Either.left(it) else Either.right(it) },
)

private val vectorCodec = Codec.INT.listOf(2, 2).xmap({ Vector2i(it[0], it[1]) }, { listOf(it.x, it.y) })

class LevelingMiningNode(
    override val id: String,
    override val name: String,
    override val location: Vector2i,
    override val maxLevel: Int,
    val powderType: PowderType,
    val costFormula: String,
    override val rewards: Map<String, String>,
    override val tooltip: List<String>,
) : LevelableMiningNode, LevelableTooltipNode {
    companion object {
        val CODEC = RecordCodecBuilder.mapCodec {
            it.group(
                Codec.STRING.fieldOf("id").forGetter(LevelingMiningNode::id),
                Codec.STRING.fieldOf("name").forGetter(LevelingMiningNode::name),
                vectorCodec.fieldOf("location").forGetter(LevelingMiningNode::location),
                Codec.INT.fieldOf("max_level").forGetter(LevelingMiningNode::maxLevel),
                PowderType.CODEC.fieldOf("powder_type").forGetter(LevelingMiningNode::powderType),
                Codec.STRING.fieldOf("cost_formula").forGetter(LevelingMiningNode::costFormula),
                rewardFormulaCodec.fieldOf("reward_formula").forGetter(LevelingMiningNode::rewards),
                Codec.STRING.listOf().fieldOf("tooltip").forGetter(LevelingMiningNode::tooltip),
            ).apply(it, ::LevelingMiningNode)
        }
    }

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

    override fun type(): MapCodec<LevelingMiningNode> = CODEC

    override fun isMaxed(level: Int) = level >= maxLevel
}

class AbilityMiningNode(
    override val id: String,
    override val name: String,
    override val location: Vector2i,
    override val rewards: Map<String, String>,
    override val tooltip: List<String>,
) : LevelableTooltipNode {
    companion object {
        val CODEC: MapCodec<AbilityMiningNode> = RecordCodecBuilder.mapCodec {
            it.group(
                Codec.STRING.fieldOf("id").forGetter(AbilityMiningNode::id),
                Codec.STRING.fieldOf("name").forGetter(AbilityMiningNode::name),
                vectorCodec.fieldOf("location").forGetter(AbilityMiningNode::location),
                rewardFormulaCodec.fieldOf("reward_formula").forGetter(AbilityMiningNode::rewards),
                Codec.STRING.listOf().fieldOf("tooltip").forGetter(AbilityMiningNode::tooltip),
            ).apply(it, ::AbilityMiningNode)
        }
    }

    override fun type(): MapCodec<AbilityMiningNode> = CODEC

    override fun isMaxed(level: Int) = level == 3
}

class CoreMiningNode(
    override val id: String,
    override val name: String,
    override val location: Vector2i,
    val level: List<CotmLevel>,
) : LevelableMiningNode {
    companion object {
        val CODEC: MapCodec<CoreMiningNode> = RecordCodecBuilder.mapCodec {
            it.group(
                Codec.STRING.fieldOf("id").forGetter(CoreMiningNode::id),
                Codec.STRING.fieldOf("name").forGetter(CoreMiningNode::name),
                vectorCodec.fieldOf("location").forGetter(CoreMiningNode::location),
                CotmLevel.CODEC.listOf().fieldOf("level").forGetter(CoreMiningNode::level),
            ).apply(it, ::CoreMiningNode)
        }
    }

    data class CotmCost(val type: PowderType, val amount: Int) {
        fun toPair() = type to amount

        companion object {
            val CODEC: Codec<CotmCost> = RecordCodecBuilder.create {
                it.group(
                    PowderType.CODEC.optionalFieldOf("type", PowderType.MITHRIL).forGetter(CotmCost::type),
                    Codec.INT.optionalFieldOf("amount", 0).forGetter(CotmCost::amount),
                ).apply(it, ::CotmCost)
            }
        }
    }

    data class CotmLevel(val cost: CotmCost, val include: List<Int>, val reward: List<String>) {
        companion object {
            private val rewardCodec = Codec.either(
                Codec.STRING.listOf(),
                Codec.STRING.xmap({ listOf(it) }, { it.first() }),
            ).xmap(
                { Either.unwrap(it) },
                { if (it.size > 1) Either.left(it) else Either.right(it) },
            )

            val CODEC: Codec<CotmLevel> = RecordCodecBuilder.create {
                it.group(
                    CotmCost.CODEC.fieldOf("cost").forGetter(CotmLevel::cost),
                    Codec.INT.listOf().optionalFieldOf("include", emptyList()).forGetter(CotmLevel::include),
                    rewardCodec.fieldOf("reward").forGetter(CotmLevel::reward),
                ).apply(it, ::CotmLevel)
            }
        }

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

    override fun type(): MapCodec<CoreMiningNode> = CODEC

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

class TierNode(override val name: String, override val id: String, override val location: Vector2i, val rewards: List<String>) : MiningNode {
    companion object {
        val CODEC: MapCodec<TierNode> = RecordCodecBuilder.mapCodec {
            it.group(
                Codec.STRING.fieldOf("name").forGetter(TierNode::name),
                Codec.STRING.fieldOf("id").forGetter(TierNode::id),
                vectorCodec.fieldOf("location").forGetter(TierNode::location),
                Codec.STRING.listOf().fieldOf("rewards").forGetter(TierNode::rewards),
            ).apply(it, ::TierNode)
        }
    }

    override fun type() = CODEC

    override fun tooltip(context: Context) = rewards.map {
        TagParser.QUICK_TEXT_SAFE.parseText(it, ParserContext.of())
    }

    override fun isMaxed(level: Int) = level >= (location.y + 1)
}
