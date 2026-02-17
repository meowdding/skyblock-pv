package me.owdding.skyblockpv.data.api.skills

import com.google.gson.JsonObject
import me.owdding.skyblockpv.api.data.profile.BackingSkyBlockProfile.Companion.future
import me.owdding.skyblockpv.data.api.skills.SkillTree.Companion.parse
import me.owdding.skyblockpv.utils.debugToggle
import me.owdding.skyblockpv.utils.json.getAs
import me.owdding.skyblockpv.utils.json.getPathAs
import tech.thatgravyboat.skyblockapi.utils.extentions.asBoolean
import tech.thatgravyboat.skyblockapi.utils.extentions.asInt
import tech.thatgravyboat.skyblockapi.utils.extentions.asMap
import java.util.concurrent.CompletableFuture

data class SkillTree(
    val nodes: Map<String, Int>,
    val disabled: Set<String>,
    val selectedAbility: String?,
    val tokensSpent: Int,
    val experience: Long,
    val lastReset: Long,
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

    fun getTreeLevel(): Int = levelToExp.entries.findLast { it.value <= experience }?.key ?: 0
    fun getXpToNextLevel() = experience - (levelToExp[getTreeLevel()] ?: 0)
    fun getXpRequiredForNextLevel(): Int {
        val level = (getTreeLevel() + 1).coerceAtMost(10)
        return (levelToExp[level] ?: 0) - (levelToExp[level - 1] ?: 0)
    }

    fun getAbilityLevel(coreNode: String) = 1.takeIf { (nodes[coreNode] ?: 0) < 1 } ?: 2


    companion object {
        fun JsonObject.parse(type: SkillTreeType) : SkillTree {
            val (skillType, treeType) = type

            val nodes = getPathAs<JsonObject>("nodes.$skillType").asMap { id, amount -> id to amount.asInt(0) }.filterKeys { !it.startsWith("toggle_") }
            val disabled = getPathAs<JsonObject>("nodes.$skillType")?.entrySet()
                ?.asSequence()
                ?.filter { it.key.startsWith("toggle") }
                ?.map { it.key.removePrefix("toggle_") to it.value.asBoolean(true) }
                ?.filterNot { it.second }
                ?.map { it.first }
                ?.toSet() ?: emptySet()

            return SkillTree(
                nodes,
                disabled,
                getPathAs<String>("selected_ability.$skillType"),
                getPathAs<Int>("selected_ability.$treeType", 0),
                getPathAs<Long>("experience.$skillType", 0),
                getPathAs<Long>("last_reset.$skillType", 0),
            )
        }
    }
}

enum class SkillTreeType(val skillType: String, val treeType: String) {
    MINING("mining", "mountain"),
    FORAGING("foraging", "forest"),
    ;

    operator fun component1() = skillType
    operator fun component2() = treeType
}

data class SkillTrees(
    val mining: SkillTree,
    val foraging: SkillTree,
    val refundAbilityFree: Boolean,
) {

    companion object {
        fun fromJson(member: JsonObject): CompletableFuture<SkillTrees?> = future {
            val skillTrees = member.getAs<JsonObject>("skill_tree") ?: return@future null

            SkillTrees(
                skillTrees.parse(SkillTreeType.MINING),
                skillTrees.parse(SkillTreeType.FORAGING),
                skillTrees.getAs<Boolean>("refund_ability_free", false)
            )
        }
    }

}
