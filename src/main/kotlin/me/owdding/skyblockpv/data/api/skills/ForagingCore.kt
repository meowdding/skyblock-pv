package me.owdding.skyblockpv.data.api.skills

import com.google.gson.JsonObject
import me.owdding.skyblockpv.utils.ParseHelper

data class ForagingCore(override val json: JsonObject) : ParseHelper {
    val dailyTreesCut by int("daily_trees_cut")
    val dailyTreesCutDay by int("daily_trees_cut_day")

    val dailyLogCutDay by int("daily_log_cut_day")
    val dailyLogCut by stringList("daily_log_cut").map { it.toSet() }

    val dailyGifts by int("daily_gifts")

    val forestsWhispers: Int by int("forests_whispers")
    val forestsSpentWhispers: Int by int("forests_whispers_spent")
}

data class PersonalBests(override val json: JsonObject) : ParseHelper {
    val agatha by int()
    val fig by int("FIG_LOG")
    val mangrove by int("MANGROVE_LOG")
}

data class TreeGifts(override val json: JsonObject) : ParseHelper {
    val fig by int("FIG")
    val mangrove by int("MANGROVE")
    val figTierClaimed by int("milestone_tier_claimed.FIG")
    val mangroveTierClaimed by int("milestone_tier_claimed.MANGROVE")
}

data class ForagingData(override val json: JsonObject) : ParseHelper {
    val personalBests by obj("starlyn.personal_bests", ::PersonalBests)
    val fishFamily: Set<String> by stringSet()
    val treeGifts by obj(transform = ::TreeGifts)
}
