package me.owdding.skyblockpv.utils.theme

import earth.terrarium.olympus.client.constants.MinecraftColors
import kotlin.reflect.KProperty
import com.teamresourceful.resourcefullib.common.color.Color as RColor

object PvColors {

    var DARK_GRAY_COLOR: RColor = MinecraftColors.DARK_GRAY
        get() {
            if (field.value != DARK_GRAY) {
                field = RColor(DARK_GRAY)
            }

            return field
        }
        private set

    val BLACK: Int by Color(PvThemeColors::black)
    val DARK_BLUE: Int by Color(PvThemeColors::darkBlue)
    val DARK_GREEN: Int by Color(PvThemeColors::darkGreen)
    val DARK_AQUA: Int by Color(PvThemeColors::darkAqua)
    val DARK_RED: Int by Color(PvThemeColors::darkRed)
    val DARK_PURPLE: Int by Color(PvThemeColors::darkPurple)
    val MAGENTA: Int get() = DARK_PURPLE
    val GOLD: Int by Color(PvThemeColors::gold)
    val ORANGE: Int get() = GOLD
    val GRAY: Int by Color(PvThemeColors::gray)
    val DARK_GRAY: Int by Color(PvThemeColors::darkGray)
    val BLUE: Int by Color(PvThemeColors::blue)
    val GREEN: Int by Color(PvThemeColors::green)
    val AQUA: Int by Color(PvThemeColors::aqua)
    val RED: Int by Color(PvThemeColors::red)
    val LIGHT_PURPLE: Int by Color(PvThemeColors::lightPurple)
    val PINK: Int get() = LIGHT_PURPLE
    val YELLOW: Int by Color(PvThemeColors::yellow)
    val WHITE: Int by Color(PvThemeColors::white)

    data class Color(
        val supplier: PvThemeColors.() -> Int,
    ) {
        operator fun getValue(ref: PvColors, property: KProperty<*>) = ThemeSupport.pvColors.supplier()
    }

}
