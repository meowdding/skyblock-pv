package tech.thatgravyboat.skyblockpv.utils

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.layouts.FrameLayout
import net.minecraft.client.gui.layouts.LayoutElement
import java.text.DecimalFormat

object Utils {

    fun PoseStack.translate(x: Int, y: Int, z: Int) {
        this.translate(x.toFloat(), y.toFloat(), z.toFloat())
    }

    inline fun GuiGraphics.scissor(x: Int, y: Int, width: Int, height: Int, action: () -> Unit) {
        this.enableScissor(x, y, x + width, y + height)
        action()
        this.disableScissor()
    }

    inline fun GuiGraphics.pushPop(action: PoseStack.() -> Unit) {
        this.pose().pushPop(action)
    }

    inline fun PoseStack.pushPop(action: PoseStack.() -> Unit) {
        this.pushPose()
        this.action()
        this.popPose()
    }

    fun Number.round(): String = DecimalFormat("#.##").format(this)

    fun LayoutElement.centerVertically(height: Int) : LayoutElement {
        return FrameLayout(0, height).also { it.addChild(this) }
    }

    fun LayoutElement.centerHorizontally(width: Int) : LayoutElement {
        return FrameLayout(width, 0).also { it.addChild(this) }
    }
}
