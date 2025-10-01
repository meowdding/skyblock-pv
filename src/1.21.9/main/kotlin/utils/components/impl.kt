package me.owdding.skyblockpv.utils.components

import net.minecraft.client.gui.GuiGraphics

actual fun GuiGraphics.renderCarouselOverlay(
    renderer: GuiGraphics.() -> Unit,
) {
    renderer.invoke(this)
}
