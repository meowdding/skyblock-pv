package me.owdding.skyblockpv.utils.components

import com.mojang.blaze3d.platform.cursor.CursorType
import com.mojang.blaze3d.platform.cursor.CursorTypes
import earth.terrarium.olympus.client.components.buttons.Button
import earth.terrarium.olympus.client.components.renderers.WidgetRenderers
import me.owdding.lib.displays.Display
import me.owdding.lib.displays.DisplayWidget
import me.owdding.lib.displays.Displays
import me.owdding.lib.platform.screens.BaseWidget
import me.owdding.lib.platform.screens.MouseButtonEvent
import me.owdding.skyblockpv.screens.windowed.elements.ExtraConstants
import me.owdding.skyblockpv.utils.CarouselPageState
import me.owdding.skyblockpv.utils.ExtraWidgetRenderers
import me.owdding.skyblockpv.utils.PvPageState
import me.owdding.skyblockpv.utils.Utils
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.client.gui.layouts.LayoutSettings
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
) : BaseWidget() {

    val leftWidth = McFont.self.width("<")
    val rightWidth = McFont.self.width(">")

    init {
        this.height = displays.maxOfOrNull(Display::getHeight) ?: 0
        this.width = width
    }

    private fun GuiGraphicsExtractor.renderCarouselOverlay(block: GuiGraphicsExtractor.() -> Unit) {
        this.pushPop {
            block()
        }
    }

    override fun extractWidgetRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partialTicks: Float) {
        graphics.requestCursor(CursorType.DEFAULT)

        val curr = displays.getOrNull(index) ?: return
        val last = displays.getOrNull((index - 1 + displays.size) % displays.size)
        val next = displays.getOrNull((index + 1) % displays.size)

        val left = x + (width - curr.getWidth()) / 2
        val right = left + curr.getWidth()

        val midY = y + 10
        val sideBottom = y + height

        graphics.scissor(x..left, midY..sideBottom) {

            Displays.disableTooltips {
                last?.extract(graphics, x, midY)
            }

            graphics.renderCarouselOverlay {
                graphics.fill(x, midY, left, sideBottom, 0x7F000000)

                if (mouseX in x..left && mouseY in midY..sideBottom) {
                    graphics.pushPop {
                        graphics.translate(x + (left - x) / 2f, midY + (sideBottom - midY) / 2f - 7.5f)
                        graphics.scale(2f, 2f)
                        graphics.drawString("<", -leftWidth / 2, 0, 0xFFFFFF)
                    }
                    graphics.requestCursor(CursorTypes.POINTING_HAND)
                }
            }
        }

        graphics.scissor(right..(x + width), midY..sideBottom) {
            Displays.disableTooltips {
                next?.extract(graphics, x + width, midY, alignmentX = 1f)
            }

            graphics.renderCarouselOverlay {
                graphics.fill(right, midY, x + width, sideBottom, 0x7F000000)

                if (mouseX in right..(x + width) && mouseY in midY..sideBottom) {
                    graphics.pushPop {
                        graphics.translate(right + (x + width - right) / 2f, midY + (sideBottom - midY) / 2f - 7.5f)
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
        val (mouseX, mouseY) = event
        val curr = displays.getOrNull(index) ?: return

        val left = x + (width - curr.getWidth()) / 2
        val right = left + curr.getWidth()

        val midY = y + 10
        val sideBottom = y + height

        if (mouseX.toInt() in x..left && mouseY.toInt() in midY..sideBottom) {
            index = (index - 1 + displays.size) % displays.size
        }

        if (mouseX.toInt() in right..(x + width) && mouseY.toInt() in midY..sideBottom) {
            index = (index + 1) % displays.size
        }
    }

    fun getIcons(perRow: Int = 9, page: PvPageState, displays: () -> List<Display>): Layout {
        val buttons = displays.invoke().mapIndexed { index, display ->
            Button()
                .withSize(20, 20)
                .withTexture(null)
                .withRenderer(
                    WidgetRenderers.layered(
                        ExtraWidgetRenderers.conditional(
                            WidgetRenderers.sprite(ExtraConstants.BUTTON_PRIMARY_OPAQUE),
                            WidgetRenderers.sprite(ExtraConstants.BUTTON_DARK_OPAQUE),
                        ) { this.index == index },
                        WidgetRenderers.center(20, 20, WidgetRenderers.padded(1, 2, 3, 2, DisplayWidget.displayRenderer(display))),
                    ),
                ).withCallback {
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
}
