package me.owdding.skyblockpv.utils

import earth.terrarium.olympus.client.pipelines.RoundedRectangle
import net.minecraft.client.gui.GuiGraphics

actual fun GuiGraphics.drawRoundedRec(
    x: Int, y: Int, width: Int, height: Int,
    backgroundColor: Int, borderColor: Int,
    borderSize: Int, radius: Int,
) {
    this.flush()

        RoundedRectangle.drawRelative(
            this@drawRoundedRec, x, y, width, height,
            backgroundColor, borderColor, width.coerceAtMost(height) * (radius / 100f), borderSize,
        )
}
