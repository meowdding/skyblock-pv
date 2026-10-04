package me.owdding.skyblockpv.data.api

import com.google.gson.JsonObject
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.data.repo.EmblemsCodecs
import me.owdding.skyblockpv.utils.ChatUtils.sendWithPrefix
import me.owdding.skyblockpv.utils.ParseHelper
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.extentions.toTitleCase
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextColor

data class SkyBlockXpData(override val json: JsonObject) : ParseHelper {
    val emblem: EmblemsCodecs.Emblem? by string("selected_symbol").map { emblem ->
        EmblemsCodecs.getEmblem(emblem) ?: run {
            ifUnknownEmblem(emblem)
            if (emblem.isNotBlank()) {
                EmblemsCodecs.Emblem(emblem, "UNKNOWN", emblem.toTitleCase(), Text.of("??", TextColor.RED))
            } else null
        }
    }
    val experience by int()

    // Only used for confirming/finding unknown emblems
    val unlockedEmblems by stringList("emblem_unlocks").map { emblems ->
        emblems.forEach { emblem ->
            if (!EmblemsCodecs.isEmblemKnown(emblem)) {
                ifUnknownEmblem(emblem)
            }
        }
    }

    val getLevel get() = experience / 100
    val getProgress get() = experience % 100

    private fun ifUnknownEmblem(id: String) {
        if (id.isNotBlank()) {
            SkyBlockPv.warn("Unknown Emblem Id $id")
            SkyBlockPv.ifDevMode { McClient.runNextTick { Text.of("Unknown Emblem Id: $id").sendWithPrefix() } }
        }
    }
}
