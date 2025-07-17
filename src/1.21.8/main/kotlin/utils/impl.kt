package me.owdding.skyblockpv.utils

import earth.terrarium.olympus.client.pipelines.RoundedRectangle
import net.minecraft.client.gui.GuiGraphics
import tech.thatgravyboat.skyblockapi.platform.pushPop

actual fun GuiGraphics.drawRoundedRec(
    x: Int, y: Int, width: Int, height: Int,
    backgroundColor: Int, borderColor: Int,
    borderSize: Int, radius: Int,
) {
    pushPop {
        RoundedRectangle.draw(
            this@drawRoundedRec, x, y, width, height,
            backgroundColor, borderColor, width.coerceAtMost(height) * (radius / 100f), borderSize,
        )
    }
}
