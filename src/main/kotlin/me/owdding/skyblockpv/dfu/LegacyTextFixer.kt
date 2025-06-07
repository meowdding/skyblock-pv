package me.owdding.skyblockpv.dfu

import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.utils.StringReader
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append

object LegacyTextFixer {

    private const val CONTROL_CHAR = '§'
    val codeMap = buildMap {
        fun put(formatting: ChatFormatting, init: Style.() -> Style) {
            put(formatting.char.lowercaseChar(), init)
        }

        ChatFormatting.entries.filter { it.isColor }.forEach { formatting -> put(formatting) { this.withColor(formatting) } }

        put(ChatFormatting.BOLD) { this.withBold(true) }
        put(ChatFormatting.ITALIC) { this.withItalic(true) }
        put(ChatFormatting.STRIKETHROUGH) { this.withStrikethrough(true) }
        put(ChatFormatting.UNDERLINE) { this.withUnderlined(true) }
        put(ChatFormatting.OBFUSCATED) { this.withObfuscated(true) }

        put(ChatFormatting.RESET) { Style.EMPTY }
    }

    fun parse(text: String): Component = Text.of {
        if (!text.contains(CONTROL_CHAR)) {
            append(text)
            return@of
        }

        var last: Style = Style.EMPTY
        with(StringReader(text)) {
            append(this.readUntil(CONTROL_CHAR))

            while (this.canRead()) {
                codeMap[this.read().lowercaseChar()]?.let { last = last.it() } ?: SkyBlockPv.warn("Unknown control character ${this.peek()} in text $text")

                if (peek() == CONTROL_CHAR) {
                    skip()
                    continue
                }

                val readStringUntil = readUntil(CONTROL_CHAR)
                append(readStringUntil) {
                    withStyle(last)
                }

                last = Style.EMPTY
            }
        }
    }

}
