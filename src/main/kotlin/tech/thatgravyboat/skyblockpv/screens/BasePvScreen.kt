package tech.thatgravyboat.skyblockpv.screens

import com.teamresourceful.resourcefullib.client.screens.BaseCursorScreen
import earth.terrarium.olympus.client.components.buttons.Button
import earth.terrarium.olympus.client.components.renderers.WidgetRenderers
import earth.terrarium.olympus.client.components.string.TextWidget
import earth.terrarium.olympus.client.ui.UIConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.layouts.FrameLayout
import net.minecraft.client.gui.layouts.LinearLayout
import net.minecraft.network.chat.Component
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.text.CommonText
import tech.thatgravyboat.skyblockpv.api.ProfileAPI
import tech.thatgravyboat.skyblockpv.api.data.SkyblockProfile
import tech.thatgravyboat.skyblockpv.utils.displays.DisplayWidget
import tech.thatgravyboat.skyblockpv.utils.displays.Displays
import tech.thatgravyboat.skyblockpv.utils.displays.asWidget
import java.util.*

private const val ASPECT_RATIO = 9.0 / 16.0

abstract class BasePvScreen(val name: String, val uuid: UUID, var profile: SkyblockProfile? = null) : BaseCursorScreen(CommonText.EMPTY) {

    val uiWidth get() = (this.width * 0.6).toInt()
    val uiHeight get() = (uiWidth * ASPECT_RATIO).toInt()

    abstract suspend fun create(bg: DisplayWidget)

    override fun init() {
        val bg = Displays.background(UIConstants.BUTTON.enabled, uiWidth, uiHeight).asWidget()
        val loading = TextWidget(Component.literal("Loading..."))

        CoroutineScope(Dispatchers.IO).launch {
            val screen = this@BasePvScreen

            FrameLayout.centerInRectangle(bg, 0, 0, screen.width, screen.height)

            loading.withCenterAlignment()
            FrameLayout.centerInRectangle(loading, 0, 0, screen.width, screen.height)

            val tabs = LinearLayout.vertical().spacing(2)

            // as you can see, maya has no idea what she is doing
            PvTabs.entries.forEach { tab ->
                val button = Button()
                button.setSize(20, 20)
                if (tab.name == name) {
                    button.withTexture(UIConstants.PRIMARY_BUTTON)
                } else {
                    button.withCallback { McClient.tell { McClient.setScreen(tab.create(uuid, profile)) } }
                    button.withTexture(UIConstants.BUTTON)
                }
                // Don't bother actually aligning the icon yet, design will change anyway :3
                button.withRenderer(WidgetRenderers.center(16, 16) { gr, ctx, _ -> gr.renderItem(tab.icon, ctx.x, ctx.y) })
                button.withTooltip(Component.literal(tab.name))
                tabs.addChild(button)
            }

            tabs.arrangeElements()
            tabs.setPosition(bg.x + bg.width, bg.y + 5)

            bg.visitWidgets(screen::addRenderableOnly)
            tabs.visitWidgets(screen::addRenderableWidget)
            loading.visitWidgets(screen::addRenderableOnly)

            if (profile == null) profile = fetchProfile()
            create(bg)

            loading.visible = false
        }
    }

    override fun renderBackground(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        this.renderBlurredBackground()
        this.renderTransparentBackground(guiGraphics)
    }

    suspend fun fetchProfile(): SkyblockProfile? {
        return ProfileAPI.getProfiles(uuid).find { it.selected }
    }
}
