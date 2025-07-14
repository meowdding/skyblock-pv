package me.owdding.skyblockpv.utils.accessors

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.navigation.ScreenRectangle
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

interface GuiGraphicsAccessor {

    fun `skyblockpv$popScissor`(): ScreenRectangle?
    fun `skyblockpv$pushScissor`(rectangle: ScreenRectangle?)

}

fun GuiGraphics.popExclusiveScissor(): ScreenRectangle? = (this as? GuiGraphicsAccessor)?.`skyblockpv$popScissor`()
fun GuiGraphics.pushExclusiveScissor(scissor: ScreenRectangle) = (this as? GuiGraphicsAccessor)?.`skyblockpv$pushScissor`(scissor)

@OptIn(ExperimentalContracts::class)
fun GuiGraphics.withExclusiveScissor(x: Int = 0, y: Int = 0, width: Int = 0, height: Int = 0, action: () -> Unit) {
    contract {
        callsInPlace(action, InvocationKind.EXACTLY_ONCE)
    }
    this.pushExclusiveScissor(ScreenRectangle(x, y, width, height))
    action()
    this.popExclusiveScissor()
}
