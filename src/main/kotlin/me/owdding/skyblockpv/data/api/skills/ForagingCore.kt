package me.owdding.skyblockpv.data.api.skills

import com.google.gson.JsonObject
import me.owdding.skyblockpv.utils.ParseHelper

data class ForagingCore(override val json: JsonObject) : ParseHelper {
    val dailyTreesCut by int("daily_trees_cut")
    val dailyTreesCutDay by int("daily_trees_cut_day")

    val dailyLogCutDay by int("daily_log_cut_day")
    val dailyLogCut by stringList("daily_log_cut").map { it.toSet() }

    val dailyGifts by int("daily_gifts")

    val forestsWhispers: SkillTreeCurrency by obj("whispers.forest", ::SkillTreeCurrency)
    val desertWhispers: SkillTreeCurrency by obj("whispers.desert", ::SkillTreeCurrency)
}

data class SkillTreeCurrency(
    override val json: JsonObject
):  ParseHelper {
    val first: Int by int("1.spent")
    val second: Int by int("2.spent")
    val third: Int by int("3.spent")
    val fourth: Int by int("4.spent")
    val fifth: Int by int("5.spent")
    val total: Long by long("total")

    fun spent(slot: Int?): Int = when (slot) {
        2 -> second
        3 -> third
        4 -> fourth
        5 -> fifth
        else -> first
    }
}

data class PersonalBests(override val json: JsonObject) : ParseHelper {
    val agatha by int()
    val miria by int()
    val fig by int("FIG_LOG")
    val mangrove by int("MANGROVE_LOG")
    val helix by int("HELIX_LOG")
}

data class TreeGifts(override val json: JsonObject) : ParseHelper {
    val fig by int("FIG")
    val mangrove by int("MANGROVE")
    val helix by int("HELIX")
    val figTierClaimed by int("milestone_tier_claimed.FIG")
    val mangroveTierClaimed by int("milestone_tier_claimed.MANGROVE")
    val helixTierClaimed by int("milestone_tier_claimed.HELIX")
}

data class ForagingData(override val json: JsonObject) : ParseHelper {
    val personalBests by obj("starlyn.personal_bests", ::PersonalBests)
    val fishFamily: Set<String> by stringSet("fish_family")
    val treeGifts by obj("tree_gifts", transform = ::TreeGifts)
}
