package tech.thatgravyboat.skyblockpv.utils.components

import com.teamresourceful.resourcefullib.client.screens.CursorScreen
import earth.terrarium.olympus.client.components.base.BaseWidget
import net.minecraft.client.gui.GuiGraphics
import tech.thatgravyboat.skyblockapi.helpers.McFont
import tech.thatgravyboat.skyblockpv.utils.Utils.pushPop
import tech.thatgravyboat.skyblockpv.utils.Utils.scissor
import tech.thatgravyboat.skyblockpv.utils.displays.Display
import tech.thatgravyboat.skyblockpv.utils.displays.Displays

class CarouselWidget(
    private val displays: List<Display>,
    private var index: Int = 0,
    width: Int
) : BaseWidget() {

    private var cursor = CursorScreen.Cursor.DEFAULT

    init {
        this.height = displays.maxOf(Display::getHeight)
        this.width = width
    }

    override fun renderWidget(graphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTicks: Float) {
        cursor = CursorScreen.Cursor.DEFAULT

        val curr = displays.getOrNull(index) ?: return
        val last = displays.getOrNull((index - 1 + displays.size) % displays.size)
        val next = displays.getOrNull((index + 1) % displays.size)

        val left = x + (width - curr.getWidth()) / 2
        val right = left + curr.getWidth()

        graphics.scissor(x, y + 10, left - x, height - 10) {
            Displays.disableTooltips {
                last?.render(graphics, x, y + 10, alignmentX = 0f)
            }

            graphics.pushPop {
                translate(0f, 0f, 199f)
                graphics.fill(x, y + 10, left, y + height, 0x7F000000)

                if (graphics.containsPointInScissor(mouseX, mouseY)) {
                    translate(x + (left - x) / 2f, y + 10 + (height - 10) / 2f - 5f, 0f)
                    scale(2f, 2f, 1f)
                    graphics.drawCenteredString(McFont.self, "<", 0, 0, 0xFFFFFF)
                    cursor = CursorScreen.Cursor.POINTER
                }
            }
        }
        graphics.scissor(right, y + 10, x + width - right, height - 10) {
            Displays.disableTooltips {
                next?.render(graphics, x + width, y + 10, alignmentX = 1f)
            }

            graphics.pushPop {
                translate(0f, 0f, 199f)
                graphics.fill(right, y + 10, x + width, y + height, 0x7F000000)

                if (graphics.containsPointInScissor(mouseX, mouseY)) {
                    translate(right + (x + width - right) / 2f, y + 10 + (height - 10) / 2f - 5f, 0f)
                    scale(2f, 2f, 1f)
                    graphics.drawCenteredString(McFont.self, ">", 0, 0, 0xFFFFFF)
                    cursor = CursorScreen.Cursor.POINTER
                }
            }
        }
        graphics.pushPop {
            translate(0f, 0f, 150f)
            curr.render(graphics, x + width / 2, y, alignmentX = 0.5f)
        }
    }

    override fun onClick(mouseX: Double, mouseY: Double) {
        val left = x + (width - displays[index].getWidth()) / 2
        val right = left + displays[index].getWidth()

        if (mouseX.toInt() in x until left && mouseY.toInt() in y + 10 until y + height) {
            index = (index - 1 + displays.size) % displays.size
        } else if (mouseX.toInt() in right until x + width && mouseY.toInt() in y + 10 until y + height) {
            index = (index + 1) % displays.size
        }
    }

    override fun getCursor(): CursorScreen.Cursor = cursor
}
