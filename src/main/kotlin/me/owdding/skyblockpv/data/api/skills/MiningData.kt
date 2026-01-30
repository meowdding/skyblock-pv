package me.owdding.skyblockpv.data.api.skills


import com.google.gson.JsonObject
import me.owdding.skyblockpv.api.data.abstraction.HotmDataGetter
import me.owdding.skyblockpv.utils.Utils
import me.owdding.skyblockpv.utils.codecs.ExtraData
import me.owdding.skyblockpv.utils.codecs.LoadData
import me.owdding.skyblockpv.utils.json.getAs
import net.minecraft.ChatFormatting
import tech.thatgravyboat.skyblockapi.api.data.SkyBlockRarity
import tech.thatgravyboat.skyblockapi.api.remote.PetQuery
import tech.thatgravyboat.skyblockapi.api.remote.RepoItemsAPI
import tech.thatgravyboat.skyblockapi.api.remote.RepoPetsAPI
import tech.thatgravyboat.skyblockapi.utils.extentions.*
import tech.thatgravyboat.skyblockapi.utils.json.getPath

data class MiningCore(
    override val nodes: Map<String, Int>,
    val crystals: Map<String, Crystal>,
    override val experience: Long,
    val powderMithril: Int,
    val powderSpentMithril: Int,
    val powderGemstone: Int,
    val powderSpentGemstone: Int,
    val powderGlacite: Int,
    val powderSpentGlacite: Int,
    override val toggledNodes: List<String>,
    override val miningAbility: String?,
): HotmDataGetter {

    companion object {
        fun fromJson(json: JsonObject): MiningCore {
            val nodes = json.getAs<JsonObject>("nodes").asMap { id, amount -> id to amount.asInt(0) }.filterKeys { !it.startsWith("toggle_") }
            val toggledNodes = json.getAs<JsonObject>("nodes")?.entrySet()?.filter { it.key.startsWith("toggle") }
                ?.map { it.key.removePrefix("toggle_") to it.value.asBoolean(true) }
                ?.filterNot { it.second }
                ?.map { it.first } ?: emptyList()
            val crystals = json.getAs<JsonObject>("crystals").asMap { id, data ->
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
                miningAbility = json["selected_pickaxe_ability"].asString(),
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
            RepoPetsAPI.getPetAsItem(PetQuery(id, SkyBlockRarity.LEGENDARY, 100))
        } else {
            RepoItemsAPI.getItem(id)
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
                fossilsDonated = json["fossils_donated"].asList { it.asString("") },
                fossilDust = json["fossil_dust"].asInt(0),
                corpsesLooted = json.getAs<JsonObject>("corpses_looted").asMap { id, amount -> id to amount.asInt(0) },
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
    SUSPICIOUS_SCRAP,
    ;

    var list: List<String> = emptyList()
        private set

    companion object {
        init {
            Utils.loadFromRepo<Map<String, List<String>>>("gear/mining")?.forEach { (key, value) ->
                runCatching { valueOf(key.uppercase()).list = value }.onFailure { it.printStackTrace() }
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
        val suspicious_scrap = SUSPICIOUS_SCRAP.list
    }
}

enum class RockBracket(val oresRequired: Int, val rarity: SkyBlockRarity) {
    COMMON(2500, SkyBlockRarity.COMMON),
    UNCOMMON(7500, SkyBlockRarity.UNCOMMON),
    RARE(20000, SkyBlockRarity.RARE),
    EPIC(100000, SkyBlockRarity.EPIC),
    LEGENDARY(250000, SkyBlockRarity.LEGENDARY);

    companion object {
        fun getByOres(kills: Int): RockBracket? {
            return RockBracket.entries.reversed().firstOrNull { it.oresRequired <= kills }
        }
    }
}

enum class PowderType(val formatting: ChatFormatting) {
    MITHRIL(ChatFormatting.DARK_GREEN),
    GEMSTONE(ChatFormatting.LIGHT_PURPLE),
    GLACITE(ChatFormatting.AQUA);
}

@LoadData
object FossilTypes : ExtraData {

    data class Fossil(
        val id: String,
        val name: String,
        val pet: String,
    )

    var fossils: List<Fossil> = listOf()
        private set

    override suspend fun load() {
        Utils.loadFromRepo<JsonObject>("fossils")?.asMap { id, data ->
            val obj = data.asJsonObject
            id to Fossil(id, obj["name"].asString(""), obj["pet"].asString(""))
        }?.map { it.value }?.let { fossils = it }
    }
}
