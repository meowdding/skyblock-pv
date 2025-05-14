package me.owdding.skyblockpv.screens.tabs.general

import me.owdding.lib.displays.Display
import me.owdding.lib.displays.Displays
import me.owdding.lib.displays.centerIn
import me.owdding.skyblockpv.api.pronouns.PronounsDbAPI
import me.owdding.skyblockpv.utils.displays.ExtraDisplays
import tech.thatgravyboat.skyblockapi.helpers.McFont
import java.util.*

object PronounDisplay {
    fun getPronounDisplay(uuid: UUID, width: Int): Display {
        return ExtraDisplays.completableDisplay(
            PronounsDbAPI.getPronounsAsync(uuid),
            { (_, pronouns) ->
                val pronouns = pronouns.firstOrNull() ?: return@completableDisplay Displays.empty()
                Displays.text("Pronouns: ${pronouns.toDisplay()}", color = { 0x555555u }, shadow = false).centerIn(width, McFont.height)
            },
            { Displays.empty(height = McFont.height, width = width) },
            { Displays.empty(height = McFont.height, width = width) },
        )
    }
}
