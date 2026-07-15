package me.owdding.skyblockpv.data.api.skills

import com.google.gson.JsonObject
import me.owdding.lib.repo.TreeNode
import me.owdding.lib.repo.TreeRepoData
import me.owdding.skyblockpv.data.repo.SkullTextures
import me.owdding.skyblockpv.screens.windowed.tabs.base.CoreNodeItems
import me.owdding.skyblockpv.screens.windowed.tabs.base.SkillTreeItems
import me.owdding.skyblockpv.utils.ParseHelper
import me.owdding.skyblockpv.utils.json.getAs
import tech.thatgravyboat.skyblockapi.utils.extentions.asBoolean
import tech.thatgravyboat.skyblockapi.utils.extentions.asInt
import tech.thatgravyboat.skyblockapi.utils.extentions.asString

class SkillTree(override val json: JsonObject, id: String, skillType: String, treeType: String, val skillTreeType: SkillTreeType) : ParseHelper {

    constructor(json: JsonObject, id: String, treeType: SkillTreeType) : this(json, id, treeType.skillType, treeType.treeType, treeType)

    val nodes: Map<String, Int> by map("nodes.$skillType$id") { id, amount -> id to amount.asInt(0) }.map { it.filterKeys { !it.startsWith("toggle_") } }
    val disabled: List<String> by lazy {
        json.getAs<JsonObject>("nodes.$skillType")?.entrySet()?.asSequence()
            ?.filter { it.key.startsWith("toggle") }
            ?.map { it.key.removePrefix("toggle_") to it.value.asBoolean(true) }
            ?.filterNot { it.second }
            ?.map { it.first }
            ?.toList() ?: emptyList()
    }
    val selectedAbility: String? by parse("selected_ability.$skillType$id") { it.asString() }
    val tokensSpent: Int by int("selected_ability.$treeType$id")
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

enum class SkillTreeType(val skillType: String, val treeType: String, val coreNode: String, val skillTreeItems: SkillTreeItems, val nodes: () -> List<TreeNode>, val skullTextures: SkullTextures) {
    MINING("mining", "mountain", "core_of_the_mountain", SkillTreeItems.MINING, TreeRepoData::hotm, SkullTextures.HOTM),
    FORAGING("foraging", "forest", "center_of_the_forest", SkillTreeItems.FORAGING, TreeRepoData::hotf, SkullTextures.HOTF),
    ;
}

data class SkillTrees(override val json: JsonObject) : ParseHelper {
    val mining: SkillTree by parse("skill_tree") { SkillTree(it?.asJsonObject ?: JsonObject(), "", SkillTreeType.MINING) }
    val mining2: SkillTree by parse("skill_tree") { SkillTree(it?.asJsonObject ?: JsonObject(), "_2", SkillTreeType.MINING) }
    val mining3: SkillTree by parse("skill_tree") { SkillTree(it?.asJsonObject ?: JsonObject(), "_3", SkillTreeType.MINING) }
    val mining4: SkillTree by parse("skill_tree") { SkillTree(it?.asJsonObject ?: JsonObject(), "_4", SkillTreeType.MINING) }
    val mining5: SkillTree by parse("skill_tree") { SkillTree(it?.asJsonObject ?: JsonObject(), "_5", SkillTreeType.MINING) }
    val foraging: SkillTree by parse("skill_tree") { SkillTree(it?.asJsonObject ?: JsonObject(), "", SkillTreeType.FORAGING) }
    val foraging2: SkillTree by parse("skill_tree") { SkillTree(it?.asJsonObject ?: JsonObject(), "_2", SkillTreeType.FORAGING) }
    val foraging3: SkillTree by parse("skill_tree") { SkillTree(it?.asJsonObject ?: JsonObject(), "_3", SkillTreeType.FORAGING) }
    val foraging4: SkillTree by parse("skill_tree") { SkillTree(it?.asJsonObject ?: JsonObject(), "_4", SkillTreeType.FORAGING) }
    val foraging5: SkillTree by parse("skill_tree") { SkillTree(it?.asJsonObject ?: JsonObject(), "_5", SkillTreeType.FORAGING) }

    fun select(treeType: SkillTreeType, index: Int) = when (treeType) {
        SkillTreeType.FORAGING if index == 5 -> foraging5
        SkillTreeType.FORAGING if index == 4 -> foraging4
        SkillTreeType.FORAGING if index == 3 -> foraging3
        SkillTreeType.FORAGING if index == 2 -> foraging2
        SkillTreeType.FORAGING -> foraging
        SkillTreeType.MINING if index == 5 -> mining5
        SkillTreeType.MINING if index == 4 -> mining4
        SkillTreeType.MINING if index == 3 -> mining3
        SkillTreeType.MINING if index == 2 -> mining2
        SkillTreeType.MINING -> mining
    }

    val selectedMiningTree by int("selected_skill_tree_slot.mining")
    val selectedForagingTree by int("selected_skill_tree_slot.foraging")
    val selectedMining: SkillTree get() = select(SkillTreeType.MINING, selectedMiningTree)
    val selectedForaging: SkillTree get() = select(SkillTreeType.FORAGING, selectedForagingTree)

    val refundAbilityFree: Boolean by boolean("skill_tree.refund_ability_free")
}
