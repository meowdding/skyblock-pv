package tech.thatgravyboat.skyblockpv.screens

import com.teamresourceful.resourcefullib.client.screens.BaseCursorScreen
import earth.terrarium.olympus.client.ui.UIConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.layouts.FrameLayout
import tech.thatgravyboat.skyblockapi.utils.text.CommonText
import tech.thatgravyboat.skyblockpv.utils.displays.DisplayWidget
import tech.thatgravyboat.skyblockpv.utils.displays.Displays
import tech.thatgravyboat.skyblockpv.utils.displays.asWidget
import java.util.UUID

private const val ASPECT_RATIO = 9.0 / 16.0

abstract class BasePvScreen(val uuid: UUID) : BaseCursorScreen(CommonText.EMPTY) {

    abstract suspend fun create(width: Int, height: Int, bg: DisplayWidget)

    override fun init() {
        val width = (this.width * 0.6).toInt()
        val height = (width * ASPECT_RATIO).toInt()

        val bg = Displays.background(UIConstants.BUTTON.enabled, width, height).asWidget()

        CoroutineScope(Dispatchers.IO).launch {
            val screen = this@BasePvScreen

            FrameLayout.centerInRectangle(bg, 0, 0, screen.width, screen.height)

            bg.visitWidgets(screen::addRenderableOnly)

            create(width, height, bg)
        }
    }

    override fun renderBackground(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        this.renderBlurredBackground()
        this.renderTransparentBackground(guiGraphics)
    }
}
