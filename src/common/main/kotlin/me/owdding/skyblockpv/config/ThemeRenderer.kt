package me.owdding.skyblockpv.config

import com.teamresourceful.resourcefulconfig.api.client.ResourcefulConfigElementRenderer
import com.teamresourceful.resourcefulconfig.api.types.ResourcefulConfigElement
import com.teamresourceful.resourcefulconfig.api.types.elements.ResourcefulConfigEntryElement
import com.teamresourceful.resourcefulconfig.api.types.entries.ResourcefulConfigValueEntry
import com.teamresourceful.resourcefulconfig.client.UIConstants
import com.teamresourceful.resourcefulconfig.client.components.ModSprites
import com.teamresourceful.resourcefulconfig.client.components.base.BaseWidget
import com.teamresourceful.resourcefulconfig.client.components.base.ListWidget
import com.teamresourceful.resourcefulconfig.client.components.base.SpriteButton
import com.teamresourceful.resourcefulconfig.client.screens.base.OverlayScreen
import tech.thatgravyboat.skyblockapi.platform.drawSprite
import me.owdding.skyblockpv.SkyBlockPv.id
import me.owdding.skyblockpv.utils.theme.ThemeHelper
import me.owdding.skyblockpv.utils.theme.ThemeLoader
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.renderer.RenderType
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import tech.thatgravyboat.skyblockapi.utils.text.Text
import java.util.function.Consumer
import java.util.function.Supplier
import kotlin.math.min

val THEME_RENDERER = id("theme_dropdown")

class ThemeRenderer(val element: ResourcefulConfigElement) : ResourcefulConfigElementRenderer {
    override fun title(): Component = Component.translatable("skyblockpv.config.theme")
    override fun description(): Component = Component.translatable("skyblockpv.config.theme.desc")

    override fun widgets(): List<AbstractWidget> {
        val entry = (element as ResourcefulConfigEntryElement).entry() as ResourcefulConfigValueEntry
        return listOf(
            ThemeWidget(
                getter = { entry.string },
                setter = { entry.string = it },
            ),
            SpriteButton.builder(12, 12)
                .padding(2)
                .sprite(ModSprites.RESET)
                .tooltip(UIConstants.RESET)
                .onPress { entry.reset() }
                .build(),
        )
    }
}

const val WIDTH: Int = 80

class ThemeWidget(
    val getter: Supplier<String>,
    val setter: Consumer<String>,
) : BaseWidget(WIDTH, 16) {

    override fun renderWidget(graphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTicks: Float) {

        graphics.drawSprite(
            ModSprites.ofButton(this.isHovered()),
            x,
            y,
            getWidth(),
            getHeight(),
        )
        renderScrollingString(
            graphics,
            this.font,
            Text.translatable(ThemeLoader.themes.entries.firstOrNull { (key) -> key.toString() == this.getter.get() }?.value?.name ?: "Unknown"),
            x + 4,
            y + 4,
            x + getWidth() - 16,
            y + getHeight() - 4,
            UIConstants.TEXT_PARAGRAPH,
        )
        graphics.drawSprite(
            ModSprites.CHEVRON_DOWN,
            x + getWidth() - 12,
            y + 4,
            8,
            8,
        )
    }

    override fun onClick(d: Double, e: Double) {
        Minecraft.getInstance().setScreen(DropdownOverlay(this))
    }

    class DropdownOverlay(private val widget: ThemeWidget) : OverlayScreen(Minecraft.getInstance().screen) {
        override fun init() {
            val list: DropdownList = addRenderableWidget(DropdownList.Companion.of(widget))
            for ((option, theme) in ThemeLoader.themes) {
                list.add(
                    DropdownItem(
                        option.toString(),
                        theme.name,
                    ) { value: String ->
                        widget.setter.accept(value)
                        this.onClose()
                    },
                )
            }
        }

        override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
            if (getChildAt(mouseX, mouseY).isEmpty) {
                this.onClose()
                return true
            }
            return super.mouseClicked(mouseX, mouseY, button)
        }
    }

    class DropdownList(x: Int, y: Int, height: Int) : ListWidget(x + 1, y, WIDTH - 2, height) {
        override fun renderWidget(graphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTicks: Float) {
            graphics.drawSprite(
                ModSprites.ACCENT,
                x - 1,
                y - 1,
                getWidth() + 2,
                getHeight() + 2,
            )
            graphics.drawSprite(
                ModSprites.BUTTON,
                x,
                y,
                getWidth(),
                getHeight(),
            )
            super.renderWidget(graphics, mouseX, mouseY, partialTicks)
        }

        companion object {
            fun of(widget: ThemeWidget): DropdownList {
                val windowHeight = Minecraft.getInstance().window.guiScaledHeight
                var widgetY = widget.y + widget.getHeight()
                val listHeight = min(ThemeHelper.ids.size * 12, 12 * 8) + 1
                if (widgetY + listHeight > windowHeight) {
                    widgetY = widget.y - listHeight - 1
                }
                return DropdownList(widget.x, widgetY, listHeight)
            }
        }
    }

    private class DropdownItem(private val option: String, private val translationKey: String, private val setter: Consumer<String>) : BaseWidget(WIDTH, 12),
        ListWidget.Item {
        override fun renderWidget(graphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTicks: Float) {
            graphics.drawSprite(
                ModSprites.ofButton(this.isHovered()),
                x + 1,
                y,
                getWidth() - 1,
                getHeight(),
            )
            val color = if (this.isHovered()) UIConstants.TEXT_TITLE else UIConstants.TEXT_PARAGRAPH
            renderScrollingString(
                graphics, this.font, Text.translatable(translationKey),
                x + 4, y + 1,
                x + getWidth() - 4, y + getHeight() - 1,
                color,
            )
        }

        override fun onClick(mouseX: Double, e: Double) {
            this.setter.accept(this.option)
        }

        override fun getRectangle(): ScreenRectangle = super<BaseWidget>.rectangle

        override fun setItemWidth(width: Int) {
            this.setWidth(width)
        }
    }
}
