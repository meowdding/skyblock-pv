package tech.thatgravyboat.skyblockpv.utils.displays

import com.mojang.blaze3d.vertex.PoseStack
import com.teamresourceful.resourcefullib.client.utils.RenderUtils
import com.teamresourceful.resourcefullib.client.utils.ScreenUtils
import earth.terrarium.olympus.client.utils.Orientation
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.PlayerFaceRenderer
import net.minecraft.client.gui.components.Renderable
import net.minecraft.client.gui.layouts.LayoutElement
import net.minecraft.client.gui.screens.inventory.InventoryScreen.renderEntityInInventory
import net.minecraft.client.renderer.RenderType
import net.minecraft.locale.Language
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.FormattedText
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.ARGB
import net.minecraft.util.FormattedCharSequence
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.ItemLike
import org.joml.Quaternionf
import org.joml.Vector3f
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McFont
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.width
import tech.thatgravyboat.skyblockapi.utils.text.TextUtils.splitLines
import tech.thatgravyboat.skyblockpv.SkyBlockPv
import tech.thatgravyboat.skyblockpv.utils.Utils.drawRoundedRec
import tech.thatgravyboat.skyblockpv.utils.Utils.pushPop
import tech.thatgravyboat.skyblockpv.utils.Utils.scissor
import tech.thatgravyboat.skyblockpv.utils.Utils.translate
import tech.thatgravyboat.skyblockpv.utils.Utils.translated
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.sin
import tech.thatgravyboat.skyblockpv.utils.RenderUtils as SbPvRenderUtils

private const val NO_SPLIT = -1

object Displays {

    private var showTooltips = true

    fun empty(width: Int = 0, height: Int = 0): Display {
        return object : Display {
            override fun getWidth() = width
            override fun getHeight() = height
            override fun render(graphics: GuiGraphics) {}
        }
    }

    fun supplied(display: () -> Display): Display {
        return object : Display {
            override fun getWidth() = display().getWidth()
            override fun getHeight() = display().getHeight()
            override fun render(graphics: GuiGraphics) {
                display().render(graphics)
            }
        }
    }

    fun fixed(width: Int, height: Int, display: Display): Display {
        return object : Display {
            override fun getWidth() = width
            override fun getHeight() = height
            override fun render(graphics: GuiGraphics) {
                graphics.scissor(0, 0, width, height) {
                    display.render(graphics)
                }
            }
        }
    }

    fun placeholder(width: Int, height: Int): Display {
        return fixed(width, height, empty())
    }

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

    fun background(color: UInt, display: Display): Display {
        return object : Display {
            override fun getWidth() = display.getWidth()
            override fun getHeight() = display.getHeight()
            override fun render(graphics: GuiGraphics) {
                graphics.fill(
                    0, 0,
                    getWidth(), getHeight(),
                    color.toInt(),
                )
                display.render(graphics)
            }
        }
    }

    fun background(sprite: ResourceLocation, display: Display, color: Int = -1): Display {
        return object : Display {
            override fun getWidth() = display.getWidth()
            override fun getHeight() = display.getHeight()
            override fun render(graphics: GuiGraphics) {
                graphics.blitSprite(RenderType::guiTextured, sprite, 0, 0, display.getWidth(), display.getHeight(), color.and(0xFFFFFF).or(-0xF000000))
                display.render(graphics)
            }
        }
    }

    fun background(sprite: ResourceLocation, width: Int, height: Int): Display {
        return background(sprite, empty(width, height))
    }

    fun padding(padding: Int, display: Display): Display {
        return padding(padding, padding, display)
    }

    fun padding(padX: Int, padY: Int, display: Display): Display {
        return padding(padX, padX, padY, padY, display)
    }

    fun padding(left: Int, right: Int, top: Int, bottom: Int, display: Display): Display {
        return object : Display {
            override fun getWidth() = left + display.getWidth() + right
            override fun getHeight() = top + display.getHeight() + bottom
            override fun render(graphics: GuiGraphics) {
                display.render(graphics, left, top)
            }
        }
    }

