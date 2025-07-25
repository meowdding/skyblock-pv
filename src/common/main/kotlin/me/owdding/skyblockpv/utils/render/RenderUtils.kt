package me.owdding.skyblockpv.utils.render

import earth.terrarium.olympus.client.utils.Orientation
import me.owdding.skyblockpv.utils.render.inventory.InventoryTextureRender
import net.minecraft.client.gui.GuiGraphics

object RenderUtils {

    var TEXT_SHADER: TextShader? = null
        private set

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
        TEXT_SHADER = shader
        action()
        TEXT_SHADER = null
    }

    fun pushPopTextShader(shader: TextShader?, action: () -> Unit) {
        TEXT_SHADER = shader
        action()
        TEXT_SHADER = null
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
