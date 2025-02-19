package tech.thatgravyboat.skyblockpv.utils.widgets

import com.mojang.blaze3d.systems.RenderSystem
import earth.terrarium.olympus.client.components.base.BaseWidget
import earth.terrarium.olympus.client.components.base.renderer.WidgetRenderer
import earth.terrarium.olympus.client.components.base.renderer.WidgetRendererContext
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.world.item.ItemStack

// todo dont use this i guess
class ItemWidget : BaseWidget() {
    private var renderer: WidgetRenderer<in ItemWidget?> = WidgetRenderer.empty<ItemWidget?>()
    private var itemStack: ItemStack? = null

    override fun renderWidget(graphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        this.isHovered = graphics.containsPointInScissor(mouseX, mouseY) && this.isMouseOver(mouseX.toDouble(), mouseY.toDouble())
        RenderSystem.enableBlend()
        RenderSystem.enableDepthTest()
        itemStack?.let { itemStack -> graphics.renderItem(itemStack, this.x, this.y) }

        this.renderer.render(graphics, WidgetRendererContext(this, mouseX, mouseY), partialTick)
    }

    fun withRenderer(renderer: WidgetRenderer<in ItemWidget?>): ItemWidget {
        this.renderer = renderer
        return this
    }

    fun withItemStack(item: ItemStack?): ItemWidget {
        this.itemStack = item
        return this
    }

    override fun withSize(width: Int, height: Int): ItemWidget = super.withSize(width, height) as ItemWidget

}