    fun center(width: Int = -1, height: Int = -1, display: Display): Display {
        return object : Display {
            override fun getWidth() = if (width == -1) display.getWidth() else width
            override fun getHeight() = if (height == -1) display.getHeight() else height
            override fun render(graphics: GuiGraphics) {
                display.render(graphics, (getWidth() - display.getWidth()) / 2, (getHeight() - display.getHeight()) / 2)
            }
        }
    }

    fun outline(color: () -> UInt, display: Display): Display {
        return object : Display {
            override fun getWidth() = display.getWidth() + 2
            override fun getHeight() = display.getHeight() + 2
            override fun render(graphics: GuiGraphics) {
                display.render(graphics, 1, 1)
                graphics.renderOutline(0, 0, getWidth(), getHeight(), color().toInt())
            }
        }
    }

    fun face(texture: () -> ResourceLocation, size: Int = 8): Display {
        return object : Display {
            override fun getWidth(): Int = size
            override fun getHeight(): Int = size

            override fun render(graphics: GuiGraphics) {
                PlayerFaceRenderer.draw(graphics, texture(), 0, 0, 8, true, false, -1)
            }
        }
    }

    fun sprite(sprite: ResourceLocation, width: Int, height: Int): Display {
        return object : Display {
            override fun getWidth() = width
            override fun getHeight() = height
            override fun render(graphics: GuiGraphics) {
                graphics.blitSprite(RenderType::guiTextured, sprite, width, height, 0, 0, 0, 0, width, height)
            }
        }
    }

    fun text(text: String, color: () -> UInt = { 0xFFFFFFFFu }, shadow: Boolean = true): Display {
        return text({ text }, color, shadow)
    }

    fun text(text: () -> String, color: () -> UInt = { 0xFFFFFFFFu }, shadow: Boolean = true): Display {
        return object : Display {

            val component: MutableComponent
                get() = Text.of(text())

            override fun getWidth() = component.width
            override fun getHeight() = 10
            override fun render(graphics: GuiGraphics) {
                graphics.drawString(McFont.self, component, 0, 1, color().toInt(), shadow)
            }
        }
    }

    fun component(component: () -> Component, color: () -> UInt = { 0xFFFFFFFFu }, shadow: Boolean = true): Display {
        return object : Display {
            override fun getWidth() = component().width
            override fun getHeight() = 10
            override fun render(graphics: GuiGraphics) {
                graphics.drawString(McFont.self, component(), 0, 1, color().toInt(), shadow)
            }
        }
    }

    fun component(
        component: Component,
        maxWidth: Int = NO_SPLIT,
        color: () -> UInt = { 0xFFFFFFFFu },
        shadow: Boolean = true,
    ): Display {
        val lines = if (maxWidth == NO_SPLIT) listOf(component.visualOrderText) else McFont.split(component, maxWidth)
        val width = lines.maxOfOrNull(McFont::width) ?: 0
        val height = lines.size * McFont.height

        return object : Display {
            override fun getWidth() = width
            override fun getHeight() = height
            override fun render(graphics: GuiGraphics) {
                lines.forEachIndexed { index, line ->
                    graphics.drawString(McFont.self, line, 0, index * McFont.height, color().toInt(), shadow)
                }
            }
        }
    }

    fun text(
        sequence: FormattedCharSequence,
        color: () -> UInt = { 0xFFFFFFFFu },
        shadow: Boolean = true,
    ): Display {
        return object : Display {
            override fun getWidth() = McFont.width(sequence)
            override fun getHeight() = McFont.height
            override fun render(graphics: GuiGraphics) {
                graphics.drawString(McFont.self, sequence, 0, 0, color().toInt(), shadow)
            }
        }
    }

    fun text(
        text: FormattedText,
        color: () -> UInt = { 0xFFFFFFFFu },
        shadow: Boolean = true,
    ) = text(Language.getInstance().getVisualOrder(text), color, shadow)

    fun row(
        vararg displays: Display,
        spacing: Int = 0,
        alignment: Alignment = Alignment.START,
    ): Display {
        return object : Display {
            override fun getWidth() = displays.sumOf { it.getWidth() } + spacing * (displays.size - 1)
            override fun getHeight() = displays.maxOfOrNull { it.getHeight() } ?: 0
            override fun render(graphics: GuiGraphics) {
                val maxHeight = getHeight()
                var currentX = 0

                displays.forEachIndexed { index, display ->
                    val yOffset = when (alignment) {
                        Alignment.START -> 0
                        Alignment.CENTER -> (maxHeight - display.getHeight()) / 2
                        Alignment.END -> maxHeight - display.getHeight()
                    }

                    graphics.translated(currentX, yOffset, 0) {
                        display.render(graphics)
                        currentX += display.getWidth() + spacing
                    }
                }
            }
        }
    }

