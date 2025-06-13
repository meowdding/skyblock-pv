package me.owdding.skyblockpv.widgets

import me.owdding.lib.displays.Display
import me.owdding.lib.displays.Displays
import me.owdding.lib.displays.centerIn
import me.owdding.skyblockpv.api.pronouns.PronounDbDecorations
import me.owdding.skyblockpv.api.pronouns.PronounsDbAPI
import me.owdding.skyblockpv.utils.Utils.asTranslated
import me.owdding.skyblockpv.utils.Utils.withTextShader
import me.owdding.skyblockpv.utils.displays.ExtraDisplays
import tech.thatgravyboat.skyblockapi.helpers.McFont
import java.util.*

object PronounWidget {
    fun getPronounDisplay(uuid: UUID, width: Int): Display {
        return ExtraDisplays.completableDisplay(
            PronounsDbAPI.getPronounsAsync(uuid),
            { (decoration, pronouns) ->
                val pronouns = pronouns.firstOrNull() ?: return@completableDisplay Displays.empty()
                val shader = PronounDbDecorations.getShader(decoration ?: "")
                Displays.component(
                    component = "widgets.pronouns".asTranslated(pronouns.toDisplay()),
                    color = { 0xFF555555u.takeUnless { shader != null } ?: 0xFFFFFFFFu },
                    shadow = shader != null,
                ).centerIn(width, McFont.height).withTextShader(shader)
            },
            { Displays.text("Error") },
            { Displays.text("Loading") },
        )
    }
}
