package me.owdding.skyblockpv.utils

import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.Text.send
import tech.thatgravyboat.skyblockapi.utils.text.TextColor

object ChatUtils {

    private val prefix = Text.join(
        Text.of("[").withColor(TextColor.GRAY),
        Text.of("SbPv").withColor(TextColor.LIGHT_PURPLE),
        Text.of("] ").withColor(TextColor.GRAY),
    )

    fun chat(text: String) {
        chat(Text.of(text))
    }

    fun chat(text: Component) {
        Text.join(prefix, text).send()
    }

    fun MutableComponent.sendWithPrefix() {
        Text.join(prefix, this).send()
    }

}
