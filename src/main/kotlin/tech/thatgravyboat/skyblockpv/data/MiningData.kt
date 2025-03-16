package tech.thatgravyboat.skyblockpv.data

import com.google.gson.JsonArray
import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import com.notkamui.keval.KevalBuilder
import com.notkamui.keval.keval
import eu.pb4.placeholders.api.ParserContext
import eu.pb4.placeholders.api.parsers.TagParser
import net.minecraft.network.chat.Component
import net.minecraft.util.ExtraCodecs.LateBoundIdMapper
import net.minecraft.util.StringRepresentable
import org.joml.Vector2i
import tech.thatgravyboat.skyblockapi.utils.Logger
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockpv.utils.Utils


import com.google.gson.JsonObject
import tech.thatgravyboat.skyblockpv.utils.asBoolean
import tech.thatgravyboat.skyblockpv.utils.asLong
import tech.thatgravyboat.skyblockpv.utils.asMap
import tech.thatgravyboat.skyblockpv.utils.asString

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

enum class PowderType : StringRepresentable {
    MITHRIL,
    GEMSTONE,
    GLACITE;

    override fun getSerializedName() = name

    companion object {
        val CODEC = StringRepresentable.fromEnum { entries.toTypedArray() }
    }
}

object MiningNodes {
    val ID_MAPPER: LateBoundIdMapper<String, MapCodec<out MiningNode>> = LateBoundIdMapper()
    val CODEC: Codec<MiningNode> = ID_MAPPER.codec(Codec.STRING).dispatch({ it.type() }, { it })

    val miningNodes: List<MiningNode>

    init {
        ID_MAPPER.put("perk", LevelingMiningNode.CODEC)
        ID_MAPPER.put("unlevelable", UnlevelableMiningNode.CODEC)

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
    val rewards: Map<String, String>,
    val tooltip: List<String>,
) : LevelableMiningNode {
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

    private fun evaluate(context: Context): Map<String, String> {
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
            TagParser.QUICK_TEXT_SAFE.parseText(it.let {
                var text = it
                replacement.forEach { entry ->
                    text = text.replace("%${entry.key}%", entry.value)
                }
                text
            }, ParserContext.of())
        }
    }

    override fun isMaxed(level: Int) = level >= maxLevel
}
