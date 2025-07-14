package me.owdding.skyblockpv.utils.render.inventory

import earth.terrarium.olympus.client.utils.Orientation
import net.minecraft.client.gui.GuiGraphics

actual object InventoryTextureRender {

    actual fun drawInventory(
        graphics: GuiGraphics,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        size: Int,
        orientation: Orientation,
        color: Int,
    ) {
        graphics.fill(x, y, width, height, color)
    }

    actual fun drawInventory(
        graphics: GuiGraphics,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        columns: Int,
        rows: Int,
        color: Int,
    ) {
        graphics.fill(x, y, width, height, color)
    }
}
