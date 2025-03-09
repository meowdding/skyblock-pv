package tech.thatgravyboat.skyblockpv.utils.components

import com.teamresourceful.resourcefullib.client.screens.CursorScreen
import earth.terrarium.olympus.client.components.base.BaseWidget
import net.minecraft.client.gui.GuiGraphics
import tech.thatgravyboat.skyblockapi.helpers.McFont
import tech.thatgravyboat.skyblockpv.utils.Utils.pushPop
import tech.thatgravyboat.skyblockpv.utils.Utils.scissorRange
import tech.thatgravyboat.skyblockpv.utils.displays.Display
import tech.thatgravyboat.skyblockpv.utils.displays.Displays

class CarouselWidget(
    private val displays: List<Display>,
    var index: Int = 0,
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

        val midY = y + height - curr.getHeight() + 10
        val bottom = y + height
        val sideHeight = bottom - midY

        graphics.scissorRange(x..left, midY..bottom) {
            Displays.disableTooltips {
                last?.render(graphics, x, midY)
            }

            graphics.pushPop {
                translate(0f, 0f, 300f)
                graphics.fill(x, midY, left, bottom, 0x7F000000)

                if (graphics.containsPointInScissor(mouseX, mouseY)) {
                    translate(x + (left - x) / 2f, midY + sideHeight / 2f - 7.5f, 0f)
                    scale(2f, 2f, 1f)
                    graphics.drawCenteredString(McFont.self, "<", 0, 0, 0xFFFFFF)
                    cursor = CursorScreen.Cursor.POINTER
                }
            }
        }
        graphics.scissorRange(right..(x + width), midY..bottom) {
            Displays.disableTooltips {
                next?.render(graphics, x + width, midY, alignmentX = 1f)
            }

            graphics.pushPop {
                translate(0f, 0f, 300f)
                graphics.fill(right, midY, x + width, bottom, 0x7F000000)

                if (graphics.containsPointInScissor(mouseX, mouseY)) {
                    translate(right + (x + width - right) / 2f, midY + sideHeight / 2f - 7.5f, 0f)
                    scale(2f, 2f, 1f)
                    graphics.drawCenteredString(McFont.self, ">", 0, 0, 0xFFFFFF)
                    cursor = CursorScreen.Cursor.POINTER
                }
            }
        }
        graphics.pushPop {
            translate(0f, 0f, 150f)
            curr.render(graphics, x + width / 2, y + height, alignmentX = 0.5f, alignmentY = 1f)
        }
    }

    override fun onClick(mouseX: Double, mouseY: Double) {
        val left = x + (width - displays[index].getWidth()) / 2
        val right = left + displays[index].getWidth()
        val midY = y + height - displays[index].getHeight()

        if (mouseX.toInt() in x until left && mouseY.toInt() in midY + 10 until y + height) {
            index = (index - 1 + displays.size) % displays.size
        } else if (mouseX.toInt() in right until x + width && mouseY.toInt() in midY + 10 until y + height) {
            index = (index + 1) % displays.size
        }
    }

    override fun getCursor(): CursorScreen.Cursor = cursor
}
