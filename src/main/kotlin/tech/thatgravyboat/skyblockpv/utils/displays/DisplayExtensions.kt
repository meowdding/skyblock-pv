package tech.thatgravyboat.skyblockpv.utils.displays

import eu.pb4.placeholders.api.ParserContext
import eu.pb4.placeholders.api.parsers.TagParser
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockpv.mixin.I18nAccessor

fun List<Any>.toColumn(spacing: Int = 0, alignment: Alignment = Alignment.START): Display {
    return Displays.column(
        *this.map {
            when (it) {
                is String -> Displays.text(it)
                is Component -> Displays.text(it)
                is Display -> it
                else -> throw IllegalArgumentException("Unsupported type: ${it::class.simpleName}")
            }
        }.toTypedArray(),
        spacing = spacing,
        alignment = alignment,
    )
}

fun List<Any>.toRow(spacing: Int = 0, alignment: Alignment = Alignment.START): Display {
    return Displays.row(
        *this.map {
            when (it) {
                is String -> Displays.text(it)
                is Component -> Displays.text(it)
                is Display -> it
                else -> throw IllegalArgumentException("Unsupported type: ${it::class.simpleName}")
            }
        }.toTypedArray(),
        spacing = spacing,
        alignment = alignment,
    )
}

fun List<Any>.asLayer(): Display {
    return Displays.layered(
        *this.map {
            when (it) {
                is String -> Displays.text(it)
                is Component -> Displays.text(it)
                is Display -> it
                else -> throw IllegalArgumentException("Unsupported type: ${it::class.simpleName}")
            }
        }.toTypedArray(),
    )
}

fun List<List<Any>>.asTable(spacing: Int = 0): Display =
    Displays.table(
        this.map {
            it.map {
                when (it) {
                    is Display -> it
                    is ResourceLocation -> Displays.sprite(it, 12, 12)
                    else -> Displays.text(it.toString(), color = { 0x555555u }, shadow = false)
                }
            }
        },
        spacing,
    )

fun Display.centerIn(width: Int, height: Int): Display = Displays.center(width, height, this)

fun Display.withBackground(color: UInt): Display = Displays.background(color, this)

fun Display.asWidget(): DisplayWidget = DisplayWidget(this)

fun Display.withTooltip(vararg tooltip: Any?): Display = Displays.tooltip(this, Text.multiline(*tooltip))
fun Display.withTranslatedTooltip(key: String, vararg args: Any?): Display {
    var raw = I18nAccessor.getLanguage().getOrDefault(key)
    args.forEachIndexed { index, any ->
        raw = raw.replace("<$index>", any.toString())
    }

    val text = TagParser.QUICK_TEXT_SAFE.parseText(raw, ParserContext.of())
    return Displays.tooltip(this, text)
}
