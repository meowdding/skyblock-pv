package me.owdding.skyblockpv.utils.theme

import me.owdding.skyblockpv.SkyBlockPv.id
import me.owdding.skyblockpv.config.Config
import net.minecraft.resources.ResourceLocation

object ThemeSupport {

    val currentTheme: PvTheme get() = ThemeLoader.themes[Config.theme] ?: ThemeHelper.fallbackTheme

    val pvColors get() = currentTheme.colors
    val pvTextures get() = currentTheme.textures

    fun texture(path: String) = texture(id(path))
    fun texture(path: ResourceLocation) = pvTextures.getOrDefault(path, path)

}
