package tech.thatgravyboat.skyblockpv.screens

import com.teamresourceful.resourcefullib.client.screens.BaseCursorScreen
import earth.terrarium.olympus.client.components.buttons.Button
import earth.terrarium.olympus.client.components.renderers.WidgetRenderers
import earth.terrarium.olympus.client.ui.UIConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.layouts.FrameLayout
import net.minecraft.client.gui.layouts.LinearLayout
import net.minecraft.network.chat.Component
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.Logger
import tech.thatgravyboat.skyblockapi.utils.text.CommonText
import tech.thatgravyboat.skyblockpv.utils.displays.DisplayWidget
import tech.thatgravyboat.skyblockpv.utils.displays.Displays
import tech.thatgravyboat.skyblockpv.utils.displays.asWidget
import java.util.UUID

private const val ASPECT_RATIO = 9.0 / 16.0

abstract class BasePvScreen(val name: String, val uuid: UUID) : BaseCursorScreen(CommonText.EMPTY) {

    abstract suspend fun create(width: Int, height: Int, bg: DisplayWidget)

    override fun init() {
        val width = (this.width * 0.6).toInt()
        val height = (width * ASPECT_RATIO).toInt()

        val bg = Displays.background(UIConstants.BUTTON.enabled, width, height).asWidget()

        CoroutineScope(Dispatchers.IO).launch {
            val screen = this@BasePvScreen

            FrameLayout.centerInRectangle(bg, 0, 0, screen.width, screen.height)

            bg.visitWidgets(screen::addRenderableOnly)

            val tabs = LinearLayout.vertical().spacing(2)

            // as you can see, maya has no idea what she is doing
            PvTabs.entries.forEach { tab ->
                val button = Button()
                button.withCallback { McClient.tell { McClient.setScreen(tab.create(uuid)) } }
                button.setSize(20, 20)
                if (tab.name == name) {
                    button.withRenderer(WidgetRenderers.icon<AbstractWidget>(UIConstants.BUTTON.enabled))
                } else {
                    button.withRenderer(WidgetRenderers.icon<AbstractWidget>(UIConstants.BUTTON.disabled))
                }
                button.withTooltip(Component.literal(tab.name))
                tabs.addChild(button)
            }

            tabs.arrangeElements()
            tabs.setPosition(bg.x + bg.width, bg.y + 5)

            tabs.visitWidgets(this@BasePvScreen::addRenderableWidget)

            create(width, height, bg)
        }
    }

    override fun renderBackground(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        this.renderBlurredBackground()
        this.renderTransparentBackground(guiGraphics)
    }
}
