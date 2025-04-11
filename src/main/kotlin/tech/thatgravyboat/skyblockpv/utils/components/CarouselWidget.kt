package tech.thatgravyboat.skyblockpv.utils.components

import com.teamresourceful.resourcefullib.client.screens.CursorScreen
import earth.terrarium.olympus.client.components.base.BaseWidget
import earth.terrarium.olympus.client.components.buttons.Button
import earth.terrarium.olympus.client.components.renderers.WidgetRenderers
import earth.terrarium.olympus.client.layouts.Layouts
import earth.terrarium.olympus.client.layouts.LinearViewLayout
import earth.terrarium.olympus.client.ui.UIConstants
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.layouts.Layout
import tech.thatgravyboat.skyblockapi.helpers.McFont
import tech.thatgravyboat.skyblockpv.utils.ExtraWidgetRenderers
import tech.thatgravyboat.skyblockpv.utils.Utils.scissorRange
import tech.thatgravyboat.skyblockpv.utils.Utils.translated
import tech.thatgravyboat.skyblockpv.utils.displays.Display
import tech.thatgravyboat.skyblockpv.utils.displays.Displays

class CarouselWidget(
    private val displays: List<Display>,
    var index: Int = 0,
    width: Int,
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

        // Aligning the carousel to the top
        // @Sophie if theres a better solution pls tell me :3
        this.height = curr.getHeight()

        val left = x + (width - curr.getWidth()) / 2
        val right = left + curr.getWidth()

        val midY = y + height - curr.getHeight() + 10
        val bottom = y + height
        val sideHeight = bottom - midY

        graphics.scissorRange(x..left, midY..bottom) {
            Displays.disableTooltips {
                last?.render(graphics, x, midY)
            }

            graphics.translated(0f, 0f, 300f) {
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

            graphics.translated(0f, 0f, 300f) {
                graphics.fill(right, midY, x + width, bottom, 0x7F000000)

                if (graphics.containsPointInScissor(mouseX, mouseY)) {
                    translate(right + (x + width - right) / 2f, midY + sideHeight / 2f - 7.5f, 0f)
                    scale(2f, 2f, 1f)
                    graphics.drawCenteredString(McFont.self, ">", 0, 0, 0xFFFFFF)
                    cursor = CursorScreen.Cursor.POINTER
                }
            }
        }
        graphics.translated(0f, 0f, 150f) {
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

    fun getIcons(displays: () -> List<Display>): Layout {
        val buttons = displays.invoke().mapIndexed { index, it ->
            Button()
                .withSize(20, 20)
                .withRenderer(
                    WidgetRenderers.layered(
                        ExtraWidgetRenderers.conditional(
                            WidgetRenderers.sprite(UIConstants.PRIMARY_BUTTON),
                            WidgetRenderers.sprite(UIConstants.DARK_BUTTON),
                        ) { this.index == index },
                        WidgetRenderers.center(16, 20, ExtraWidgetRenderers.display(it)),
                    ),
                )
                .withCallback {
                    this.index = index
                }
        }

        return buttons.chunked(9)
            .map { it.fold(Layouts.row().withGap(1), LinearViewLayout::withChild) }
            .fold(Layouts.column().withGap(1), LinearViewLayout::withChild)
    }
}
