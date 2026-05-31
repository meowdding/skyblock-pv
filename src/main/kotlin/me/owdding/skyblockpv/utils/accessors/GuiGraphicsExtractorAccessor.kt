package me.owdding.skyblockpv.utils.accessors

import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.navigation.ScreenRectangle
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

interface GuiGraphicsExtractorAccessor {

    fun `skyblockpv$popScissor`(): ScreenRectangle?
    fun `skyblockpv$pushScissor`(rectangle: ScreenRectangle)

}

fun GuiGraphicsExtractor.popExclusiveScissor(): ScreenRectangle? = (this as? GuiGraphicsExtractorAccessor)?.`skyblockpv$popScissor`()
fun GuiGraphicsExtractor.pushExclusiveScissor(scissor: ScreenRectangle) = (this as? GuiGraphicsExtractorAccessor)?.`skyblockpv$pushScissor`(scissor)

@OptIn(ExperimentalContracts::class)
fun GuiGraphicsExtractor.withExclusiveScissor(x: Int = 0, y: Int = 0, width: Int = 0, height: Int = 0, action: () -> Unit) {
    contract {
        callsInPlace(action, InvocationKind.EXACTLY_ONCE)
    }
    this.pushExclusiveScissor(ScreenRectangle(x, y, width, height))
    action()
    this.popExclusiveScissor()
}
