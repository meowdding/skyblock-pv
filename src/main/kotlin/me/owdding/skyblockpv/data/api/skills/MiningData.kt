package me.owdding.skyblockpv.data.api.skills


import com.google.gson.JsonObject
import me.owdding.skyblockpv.utils.ParseHelper
import me.owdding.skyblockpv.utils.Utils
import me.owdding.skyblockpv.utils.codecs.DefaultedData
import me.owdding.skyblockpv.utils.codecs.LoadData
import net.minecraft.ChatFormatting
import tech.thatgravyboat.skyblockapi.api.data.SkyBlockRarity
import tech.thatgravyboat.skyblockapi.api.remote.PetQuery
import tech.thatgravyboat.skyblockapi.api.remote.RepoItemsAPI
import tech.thatgravyboat.skyblockapi.api.remote.RepoPetsAPI
import tech.thatgravyboat.skyblockapi.utils.extentions.asMap
import tech.thatgravyboat.skyblockapi.utils.extentions.asString

data class MiningCore(override val json: JsonObject) : ParseHelper {
    val crystals: Map<String, Crystal> by map("crystals") { id, data -> id to Crystal(data.asJsonObject) }
    val powderMithril: Int by int("powder_mithril")
    val powderSpentMithril: Int by int("powder_spent_mithril")
    val powderGemstone: Int by int("powder_gemstone")
    val powderSpentGemstone: Int by int("powder_spent_gemstone")
    val powderGlacite: Int by int("powder_glacite")
    val powderSpentGlacite: Int by int("powder_spent_glacite")
}

data class Crystal(override val json: JsonObject) : ParseHelper {
    val state: String by string(default = "NOT_FOUND")
    val totalPlaced: Int by int("total_placed")
    val totalFound: Int by int("total_found")

    companion object {
        val EMPTY = Crystal(JsonObject())
    }
}

data class Forge(override val json: JsonObject) : ParseHelper {
    val slots: Map<Int, ForgeSlot> by map("forge_processes.forge_1") { key, value -> key.toInt() to ForgeSlot(value.asJsonObject) }
}

data class ForgeSlot(override val json: JsonObject) : ParseHelper {
    val type: String by string()
    val id: String by string()
    val startTime: Long by long()
    val notified: Boolean by boolean()

    val itemStack by lazy {
        if (type == "PETS") {
            RepoPetsAPI.getPetAsItem(PetQuery(id, SkyBlockRarity.LEGENDARY, 100))
        } else {
            RepoItemsAPI.getItem(id)
        }
    }
}

data class GlaciteData(override val json: JsonObject) : ParseHelper {
    val fossilsDonated: List<String> by stringList("fossils_donated")
    val fossilDust: Int by int("fossil_dust")
    val corpsesLooted: Map<String, Int> by stringIntMap("corpses_looted")
    val mineshaftsEntered: Int by int("mineshafts_entered")
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
            Utils.loadFromRemoteRepo<Map<String, List<String>>>("pv/gear/mining")?.forEach { (key, value) ->
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
object FossilTypes : DefaultedData {

    data class Fossil(
        val id: String,
        val name: String,
        val pet: String,
    )

    var fossils: List<Fossil> = listOf()
        private set

    override suspend fun load() {
        Utils.loadFromRemoteRepo<JsonObject>("pv/fossils")?.asMap { id, data ->
            val obj = data.asJsonObject
            id to Fossil(id, obj["name"].asString(""), obj["pet"].asString(""))
        }?.map { it.value }?.let { fossils = it }
    }
}
