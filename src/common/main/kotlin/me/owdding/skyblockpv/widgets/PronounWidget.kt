package me.owdding.skyblockpv.widgets

import me.owdding.lib.displays.Display
import me.owdding.lib.displays.Displays
import me.owdding.lib.displays.centerIn
import me.owdding.skyblockpv.api.pronouns.PronounDbDecorations
import me.owdding.skyblockpv.api.pronouns.PronounsDbAPI
import me.owdding.skyblockpv.utils.Utils.asTranslated
import me.owdding.skyblockpv.utils.Utils.withTextShader
import me.owdding.skyblockpv.utils.displays.ExtraDisplays
import me.owdding.skyblockpv.utils.theme.PvColors
import tech.thatgravyboat.skyblockapi.helpers.McFont
import java.util.*

object PronounWidget {
    fun getPronounDisplay(uuid: UUID, width: Int): Display {
        return ExtraDisplays.completableDisplay(
            PronounsDbAPI.getPronounsAsync(uuid),
            { (decoration, pronouns) ->
                val display = when (pronouns.size) {
                    0 -> return@completableDisplay Displays.empty()
                    1 -> "${pronouns.first().first}/${pronouns.first().second}"
                    else -> pronouns.fold("") { acc, set -> if (acc.isEmpty()) set.first else "$acc/${set.first}" }
                }
                val shader = PronounDbDecorations.getShader(decoration ?: "")
                ExtraDisplays.component(
                    component = "widgets.pronouns".asTranslated(display),
                    color = { PvColors.DARK_GRAY.toUInt() or 0xFF000000u },
                    shadow = shader != null,
                ).centerIn(width, McFont.height).withTextShader(shader)
            },
            { ExtraDisplays.text("Error", color = { PvColors.RED.toUInt() }).centerIn(width, McFont.height) },
            { ExtraDisplays.text("Loading").centerIn(width, McFont.height) },
        )
    }
}
