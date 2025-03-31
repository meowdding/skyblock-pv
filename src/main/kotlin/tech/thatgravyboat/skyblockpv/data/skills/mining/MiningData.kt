package tech.thatgravyboat.skyblockpv.data.skills.mining


import com.google.gson.JsonObject
import com.mojang.serialization.Codec
import net.minecraft.ChatFormatting
import net.minecraft.util.StringRepresentable
import tech.thatgravyboat.skyblockapi.api.data.SkyBlockRarity
import tech.thatgravyboat.skyblockapi.utils.extentions.*
import tech.thatgravyboat.skyblockpv.api.ItemAPI
import tech.thatgravyboat.skyblockpv.utils.Utils
import tech.thatgravyboat.skyblockpv.utils.getPath

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

    fun getAbilityLevel() = 1.takeIf { (nodes["special_0"] ?: 0) < 1 } ?: 2

    companion object {
        fun fromJson(json: JsonObject): MiningCore {
            val nodes = json.getAsJsonObject("nodes").asMap { id, amount -> id to amount.asInt(0) }.filterKeys { !it.startsWith("toggle_") }
            val toggledNodes = json.getAsJsonObject("nodes").entrySet().filter { it.key.startsWith("toggle") }
                .map { it.key.removePrefix("toggle_") to it.value.asBoolean(true) }
                .filterNot { it.second }
                .map { it.first }
            val crystals = json.getAsJsonObject("crystals").asMap { id, data ->
                val obj = data.asJsonObject
                id to Crystal(
                    state = obj["state"].asString(""),
                    totalPlaced = obj["total_placed"].asInt(0),
                    totalFound = obj["total_found"].asInt(0),
                )
            }

            return MiningCore(
                nodes = nodes,
                toggledNodes = toggledNodes,
                crystals = crystals,
                experience = json["experience"].asLong(0),
                powderMithril = json["powder_mithril"].asInt(0),
                powderSpentMithril = json["powder_spent_mithril"].asInt(0),
                powderGemstone = json["powder_gemstone"].asInt(0),
                powderSpentGemstone = json["powder_spent_gemstone"].asInt(0),
                powderGlacite = json["powder_glacite"].asInt(0),
                powderSpentGlacite = json["powder_spent_glacite"].asInt(0),
                miningAbility = json["selected_pickaxe_ability"].asString(""),
            )
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
            val forge = json.getPath("forge_processes.forge_1") ?: JsonObject()

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
) {
    val itemStack by lazy {
        if (type == "PETS") {
            ItemAPI.getPet(id, SkyBlockRarity.LEGENDARY, 100)
        } else {
            ItemAPI.getItem(id)
        }
    }
}

data class GlaciteData(
    val fossilsDonated: List<String>,
    val fossilDust: Int,
    val corpsesLooted: Map<String, Int>,
    val mineshaftsEntered: Int,
) {
    companion object {
        fun fromJson(json: JsonObject): GlaciteData {
            return GlaciteData(
                fossilsDonated = json.getAsJsonArray("fossils_donated").map { it.asString("") },
                fossilDust = json["fossil_dust"].asInt(0),
                corpsesLooted = json.getAsJsonObject("corpses_looted").asMap { id, amount -> id to amount.asInt(0) },
                mineshaftsEntered = json["mineshafts_entered"].asInt(0),
            )
        }
    }
}

enum class MiningGear {
    PICKAXES,
    ARMOR,
    BELTS,
    CLOAKS,
    NECKLACES,
    GLOVES,
    CHISELS,
    ;

    var list: List<String> = emptyList()
        private set

    companion object {
        init {
            Utils.loadFromRepo<Map<String, List<String>>>("gear/mining")?.forEach { (key, value) ->
                runCatching { MiningGear.valueOf(key.uppercase()).list = value }.onFailure { it.printStackTrace() }
            }
        }

        val cloaks = CLOAKS.list
        val gloves = GLOVES.list
        val necklaces = NECKLACES.list
        val belts = BELTS.list
        val equipment = listOf(cloaks, gloves, necklaces, belts).flatten()
        val armor = ARMOR.list
        val pickaxes = PICKAXES.list
        val chisels = CHISELS.list
    }
}

enum class RockBrackets(val oresRequired: Int, val rarity: SkyBlockRarity) {
    COMMON(2500, SkyBlockRarity.COMMON),
    UNCOMMON(7500, SkyBlockRarity.UNCOMMON),
    RARE(20000, SkyBlockRarity.RARE),
    EPIC(100000, SkyBlockRarity.EPIC),
    LEGENDARY(250000, SkyBlockRarity.LEGENDARY);

    companion object {
        fun getByOres(kills: Int): RockBrackets? {
            return RockBrackets.entries.reversed().firstOrNull { it.oresRequired <= kills }
        }
    }
}

enum class PowderType(val formatting: ChatFormatting) : StringRepresentable {
    MITHRIL(ChatFormatting.DARK_GREEN),
    GEMSTONE(ChatFormatting.LIGHT_PURPLE),
    GLACITE(ChatFormatting.AQUA);

    override fun getSerializedName() = name

    companion object {
        val CODEC: Codec<PowderType> = StringRepresentable.fromEnum { entries.toTypedArray() }
    }
}

object FossilTypes {

    data class Fossil(
        val id: String,
        val name: String,
        val pet: String,
    )

    var fossils: List<Fossil> = listOf()
        private set

    init {
        Utils.loadFromRepo<JsonObject>("fossils")?.asMap { id, data ->
            val obj = data.asJsonObject
            id to Fossil(id, obj["name"].asString(""), obj["pet"].asString(""))
        }?.map { it.value }?.let { fossils = it }
    }
}
