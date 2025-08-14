package me.owdding.skyblockpv.utils.components

import net.minecraft.client.gui.GuiGraphics
import tech.thatgravyboat.skyblockapi.utils.extensions.translated

actual fun GuiGraphics.renderCarouselOverlay(
    renderer: GuiGraphics.() -> Unit,
) {
    val graphics = this
    this.translated(0f, 0f, 300f) {
        renderer.invoke(graphics)
    }
}
