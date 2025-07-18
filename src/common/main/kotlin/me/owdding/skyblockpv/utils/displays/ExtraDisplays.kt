package me.owdding.skyblockpv.utils.displays

import earth.terrarium.olympus.client.utils.Orientation
import me.owdding.lib.displays.Display
import me.owdding.lib.displays.Displays
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.utils.drawRoundedRec
import me.owdding.skyblockpv.utils.render.RenderUtils.withTextShader
import me.owdding.skyblockpv.utils.render.TextShader
import me.owdding.skyblockpv.utils.render.dropdown.createDropdownDisplay
import me.owdding.skyblockpv.utils.render.dropdown.createDropdownOverlay
import me.owdding.skyblockpv.utils.theme.PvColors
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.FormattedText
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.ARGB
import net.minecraft.util.FormattedCharSequence
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.platform.drawSprite
import tech.thatgravyboat.skyblockapi.platform.pushPop
import tech.thatgravyboat.skyblockapi.utils.extentions.scissor
import java.util.concurrent.CompletableFuture
import kotlin.math.cos
import kotlin.math.sin
import me.owdding.skyblockpv.utils.render.RenderUtils as SbPvRenderUtils

object ExtraDisplays {

    fun inventorySlot(display: Display, color: Int = -1) = inventoryBackground(1, Orientation.HORIZONTAL, display, color)

    fun inventoryBackground(size: Int, orientation: Orientation, display: Display, color: Int = -1): Display {
        return object : Display {
            override fun getWidth() = display.getWidth()
            override fun getHeight() = display.getHeight()

            override fun render(graphics: GuiGraphics) {
                SbPvRenderUtils.drawInventory(graphics, 0, 0, display.getWidth(), display.getHeight(), size, orientation, color)
                display.render(graphics)
            }
        }
    }

    fun inventoryBackground(columns: Int, rows: Int, display: Display, color: Int = -1): Display {
        if (rows == 1) {
            return inventoryBackground(columns, Orientation.HORIZONTAL, display, color)
        }
        if (columns == 1) {
            return inventoryBackground(rows, Orientation.VERTICAL, display, color)
        }

        return object : Display {
            override fun getWidth() = display.getWidth()
            override fun getHeight() = display.getHeight()

            override fun render(graphics: GuiGraphics) {
                SbPvRenderUtils.drawInventory(graphics, 0, 0, display.getWidth(), display.getHeight(), columns, rows, color)
                display.render(graphics)
            }
        }
    }

    fun text(text: String, color: () -> UInt = { PvColors.WHITE.toUInt() }, shadow: Boolean = true) = Displays.text(text, color, shadow)
    fun text(text: () -> String, color: () -> UInt = { PvColors.WHITE.toUInt() }, shadow: Boolean = true) = Displays.text(text, color, shadow)
    fun text(text: FormattedText, color: () -> UInt = { PvColors.WHITE.toUInt() }, shadow: Boolean = true) = Displays.text(text, color, shadow)

    fun text(sequence: FormattedCharSequence, color: () -> UInt = { PvColors.WHITE.toUInt() }, shadow: Boolean = true) =
        Displays.text(sequence, color, shadow)

    fun component(component: () -> Component, color: () -> UInt = { PvColors.WHITE.toUInt() }, shadow: Boolean = true) =
        Displays.component(component, color, shadow)

    fun component(component: Component, maxWidth: Int = -1, color: () -> UInt = { PvColors.WHITE.toUInt() }, shadow: Boolean = true) =
        Displays.component(component, maxWidth, color, shadow)

    fun wrappedText(text: FormattedText, maxWidth: Int, color: () -> UInt = { PvColors.DARK_GRAY.toUInt() }, shadow: Boolean = true) =
        Displays.wrappedText(text, maxWidth, color, shadow)

