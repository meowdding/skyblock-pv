package me.owdding.skyblockpv.extensions

import com.teamresourceful.resourcefullib.client.screens.CursorScreen
import me.owdding.lib.displays.Display
import me.owdding.lib.displays.Displays
import me.owdding.skyblockpv.utils.components.CarouselWidget
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.network.chat.Component
import net.msrandom.classextensions.ClassExtension
import net.msrandom.classextensions.ExtensionShadow
import tech.thatgravyboat.skyblockapi.helpers.McFont
import tech.thatgravyboat.skyblockapi.utils.extensions.translated
import tech.thatgravyboat.skyblockapi.utils.extentions.scissor

@ClassExtension(CarouselWidget::class)
abstract class CarouselWidgetExtension : AbstractWidget(0, 0, 0, 0, Component.empty()) {

    @ExtensionShadow private val displays: List<Display> = error("Displays list is not initialized")
    @ExtensionShadow var index: Int = error("Index is not initialized")
    @ExtensionShadow private var cursor: CursorScreen.Cursor = error("Cursor is not initialized")

    @ExtensionShadow private val leftWidth: Int = error("Left width is not initialized")
    @ExtensionShadow private val rightWidth: Int = error("Right width is not initialized")

    override fun renderWidget(graphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTicks: Float) {
        cursor = CursorScreen.Cursor.DEFAULT

        val curr = displays.getOrNull(index) ?: return
        val last = displays.getOrNull((index - 1 + displays.size) % displays.size)
        val next = displays.getOrNull((index + 1) % displays.size)

        val left = x + (width - curr.getWidth()) / 2
        val right = left + curr.getWidth()

        val midY = y + 10
        val bottom = y + curr.getHeight()
        val sideHeight = bottom - midY

        val lastDiff = curr.getHeight() - 10 - (curr.getHeight() - (last?.getHeight() ?: 0)).coerceAtLeast(0)
        val lastY = midY + sideHeight - lastDiff
        val lastBottom = lastY + lastDiff
        graphics.scissor(x..left, lastY..lastBottom) {

            Displays.disableTooltips {
                last?.render(graphics, x, lastY)
            }

            graphics.translated(0f, 0f, 300f) {
                graphics.fill(x, lastY, left, lastBottom, 0x7F000000)

                if (mouseX in x..left && mouseY in lastY..lastBottom) {
                    translate(x + (left - x) / 2f, lastY + lastDiff / 2f - 7.5f, 0f)
                    scale(2f, 2f, 1f)
                    graphics.drawString(McFont.self, "<", -leftWidth / 2, 0, 0xFFFFFF)
                    cursor = CursorScreen.Cursor.POINTER
                }
            }
        }

        val nextDiff = curr.getHeight() - 10 - (curr.getHeight() - (next?.getHeight() ?: 0)).coerceAtLeast(0)
        val nextY = midY + sideHeight - nextDiff
        val nextBottom = nextY + nextDiff

        graphics.scissor(right..(x + width), nextY..nextBottom) {
            Displays.disableTooltips {
                next?.render(graphics, x + width, nextY, alignmentX = 1f)
            }

            graphics.translated(0f, 0f, 300f) {
                graphics.fill(right, nextY, x + width, nextBottom, 0x7F000000)

                if (mouseX in right..(x + width) && mouseY in nextY..nextBottom) {
                    translate(right + (x + width - right) / 2f, nextY + nextDiff / 2f - 7.5f, 0f)
                    scale(2f, 2f, 1f)
                    graphics.drawString(McFont.self, ">", -rightWidth / 2, 0, 0xFFFFFF)
                    cursor = CursorScreen.Cursor.POINTER
                }
            }
        }

        graphics.translated(0f, 0f, 150f) {
            curr.render(graphics, x + width / 2, y, alignmentX = 0.5f, alignmentY = 0f)
        }
    }
}
