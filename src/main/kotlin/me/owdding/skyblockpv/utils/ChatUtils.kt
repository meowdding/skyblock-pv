package me.owdding.skyblockpv.utils

import net.minecraft.network.chat.Component
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

    fun Component.sendWithPrefix() {
        Text.join(prefix, this).send()
    }

    fun <T> Collection<T>.joinToComponent(separator: String, transform: (T) -> Component) = joinToComponent(Text.of(separator), transform)
    fun <T> Collection<T>.joinToComponent(separator: Component, transform: (T) -> Component) = Text.join(this.map(transform), separator = separator.copy())
    fun Collection<Component>.join(separator: String, transform: (Component) -> Component = { it }) = join(Text.of(separator), transform)
    fun Collection<Component>.join(separator: Component, transform: (Component) -> Component = { it }) =
        Text.join(this.map(transform), separator = separator.copy())

}
