package me.owdding.skyblockpv.utils.theme

import me.owdding.skyblockpv.SkyBlockPv.id
import me.owdding.skyblockpv.accessor.WidgetSpritesAccessor
import me.owdding.skyblockpv.config.Config
import net.minecraft.client.gui.components.WidgetSprites
import net.minecraft.resources.ResourceLocation

object ThemeSupport {

    val currentTheme: PvTheme get() = ThemeLoader.themes[Config.theme] ?: ThemeHelper.fallbackTheme

    val pvColors get() = currentTheme.colors
    val pvTextures get() = currentTheme.textures

    fun nextTheme() {
        val themes = ThemeLoader.themes.keys.toList()
        Config.theme = themes[(themes.indexOf(Config.theme) + 1) % themes.size]
    }

    fun texture(path: String) = texture(id(path))
    fun texture(path: ResourceLocation) = pvTextures.getOrDefault(path, path)

    fun WidgetSprites.withThemeSupport(): WidgetSprites = also { WidgetSpritesAccessor.withThemeSupport(it) }

    fun ThemedWidgetSprites(
        enabled: ResourceLocation,
        disabled: ResourceLocation,
        enabledFocused: ResourceLocation,
        disabledFocused: ResourceLocation = disabled,
    ) = WidgetSprites(enabled, disabled, enabledFocused, disabledFocused).withThemeSupport()

}