    fun column(
        vararg displays: Display,
        spacing: Int = 0,
        alignment: Alignment = Alignment.START,
    ): Display {
        return object : Display {
            override fun getWidth() = displays.maxOfOrNull { it.getWidth() } ?: 0
            override fun getHeight() = displays.sumOf { it.getHeight() } + spacing * (displays.size - 1)

            override fun render(graphics: GuiGraphics) {
                val maxWidth = getWidth()
                var currentY = 0

                displays.forEach { display ->
                    val xOffset = when (alignment) {
                        Alignment.START -> 0
                        Alignment.CENTER -> (maxWidth - display.getWidth()) / 2
                        Alignment.END -> maxWidth - display.getWidth()
                    }
                    graphics.translated(xOffset, currentY) {
                        display.render(graphics)
                        currentY += display.getHeight() + spacing
                    }
                }
            }
        }
    }

    fun item(
        item: ItemLike,
        width: Int = 16,
        height: Int = 16,
        showTooltip: Boolean = false,
        showStackSize: Boolean = false,
        customStackText: Any? = null,
    ): Display {
        return item(item.asItem().defaultInstance, width, height, showTooltip, showStackSize, customStackText)
    }

    fun item(
        item: ItemStack,
        width: Int = 16,
        height: Int = 16,
        showTooltip: Boolean = false,
        showStackSize: Boolean = false,
        customStackText: Any? = null,
    ): Display {
        return object : Display {
            override fun getWidth() = width
            override fun getHeight() = height

            override fun render(graphics: GuiGraphics) {
                if (showTooltip && !item.isEmpty) {
                    if (isMouseOver(this, graphics)) {
                        ScreenUtils.setTooltip(item)
                    }
                }

                graphics.pushPop {
                    scale(width / 16f, height / 16f, 1f)
                    graphics.renderItem(item, 0, 0)

                    val stackSize = item.count
                    if ((showStackSize && stackSize > 1) || customStackText != null) {
                        translate(1, 2, 200)
                        val component = when (customStackText) {
                            null -> Text.of(stackSize.toString())
                            is Component -> customStackText
                            is String -> Text.of(customStackText)
                            else -> Text.of(customStackText.toString())
                        }
                        graphics.drawString(
                            McFont.self,
                            component,
                            width - McFont.width(component),
                            height - McFont.height,
                            0xFFFFFFFF.toInt(),
                            true,
                        )
                    }
                }
            }
        }
    }

    fun <T> renderable(renderable: T, width: Int = -1, height: Int = -1): Display
        where T : Renderable, T : LayoutElement {
        return object : Display {
            override fun getWidth(): Int = if (width == -1) renderable.width else width
            override fun getHeight(): Int = if (height == -1) renderable.height else height
            override fun render(graphics: GuiGraphics) {
                renderable.render(graphics, -1, -1, 0f)
            }
        }
    }

    fun layered(vararg displays: Display): Display {
        return object : Display {
            override fun getWidth() = displays.maxOfOrNull { it.getWidth() } ?: 0
            override fun getHeight() = displays.maxOfOrNull { it.getHeight() } ?: 0
            override fun render(graphics: GuiGraphics) {
                displays.forEach { it.render(graphics) }
            }
        }
    }

    fun pushPop(display: Display, operations: PoseStack.() -> Unit): Display {
        return object : Display {
            // Does not account for scaling
            override fun getWidth() = display.getWidth()
            override fun getHeight() = display.getHeight()
            override fun render(graphics: GuiGraphics) {
                graphics.pushPop {
                    operations()
                    display.render(graphics)
                }
            }
        }
    }

