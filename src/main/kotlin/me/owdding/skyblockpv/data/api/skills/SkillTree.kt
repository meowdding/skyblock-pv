package me.owdding.skyblockpv.data.api.skills

import com.google.gson.JsonObject
import me.owdding.skyblockpv.utils.ParseHelper
import me.owdding.skyblockpv.utils.json.getAs
import tech.thatgravyboat.skyblockapi.utils.extentions.asBoolean
import tech.thatgravyboat.skyblockapi.utils.extentions.asInt
import tech.thatgravyboat.skyblockapi.utils.extentions.asString

class SkillTree(override val json: JsonObject, skillType: String, treeType: String) : ParseHelper {

    constructor(json: JsonObject, treeType: SkillTreeType) : this(json, treeType.skillType, treeType.treeType)

    val nodes: Map<String, Int> by map("nodes.$skillType") { id, amount -> id to amount.asInt(0) }.map { it.filterKeys { !it.startsWith("toggle_") } }
    val disabled: List<String> by lazy {
        json.getAs<JsonObject>("nodes.$skillType")?.entrySet()?.asSequence()
            ?.filter { it.key.startsWith("toggle") }
            ?.map { it.key.removePrefix("toggle_") to it.value.asBoolean(true) }
            ?.filterNot { it.second }
            ?.map { it.first }
            ?.toList() ?: emptyList()
    }
    val selectedAbility: String? by parse("selected_ability.$skillType") { it.asString() }
    val tokensSpent: Int by int("selected_ability.$treeType")
    val experience: Long by long("experience.$skillType")
    val lastReset: Long by long("last_reset.$skillType")

    // todo repo, low prio
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

    fun getTreeLevel(): Int = levelToExp.entries.findLast { it.value <= experience }?.key ?: 0
    fun getXpToNextLevel() = experience - (levelToExp[getTreeLevel()] ?: 0)
    fun getXpRequiredForNextLevel(): Int {
        val level = (getTreeLevel() + 1).coerceAtMost(10)
        return (levelToExp[level] ?: 0) - (levelToExp[level - 1] ?: 0)
    }

    fun getAbilityLevel(coreNode: String) = 1.takeIf { (nodes[coreNode] ?: 0) < 1 } ?: 2

}

enum class SkillTreeType(val skillType: String, val treeType: String) {
    MINING("mining", "mountain"),
    FORAGING("foraging", "forest"),
    ;
}

data class SkillTrees(override val json: JsonObject) : ParseHelper {
    val mining: SkillTree by parse("skill_tree") { SkillTree(it?.asJsonObject ?: JsonObject(), SkillTreeType.MINING) }
    val foraging: SkillTree by parse("skill_tree") { SkillTree(it?.asJsonObject ?: JsonObject(), SkillTreeType.FORAGING) }
    val refundAbilityFree: Boolean by boolean("skill_tree.refund_ability_free")
}
