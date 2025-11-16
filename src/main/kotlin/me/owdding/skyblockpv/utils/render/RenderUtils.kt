package me.owdding.skyblockpv.utils.render

import earth.terrarium.olympus.client.utils.Orientation
import me.owdding.lib.rendering.text.TextShader
import me.owdding.lib.rendering.text.TextShaders
import me.owdding.skyblockpv.utils.render.inventory.InventoryTextureRender
import net.minecraft.client.gui.GuiGraphics

object RenderUtils {

    var TEXT_SHADER: TextShader?
        get() = TextShaders.activeShader
        set(value) {
            TextShaders.activeShader = value
        }

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
    fun GuiGraphics.withTextShader(shader: TextShader?, action: () -> Unit) = pushPopTextShader(shader) {
        action()
    }

    fun pushPopTextShader(shader: TextShader?, action: () -> Unit) {
        val previous = TEXT_SHADER
        TEXT_SHADER = shader
        action()
        TEXT_SHADER = previous
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
