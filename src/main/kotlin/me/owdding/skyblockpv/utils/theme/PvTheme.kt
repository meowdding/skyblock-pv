package me.owdding.skyblockpv.utils.theme

import me.owdding.ktcodecs.FieldName
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktcodecs.NamedCodec
import net.minecraft.resources.ResourceLocation

object ThemeHelper {
    val ids get() = ThemeLoader.themes.keys
    val defaultColors: PvThemeColors = PvThemeColors()
    val fallbackTheme: PvTheme = PvTheme(name = "skyblockpv.themes.fallback")
}

@GenerateCodec
data class PvTheme(
    val colors: PvThemeColors = ThemeHelper.defaultColors,
    @NamedCodec("resource_map") val textures: Map<ResourceLocation, ResourceLocation> = mapOf(),
    val name: String,
)

@GenerateCodec
data class PvThemeColors(
    val black: Int = 0x000000,
    @FieldName("dark_blue") val darkBlue: Int = 0x0000AA,
    @FieldName("dark_green") val darkGreen: Int = 0x00AA00,
    @FieldName("dark_aqua") val darkAqua: Int = 0x00AAAA,
    @FieldName("dark_red") val darkRed: Int = 0xAA0000,
    @FieldName("dark_purple") val darkPurple: Int = 0xAA00AA,
    val gold: Int = 0xFFAA00,
    val gray: Int = 0xAAAAAA,
    @FieldName("dark_gray") val darkGray: Int = 0x555555,
    val blue: Int = 0x5555FF,
    val green: Int = 0x55FF55,
    val aqua: Int = 0x55FFFF,
    val red: Int = 0xFF5555,
    @FieldName("light_purple") val lightPurple: Int = 0xFF55FF,
    val yellow: Int = 0xFFFF55,
    val white: Int = 0xFFFFFF,
)
