package me.owdding.skyblockpv.utils.components

import com.teamresourceful.resourcefullib.client.screens.CursorScreen
import earth.terrarium.olympus.client.components.base.BaseWidget
import earth.terrarium.olympus.client.components.buttons.Button
import earth.terrarium.olympus.client.components.renderers.WidgetRenderers
import earth.terrarium.olympus.client.ui.UIConstants
import me.owdding.lib.builder.LayoutFactory
import me.owdding.lib.displays.Display
import me.owdding.lib.displays.DisplayWidget
import me.owdding.lib.displays.Displays
import me.owdding.skyblockpv.utils.ExtraWidgetRenderers
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.client.gui.layouts.LayoutSettings
import tech.thatgravyboat.skyblockapi.helpers.McFont
import tech.thatgravyboat.skyblockapi.utils.extentions.scissorRange
import tech.thatgravyboat.skyblockapi.utils.extentions.translated

class CarouselWidget(
    private val displays: List<Display>,
    var index: Int = 0,
    width: Int,
) : BaseWidget() {

    private var cursor = CursorScreen.Cursor.DEFAULT
    val leftWidth = McFont.self.width("<")
    val rightWidth = McFont.self.width(">")

    init {
        this.height = displays.maxOfOrNull(Display::getHeight) ?: 0
        this.width = width
    }

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
        graphics.scissorRange(x..left, lastY..lastBottom) {

            Displays.disableTooltips {
                last?.render(graphics, x, lastY)
            }

            graphics.translated(0f, 0f, 300f) {
                graphics.fill(x, lastY, left, lastBottom, 0x7F000000)

                if (graphics.containsPointInScissor(mouseX, mouseY)) {
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

        graphics.scissorRange(right..(x + width), nextY..nextBottom) {
            Displays.disableTooltips {
                next?.render(graphics, x + width, nextY, alignmentX = 1f)
            }

            graphics.translated(0f, 0f, 300f) {
                graphics.fill(right, nextY, x + width, nextBottom, 0x7F000000)

                if (graphics.containsPointInScissor(mouseX, mouseY)) {
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

    override fun onClick(mouseX: Double, mouseY: Double) {
        val curr = displays.getOrNull(index) ?: return
        val last = displays.getOrNull((index - 1 + displays.size) % displays.size)
        val next = displays.getOrNull((index + 1) % displays.size)

        val left = x + (width - displays[index].getWidth()) / 2
        val right = left + displays[index].getWidth()

        val midY = y + 10
        val bottom = y + curr.getHeight()
        val sideHeight = bottom - midY

        val lastDiff = curr.getHeight() - 10 - (curr.getHeight() - (last?.getHeight() ?: 0)).coerceAtLeast(0)
        val lastY = midY + sideHeight - lastDiff
        val lastBottom = lastY + lastDiff

        if (mouseX.toInt() in x..left && mouseY.toInt() in lastY..lastBottom) {
            index = (index - 1 + displays.size) % displays.size
        }

        val nextDiff = curr.getHeight() - 10 - (curr.getHeight() - (next?.getHeight() ?: 0)).coerceAtLeast(0)
        val nextY = midY + sideHeight - nextDiff
        val nextBottom = nextY + nextDiff

        if (mouseX.toInt() in right..(x + width) && mouseY.toInt() in nextY..nextBottom) {
            index = (index + 1) % displays.size
        }
    }

    override fun getCursor(): CursorScreen.Cursor = cursor

    fun getIcons(perRow: Int = 9, displays: () -> List<Display>): Layout {
        val buttons = displays.invoke().mapIndexed { index, it ->
            Button()
                .withSize(20, 20)
                .withRenderer(
                    WidgetRenderers.layered(
                        ExtraWidgetRenderers.conditional(
                            WidgetRenderers.sprite(UIConstants.PRIMARY_BUTTON),
                            WidgetRenderers.sprite(UIConstants.DARK_BUTTON),
                        ) { this.index == index },
                        WidgetRenderers.center(16, 18, DisplayWidget.displayRenderer(it)),
                    ),
                ).withCallback {
                    this.index = index
                }
        }

        val rows = buttons.chunked(perRow).map { LayoutFactory.horizontal(1) { widget(it) } }
        return LayoutFactory.vertical(1) {
            rows.forEach { it ->
                widget(it, LayoutSettings::alignHorizontallyCenter)
            }
        }
    }
}