    fun progress(
        progress: Float,
        width: Int = 91,
        height: Int = 5,
    ): Display {
        return object : Display {
            private val background = SkyBlockPv.id("progressbar/background")
            private val foreground = SkyBlockPv.id("progressbar/foreground")
            private val foregroundMaxed = SkyBlockPv.id("progressbar/foreground_maxed")
            override fun getWidth() = width
            override fun getHeight() = height

            override fun render(graphics: GuiGraphics) {
                val progressWidth = (width * progress).toInt()
                graphics.drawSprite(background, 0, 0, width, height)
                if (progressWidth >= width) {
                    graphics.drawSprite(foregroundMaxed, 0, 0, width, height)
                } else {
                    graphics.scissor(0, 0, progressWidth, height) {
                        graphics.drawSprite(foreground, 0, 0, width, height)
                    }
                }
            }
        }
    }

    fun loading(width: Int = 50, height: Int = 50, speed: Int = 4): Display {
        val size = 5
        return object : Display {
            private var frames = 0L

            override fun getWidth(): Int = width
            override fun getHeight(): Int = height

            override fun render(graphics: GuiGraphics) {
                frames++

                graphics.pushPop {

                    val time = (frames % 360) * speed

                    for (degree in 0 until 360 step 30) {

                        val x = width / 2f + cos(Math.toRadians(degree.toDouble())) * width / 2.5f
                        val y = height / 2f + sin(Math.toRadians(degree.toDouble())) * height / 2.5f

                        val alpha = (sin(Math.toRadians(degree.toDouble() + time.toDouble() * -1)) * 128 + 128).toInt()

                        graphics.drawRoundedRec(
                            x = (x - size / 2f).toInt(),
                            y = (y - size / 2f).toInt(),
                            width = size,
                            height = size,
                            backgroundColor = ARGB.color(255.coerceAtMost(alpha), 0x80000000.toInt()),
                            radius = 60,
                        )
                    }
                }
            }
        }
    }

    fun grayText(text: String) = Displays.text(text, color = { PvColors.DARK_GRAY.toUInt() }, shadow = false)
    fun grayText(text: Component) = Displays.text(text, color = { PvColors.DARK_GRAY.toUInt() }, shadow = false)

    fun dropdownOverlay(original: Display, color: Int, context: DropdownContext): Display = createDropdownOverlay(original, color, context)
    fun dropdown(original: Display, dropdown: Display, context: DropdownContext): Display = createDropdownDisplay(original, dropdown, context)

    fun <T> completableDisplay(
        completable: CompletableFuture<T>,
        onComplete: (T) -> Display,
        onError: (Throwable) -> Display,
        onLoading: () -> Display = { loading() },
    ): Display {
        return object : Display {
            private var display: Display = onLoading()

            init {
                completable.whenCompleteAsync { result, error ->
                    McClient.runNextTick {
                        display = result?.runCatching(onComplete)?.fold({ it }, onError) ?: onError(error)
                    }
                }
            }

            override fun getWidth() = display.getWidth()
            override fun getHeight() = display.getHeight()

            override fun render(graphics: GuiGraphics) {
                display.render(graphics)
            }
        }
    }

    fun textShader(shader: TextShader?, display: Display): Display {
        return object : Display {
            override fun getWidth() = display.getWidth()
            override fun getHeight() = display.getHeight()

            override fun render(graphics: GuiGraphics) {
                graphics.withTextShader(shader) {
                    display.render(graphics)
                }
            }
        }
    }

    fun List<List<Any>>.asTable(spacing: Int = 0): Display =
        Displays.table(
            this.map {
                it.map { element ->
                    when (element) {
                        is Display -> element
                        is ResourceLocation -> Displays.sprite(element, 12, 12)
                        is Component -> Displays.text(element, { PvColors.DARK_GRAY.toUInt() }, false)
                        else -> Displays.text(element.toString(), color = { PvColors.DARK_GRAY.toUInt() }, shadow = false)
                    }
                }
            },
            spacing,
        )

}
