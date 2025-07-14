package me.owdding.skyblockpv.utils.render.inventory

import earth.terrarium.olympus.client.utils.Orientation
import net.minecraft.client.gui.GuiGraphics
import net.msrandom.stub.Stub

@Stub
expect object InventoryTextureRender {

    fun drawInventory(
        graphics: GuiGraphics,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        size: Int,
        orientation: Orientation,
        color: Int,
    )

    fun drawInventory(
        graphics: GuiGraphics,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        columns: Int,
        rows: Int,
        color: Int,
    )

}
