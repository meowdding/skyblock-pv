package tech.thatgravyboat.skyblockpv.utils.displays

import net.minecraft.network.chat.Component


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
        }.toTypedArray()
    )
}

fun List<List<Display>>.asTable(spacing: Int = 0): Display = Displays.table(this, spacing)

fun Display.centerIn(width: Int, height: Int): Display = Displays.center(width, height, this)

fun Display.asWidget(): DisplayWidget {
    return DisplayWidget(this)
}