    fun entity(
        entity: LivingEntity,
        width: Int,
        height: Int,
        scale: Int,
        mouseX: Float = Float.NaN,
        mouseY: Float = Float.NaN,
        spinning: Boolean = false,
    ): Display {
        return object : Display {
            override fun getWidth() = width
            override fun getHeight() = height
            override fun render(graphics: GuiGraphics) {
                val centerX = width / 2f
                val centerY = height / 2f
                val eyesX = mouseX.takeIf { !it.isNaN() } ?: centerX
                val eyesY = mouseY.takeIf { !it.isNaN() } ?: centerY

                val rotationX = atan((centerX - eyesX) / 40.0).toFloat()
                val rotationY = atan((centerY - eyesY) / 40.0).toFloat()
                val baseRotation = Quaternionf().rotateZ(Math.PI.toFloat())
                val tiltRotation = Quaternionf().rotateX(rotationY * 20.0f * (Math.PI.toFloat() / 180f))

                if (spinning) {
                    val currentTime = System.currentTimeMillis() % 3600
                    val spinAngle = (currentTime / 10.0) % 360.0
                    baseRotation.mul(Quaternionf().rotateY(Math.toRadians(spinAngle).toFloat()))
                }

                baseRotation.mul(tiltRotation)
                val originalBodyRotation = entity.yBodyRot
                val originalYRotation = entity.yRot
                val originalXRotation = entity.xRot
                val originalHeadRotationPrev = entity.yHeadRotO
                val originalHeadRotation = entity.yHeadRot
                entity.yBodyRot = 180.0f + rotationX * 20.0f
                entity.yRot = 180.0f + rotationX * 40.0f
                entity.xRot = -rotationY * 20.0f
                entity.yHeadRot = entity.yRot
                entity.yHeadRotO = entity.yRot
                val entityScale = entity.scale
                val positionOffset = Vector3f(0.0f, entity.bbHeight / 2.0f * entityScale, 0.0f)
                val scaledSize = scale / entityScale
                renderEntityInInventory(
                    graphics,
                    centerX,
                    centerY,
                    scaledSize,
                    positionOffset,
                    baseRotation,
                    tiltRotation,
                    entity,
                )
                entity.yBodyRot = originalBodyRotation
                entity.yRot = originalYRotation
                entity.xRot = originalXRotation
                entity.yHeadRotO = originalHeadRotationPrev
                entity.yHeadRot = originalHeadRotation
            }
        }
    }

    fun table(
        table: List<List<Display>>,
        spacing: Int = 0,
    ): Display {
        return object : Display {
            val columnWidths = (0 until table.maxOf { it.size }).map { col ->
                table.maxOfOrNull { row -> row.getOrNull(col)?.getWidth() ?: 0 } ?: 0
            }

            override fun getHeight(): Int = table.sumOf { it.maxOf { it.getHeight() } } + (table.size - 1) * spacing
            override fun getWidth(): Int = columnWidths.sum() + (columnWidths.size - 1) * spacing

            override fun render(graphics: GuiGraphics) {
                var currentY = 0

                table.forEach { row ->
                    val rowHeight = row.maxOf { it.getHeight() }
                    var currentX = 0

                    graphics.translated(0, currentY) {
                        row.forEachIndexed { col, element ->
                            graphics.translated(currentX) {
                                element.render(graphics)
                            }
                            currentX += columnWidths[col] + spacing
                        }
                    }
                    currentY += rowHeight + spacing
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

    fun tooltip(display: Display, component: Component): Display {
        return object : Display {
            override fun getWidth() = display.getWidth()
            override fun getHeight() = display.getHeight()
            override fun render(graphics: GuiGraphics) {
                display.render(graphics)

                if (isMouseOver(display, graphics)) {
                    ScreenUtils.setTooltip(component.splitLines())
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

    fun disableTooltips(action: () -> Unit) {
        showTooltips = false
        action()
        showTooltips = true
    }

    private fun isMouseOver(display: Display, graphics: GuiGraphics): Boolean {
        val translation = RenderUtils.getTranslation(graphics.pose())
        val (mouseX, mouseY) = McClient.mouse
        val xRange = translation.x().toInt()..(translation.x() + display.getWidth())
        val yRange = translation.y().toInt()..(translation.y() + display.getHeight())
        return mouseX.toInt() in xRange && mouseY.toInt() in yRange && graphics.containsPointInScissor(mouseX.toInt(), mouseY.toInt()) && showTooltips
    }
}
