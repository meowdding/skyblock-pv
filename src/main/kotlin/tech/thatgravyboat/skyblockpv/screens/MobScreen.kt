package tech.thatgravyboat.skyblockpv.screens

import com.teamresourceful.resourcefullib.client.screens.BaseCursorScreen
import earth.terrarium.olympus.client.components.base.ListWidget
import earth.terrarium.olympus.client.components.string.TextWidget
import earth.terrarium.olympus.client.ui.UIConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.layouts.FrameLayout
import net.minecraft.client.gui.layouts.LinearLayout
import tech.thatgravyboat.skyblockapi.utils.text.CommonText
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockpv.api.ProfileAPI
import tech.thatgravyboat.skyblockpv.data.MobData
import tech.thatgravyboat.skyblockpv.utils.widgets.SpriteWidget
import java.util.*

private const val ASPECT_RATIO = 9.0 / 16.0

object MobScreen : BaseCursorScreen(CommonText.EMPTY) {
    override fun init() {
        val width = (this.width * 0.6).toInt()
        val height = (width * ASPECT_RATIO).toInt()

        val bg = SpriteWidget()
            .withTexture(UIConstants.BUTTON.enabled)
            .withSize(width, height)

        val uuid = UUID.fromString("b75d7e0a-03d0-4c2a-ae47-809b6b808246")

        CoroutineScope(Dispatchers.IO).launch {
            val screen = this@MobScreen
            val columnWidth = width / 2 - 20
            val columnHeight = height - 20

            val profiles = ProfileAPI.getProfiles(uuid)
            val profile = profiles.find { it.selected }

            val row = LinearLayout.horizontal().spacing(5)

            val mobData = profile?.mobData ?: emptyList()
            val sortedByKills = mobData.filter { it.kills != 0L }.sortedByDescending { it.kills }
            val sortedByDeaths = mobData.filter { it.deaths != 0L }.sortedByDescending { it.deaths }

            val killsColumn = createList("Kills", sortedByKills, true, columnWidth, columnHeight)
            val deathsColumn = createList("Deaths", sortedByDeaths, false, columnWidth, columnHeight)

            row.addChild(killsColumn)
            row.addChild(deathsColumn)

            row.arrangeElements()

            FrameLayout.centerInRectangle(bg, 0, 0, screen.width, screen.height)
            FrameLayout.centerInRectangle(row, bg.x, bg.y, bg.width, bg.height)

            bg.visitWidgets(screen::addRenderableOnly)
            row.visitWidgets(screen::addRenderableWidget)
        }
    }

    private fun createList(name: String, list: List<MobData>, useKills: Boolean, width: Int, height: Int): LinearLayout {
        val column = LinearLayout.vertical().spacing(5)
        val listWidget = ListWidget(width, height)

        list.forEach { (id, kills, death) ->
            val formattedName = id.split("_").joinToString(" ") { it.replaceFirstChar { it.titlecase() } }
            listWidget.add(TextWidget(Text.of("$formattedName: ${if (useKills) kills else death}")))
        }

        column.addChild(TextWidget(Text.of(name)))
        column.addChild(listWidget)

        return column
    }

    override fun renderBackground(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        this.renderBlurredBackground()
        this.renderTransparentBackground(guiGraphics)
    }
}
