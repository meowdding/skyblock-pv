package me.owdding.skyblockpv.utils.theme

import com.mojang.serialization.Codec
import com.teamresourceful.resourcefullib.common.color.Color
import me.owdding.ktcodecs.FieldName
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktcodecs.IncludedCodec
import me.owdding.ktcodecs.NamedCodec
import me.owdding.skyblockpv.generated.SkyBlockPVCodecs
import net.minecraft.network.chat.Component
import net.minecraft.resources.FileToIdConverter
import net.minecraft.resources.Identifier

object ThemeHelper {

    val location: FileToIdConverter = FileToIdConverter.json("themes")
    val themes: MutableMap<Identifier, PvTheme> = mutableMapOf()
    val ids get() = themes.keys
    val defaultColors: PvThemeColors = PvThemeColors()
    val fallbackTheme: PvTheme = PvTheme(name = "skyblockpv.themes.fallback")

    @IncludedCodec(named = "theme§color")
    val COLOR_CODEC: Codec<Int> = Color.CODEC.xmap(
        { color -> color.value },
        { intColor -> Color(intColor) },
    )

}

@GenerateCodec
data class PvTheme(
    val colors: PvThemeColors = ThemeHelper.defaultColors,
    @NamedCodec("identifier_map") val textures: Map<Identifier, Identifier> = mapOf(),
    val name: String,
    @FieldName("background_blur") val backgroundBlur: Boolean = true,
) {
    val translation: Component = Component.translatable(name)

    companion object {
        val CODEC = SkyBlockPVCodecs.PvThemeCodec
    }
}

@GenerateCodec
data class PvThemeColors(
    @NamedCodec("theme§color") val black: Int = 0x000000,
    @NamedCodec("theme§color") @FieldName("dark_blue") val darkBlue: Int = 0x0000AA,
    @NamedCodec("theme§color") @FieldName("dark_green") val darkGreen: Int = 0x00AA00,
    @NamedCodec("theme§color") @FieldName("dark_aqua") val darkAqua: Int = 0x00AAAA,
    @NamedCodec("theme§color") @FieldName("dark_red") val darkRed: Int = 0xAA0000,
    @NamedCodec("theme§color") @FieldName("dark_purple") val darkPurple: Int = 0xAA00AA,
    @NamedCodec("theme§color") val gold: Int = 0xFFAA00,
    @NamedCodec("theme§color") val gray: Int = 0xAAAAAA,
    @NamedCodec("theme§color") @FieldName("dark_gray") val darkGray: Int = 0x555555,
    @NamedCodec("theme§color") val blue: Int = 0x5555FF,
    @NamedCodec("theme§color") val green: Int = 0x55FF55,
    @NamedCodec("theme§color") val aqua: Int = 0x55FFFF,
    @NamedCodec("theme§color") val red: Int = 0xFF5555,
    @NamedCodec("theme§color") @FieldName("light_purple") val lightPurple: Int = 0xFF55FF,
    @NamedCodec("theme§color") val yellow: Int = 0xFFFF55,
    @NamedCodec("theme§color") val white: Int = 0xFFFFFF,
)
