package me.owdding.skyblockpv.utils.components

import com.mojang.blaze3d.platform.cursor.CursorType
import com.mojang.blaze3d.platform.cursor.CursorTypes
import earth.terrarium.olympus.client.components.base.BaseWidget
import earth.terrarium.olympus.client.components.buttons.Button
import earth.terrarium.olympus.client.components.renderers.WidgetRenderers
import me.owdding.lib.displays.Display
import me.owdding.lib.displays.DisplayWidget
import me.owdding.lib.displays.Displays
import me.owdding.skyblockpv.screens.windowed.elements.ExtraConstants
import me.owdding.skyblockpv.utils.CarouselPageState
import me.owdding.skyblockpv.utils.ExtraWidgetRenderers
import me.owdding.skyblockpv.utils.PvPageState
import me.owdding.skyblockpv.utils.Utils
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.client.gui.layouts.LayoutSettings
import net.minecraft.client.input.MouseButtonEvent
import tech.thatgravyboat.skyblockapi.helpers.McFont
import tech.thatgravyboat.skyblockapi.platform.drawString
import tech.thatgravyboat.skyblockapi.platform.pushPop
import tech.thatgravyboat.skyblockapi.platform.scale
import tech.thatgravyboat.skyblockapi.platform.translate
import tech.thatgravyboat.skyblockapi.utils.extentions.scissor
import kotlin.collections.mapIndexed


class CarouselWidget(
    private val displays: List<Display>,
    var index: Int = 0,
    width: Int,
    minHeight: Int = 0,
    private val preview: (index: Int, direction: Int) -> Display? = { _, _ -> null },
    private val onStep: (index: Int, direction: Int) -> Boolean = { _, _ -> false },
) : BaseWidget() {

    val leftWidth = McFont.self.width("<")
    val rightWidth = McFont.self.width(">")

    init {
        this.height = maxOf(displays.maxOfOrNull(Display::getHeight) ?: 0, minHeight) + SIDE_OFFSET
        this.width = width
    }

    private fun sideDisplay(direction: Int): Display? =
        preview(index, direction) ?: displays.getOrNull((index + direction + displays.size) % displays.size)

    private fun step(direction: Int) {
        if (!onStep(index, direction)) index = (index + direction + displays.size) % displays.size
    }

    private fun GuiGraphicsExtractor.renderCarouselOverlay(block: GuiGraphicsExtractor.() -> Unit) {
        this.pushPop {
            block()
        }
    }

    private fun GuiGraphicsExtractor.isVisible(left: Int, top: Int, right: Int, bottom: Int): Boolean {
        for (px in left until right step 4) {
            for (py in top until bottom step 4) {
                if (containsPointInScissor(px, py)) return true
            }
        }
        return false
    }

    override fun extractWidgetRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partialTicks: Float) {
        graphics.requestCursor(CursorType.DEFAULT)

        val curr = displays.getOrNull(index) ?: return
        val last = sideDisplay(-1)
        val next = sideDisplay(1)

        val left = x + (width - curr.getWidth()) / 2
        val right = left + curr.getWidth()

        val lastDiff = last?.getHeight() ?: 0
        val lastY = y + SIDE_OFFSET
        val lastBottom = lastY + lastDiff
        if (graphics.isVisible(x, lastY, left, lastBottom)) graphics.scissor(x..left, lastY..lastBottom) {

            Displays.disableTooltips {
                last?.extract(graphics, x, lastY)
            }

            graphics.renderCarouselOverlay {
                graphics.fill(x, lastY, left, lastBottom, 0x7F000000)

                if (mouseX in x..left && mouseY in y..lastBottom) {
                    graphics.pushPop {
                        graphics.translate(x + (left - x) / 2f, lastY + lastDiff / 2f - 7.5f)
                        graphics.scale(2f, 2f)
                        graphics.drawString("<", -leftWidth / 2, 0, 0xFFFFFF)
                    }
                    graphics.requestCursor(CursorTypes.POINTING_HAND)
                }
            }
        }

        val nextDiff = next?.getHeight() ?: 0
        val nextY = y + SIDE_OFFSET
        val nextBottom = nextY + nextDiff

        if (graphics.isVisible(right, nextY, x + width, nextBottom)) graphics.scissor(right..(x + width), nextY..nextBottom) {
            Displays.disableTooltips {
                next?.extract(graphics, x + width, nextY, alignmentX = 1f)
            }

            graphics.renderCarouselOverlay {
                graphics.fill(right, nextY, x + width, nextBottom, 0x7F000000)

                if (mouseX in right..(x + width) && mouseY in y..nextBottom) {
                    graphics.pushPop {
                        graphics.translate(right + (x + width - right) / 2f, nextY + nextDiff / 2f - 7.5f)
                        graphics.scale(2f, 2f)
                        graphics.drawString(">", -rightWidth / 2, 0, 0xFFFFFF)
                    }
                    graphics.requestCursor(CursorTypes.POINTING_HAND)
                }
            }
        }

        curr.extract(graphics, x + width / 2, y, alignmentX = 0.5f, alignmentY = 0f)
    }

    override fun onClick(event: MouseButtonEvent, doubleClick: Boolean) {
        val mouseX = event.x.toInt()
        val mouseY = event.y.toInt()
        val curr = displays.getOrNull(index) ?: return

        val left = x + (width - curr.getWidth()) / 2
        val right = left + curr.getWidth()

        val lastBottom = y + SIDE_OFFSET + (sideDisplay(-1)?.getHeight() ?: 0)
        if (mouseX in x..left && mouseY in y..lastBottom) {
            step(-1)
            return
        }

        val nextBottom = y + SIDE_OFFSET + (sideDisplay(1)?.getHeight() ?: 0)
        if (mouseX in right..(x + width) && mouseY in y..nextBottom) {
            step(1)
        }
    }

    fun getIcons(perRow: Int = 9, page: PvPageState, displays: () -> List<Display>): Layout {
        val buttons = displays.invoke().mapIndexed { index, display ->
            carouselIconButton(display, { this.index == index }) {
                this.index = index
                Utils.lastTab = CarouselPageState(page, index)
            }
        }

        val rows = buttons.chunked(perRow).map { PvLayouts.horizontal(1) { widget(it) } }
        return PvLayouts.vertical(1) {
            rows.forEach {
                widget(it, LayoutSettings::alignHorizontallyCenter)
            }
        }
    }

    companion object {
        private const val SIDE_OFFSET = 10
        fun carouselIconButton(display: Display, selected: () -> Boolean, onClick: () -> Unit): Button = Button()
            .withSize(20, 20)
            .withTexture(null)
            .withRenderer(
                WidgetRenderers.layered(
                    ExtraWidgetRenderers.conditional(
                        WidgetRenderers.sprite(ExtraConstants.BUTTON_PRIMARY_OPAQUE),
                        WidgetRenderers.sprite(ExtraConstants.BUTTON_DARK_OPAQUE),
                    ) { selected() },
                    WidgetRenderers.center(20, 20, WidgetRenderers.padded(1, 2, 3, 2, DisplayWidget.displayRenderer(display))),
                ),
            ).withCallback(onClick)
    }
}

