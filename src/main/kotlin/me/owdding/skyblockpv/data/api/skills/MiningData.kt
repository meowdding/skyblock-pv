package me.owdding.skyblockpv.data.api.skills


import com.google.gson.JsonObject
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktmodules.Module
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.utils.ParseHelper
import net.minecraft.ChatFormatting
import tech.thatgravyboat.skyblockapi.api.data.SkyBlockRarity
import tech.thatgravyboat.skyblockapi.api.repo.apis.SkyBlockItemsRepo
import tech.thatgravyboat.skyblockapi.api.repo.apis.SkyBlockPetsRepo

data class MiningCore(override val json: JsonObject) : ParseHelper {
    val crystals: Map<String, Crystal> by map("crystals") { id, data -> id to Crystal(data.asJsonObject) }
    val powderMithril = SkillTreeCurrency(json, CurrencyType.MITHRIL)
    val powderGemstone = SkillTreeCurrency(json, CurrencyType.GEMSTONE)
    val powderGlacite = SkillTreeCurrency(json, CurrencyType.GLACITE)
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
            SkyBlockPetsRepo.getItemStackOrDefault {
                this.id = this@ForgeSlot.id
                this.rarity = SkyBlockRarity.LEGENDARY
                this.level = 100
            }
        } else {
            SkyBlockItemsRepo.getItemStackOrDefault(id)
        }
    }
}

data class GlaciteData(override val json: JsonObject) : ParseHelper {
    val fossilsDonated: List<String> by stringList("fossils_donated")
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

    val list: List<String>
        get() = data[name.lowercase()].orEmpty()

    companion object {
        private val data: Map<String, List<String>> by SkyBlockPv.repoDelegate("pv/gear/mining", emptyMap())

        val cloaks get() = CLOAKS.list
        val gloves get() = GLOVES.list
        val necklaces get() = NECKLACES.list
        val belts get() = BELTS.list
        val equipment get() = listOf(cloaks, gloves, necklaces, belts).flatten()
        val armor get() = ARMOR.list
        val pickaxes get() = PICKAXES.list
        val chisels get() = CHISELS.list
        val suspicious_scrap get() = SUSPICIOUS_SCRAP.list
    }
}

enum class RockBracket(val oresRequired: Int, val rarity: SkyBlockRarity) {
    COMMON(2500, SkyBlockRarity.COMMON),
    UNCOMMON(7500, SkyBlockRarity.UNCOMMON),
    RARE(20000, SkyBlockRarity.RARE),
    EPIC(100000, SkyBlockRarity.EPIC),
    LEGENDARY(250000, SkyBlockRarity.LEGENDARY);

    companion object {
        fun getByOres(kills: Int): RockBracket? = entries.lastOrNull { it.oresRequired <= kills }
    }
}

enum class PowderType(val formatting: ChatFormatting) {
    MITHRIL(ChatFormatting.DARK_GREEN),
    GEMSTONE(ChatFormatting.LIGHT_PURPLE),
    GLACITE(ChatFormatting.AQUA);
}

@Module
object FossilTypes {

    @GenerateCodec
    data class Fossil(
        val name: String = "",
        val pet: String = "",
    )

    val fossils: Map<String, Fossil> by SkyBlockPv.repoDelegate("pv/fossils", emptyMap())
}
