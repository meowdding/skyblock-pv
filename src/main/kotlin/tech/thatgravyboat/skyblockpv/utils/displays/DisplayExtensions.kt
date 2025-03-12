package tech.thatgravyboat.skyblockpv.utils.displays

import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import tech.thatgravyboat.skyblockapi.utils.text.Text


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

fun <T> List<List<T>>.swapAxis(): List<List<T>> {
    val list = mutableListOf<MutableList<T>>()
    for (x in indices) {
        for (y in this[x].indices) {
            if (x == 0) {
                list.add(mutableListOf())
            }
            list[y].add(this[x][y])
        }
    }
    return list
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
