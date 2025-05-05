package me.owdding.skyblockpv.utils.displays

import earth.terrarium.olympus.client.utils.Orientation
import me.owdding.lib.displays.Display
import me.owdding.lib.displays.Displays
import me.owdding.lib.displays.Displays.isMouseOver
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.utils.Utils.drawRoundedRec
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.RenderType
import net.minecraft.util.ARGB
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.extentions.pushPop
import tech.thatgravyboat.skyblockapi.utils.extentions.scissor
import tech.thatgravyboat.skyblockapi.utils.extentions.translate
import tech.thatgravyboat.skyblockapi.utils.extentions.translated
import java.util.concurrent.CompletableFuture
import kotlin.math.cos
import kotlin.math.sin
import me.owdding.skyblockpv.utils.RenderUtils as SbPvRenderUtils

object ExtraDisplays {

    fun inventorySlot(display: Display, color: Int = -1) = inventoryBackground(1, Orientation.HORIZONTAL, display, color)

    fun inventoryBackground(size: Int, orientation: Orientation, display: Display, color: Int = -1): Display {
        return object : Display {
            override fun getWidth() = display.getWidth()
            override fun getHeight() = display.getHeight()

            override fun render(graphics: GuiGraphics) {
                SbPvRenderUtils.drawInventory(graphics, 0, 0, display.getWidth(), display.getHeight(), size, orientation, color)
                graphics.translated(0, 0, 2) {
                    display.render(graphics)
                }
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
                graphics.translated(0, 0, 2) {
                    display.render(graphics)
                }
            }
        }
    }

    fun progress(
        progress: Float,
        width: Int = 91,
        height: Int = 5,
    ): Display {
        return object : Display {
            private val background = SkyBlockPv.id("progressbar/background")
            private val foreground = SkyBlockPv.id("progressbar/foreground")
            override fun getWidth() = width
            override fun getHeight() = height

            override fun render(graphics: GuiGraphics) {
                val progressWidth = (width * progress).toInt()
                graphics.blitSprite(RenderType::guiTextured, background, 0, 0, width, height)
                graphics.scissor(0, 0, progressWidth, height) {
                    graphics.blitSprite(RenderType::guiTextured, foreground, 0, 0, width, height)
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
                            width = size.toInt(),
                            height = size.toInt(),
                            backgroundColor = ARGB.color(255.coerceAtMost(alpha), 0x80000000.toInt()),
                            radius = 60,
                        )
                    }
                }
            }
        }
    }

    fun dropdownOverlay(original: Display, color: Int, context: DropdownContext): Display {
        return object : Display {
            override fun getWidth() = original.getWidth()
            override fun getHeight() = original.getHeight()
            override fun render(graphics: GuiGraphics) {
                original.render(graphics)
                graphics.pushPop {
                    this.translate(0, 0, 200)
                    val difference = System.currentTimeMillis() - context.lastUpdated
                    if (context.currentDropdown != null) {
                        if (difference < context.fadeTime) {
                            val value = ((difference / context.fadeTime.toDouble()) * 255).toInt()
                            graphics.fill(0, 0, getWidth(), getHeight(), ARGB.multiply(color, ARGB.color(value, value, value, value)))
                        } else {
                            graphics.fill(0, 0, getWidth(), getHeight(), color)
                        }
                    } else if (difference < context.fadeTime) {
                        val value = ((1 - (difference / context.fadeTime.toDouble())) * 255).toInt()
                        graphics.fill(0, 0, getWidth(), getHeight(), ARGB.multiply(color, ARGB.color(value, value, value, value)))
                    }
                }
            }
        }
    }

    fun dropdown(original: Display, dropdown: Display, context: DropdownContext): Display {
        return object : Display {
            override fun getWidth() = original.getWidth()
            override fun getHeight() = original.getHeight()
            var isOpen = false
            override fun render(graphics: GuiGraphics) {
                original.render(graphics)

                if (context.isCurrentDropdown(this) && (isMouseOver(original, graphics) || (isOpen && isMouseOver(dropdown, graphics)))) {
                    isOpen = true
                    context.currentDropdown = this
                    graphics.pushPop {
                        translate(0, 0, 201)
                        dropdown.render(graphics)
                    }
                } else {
                    if (context.currentDropdown === this) {
                        context.currentDropdown = null
                    }
                    isOpen = false
                }
            }
        }
    }

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
                    McClient.tell {
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
}
