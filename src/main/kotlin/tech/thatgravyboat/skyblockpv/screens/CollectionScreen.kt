package tech.thatgravyboat.skyblockpv.screens

import com.teamresourceful.resourcefullib.client.screens.BaseCursorScreen
import earth.terrarium.olympus.client.components.Widgets
import earth.terrarium.olympus.client.ui.UIConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.layouts.FrameLayout
import net.minecraft.client.gui.layouts.LinearLayout
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.text.CommonText
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockpv.api.ProfileAPI
import tech.thatgravyboat.skyblockpv.utils.widgets.SpriteWidget
import java.util.*

private const val ASPECT_RATIO = 9.0 / 16.0

object CollectionScreen : BaseCursorScreen(CommonText.EMPTY) {
    override fun init() {
        val width = (this.width * 0.6).toInt()
        val height = (width * ASPECT_RATIO).toInt()

        val bg = SpriteWidget()
            .withTexture(UIConstants.BUTTON.enabled)
            .withSize(width, height)

        val uuid = UUID.fromString("b75d7e0a-03d0-4c2a-ae47-809b6b808246")

        CoroutineScope(Dispatchers.IO).launch {
            val screen = this@CollectionScreen

            val profiles = ProfileAPI.getProfiles(uuid)
            val profile = profiles.find { it.selected }


            val row = LinearLayout.horizontal().spacing(5)

            profile?.collections?.sortedBy { it.category.ordinal }?.chunked(31)?.forEach { chunk ->
                val col = LinearLayout.vertical().spacing(1)
                chunk.forEach { (category, id, item, amount) ->
                    val text = Text.join("§8[${category.displayName}] ", item?.hoverName ?: id, ": ${amount.toFormattedString()}")
                    col.addChild(Widgets.text(text))
                }
                col.arrangeElements()
                row.addChild(col)
            }

            row.arrangeElements()

            FrameLayout.centerInRectangle(bg, 0, 0, screen.width, screen.height)
            FrameLayout.centerInRectangle(row, bg.x, bg.y, bg.width, bg.height)

            bg.visitWidgets(screen::addRenderableWidget)
            row.visitWidgets(screen::addRenderableWidget)
        }
    }

    override fun renderBackground(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        this.renderBlurredBackground()
        this.renderTransparentBackground(guiGraphics)
    }
}
