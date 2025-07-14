package me.owdding.skyblockpv.utils.render

import earth.terrarium.olympus.client.utils.Orientation
import me.owdding.skyblockpv.utils.render.inventory.InventoryTextureRender
import net.minecraft.client.gui.GuiGraphics

object RenderUtils {


    fun drawInventory(
        graphics: GuiGraphics,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        size: Int,
        orientation: Orientation,
        color: Int,
    ) = InventoryTextureRender.drawInventory(graphics, x, y, width, height, size, orientation, color)

    @Suppress("UnusedReceiverParameter")
    fun GuiGraphics.withTextShader(shader: TextShader?, action: () -> Unit) {

    }

    fun pushPopTextShader(shader: TextShader?, action: () -> Unit) {

    }

    fun drawInventory(
        graphics: GuiGraphics,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        columns: Int,
        rows: Int,
        color: Int,
    ) = InventoryTextureRender.drawInventory(graphics, x, y, width, height, columns, rows, color)

}
