package tech.thatgravyboat.skyblockpv.screens

import com.mojang.authlib.GameProfile
import com.teamresourceful.resourcefullib.client.screens.BaseCursorScreen
import earth.terrarium.olympus.client.components.Widgets
import earth.terrarium.olympus.client.components.buttons.Button
import earth.terrarium.olympus.client.components.dropdown.DropdownState
import earth.terrarium.olympus.client.components.renderers.WidgetRenderers
import earth.terrarium.olympus.client.ui.OverlayAlignment
import earth.terrarium.olympus.client.ui.UIConstants
import earth.terrarium.olympus.client.utils.State
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.layouts.FrameLayout
import net.minecraft.client.gui.layouts.LayoutElement
import net.minecraft.network.chat.Component
import tech.thatgravyboat.skyblockapi.api.profile.profile.ProfileType
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.text.CommonText
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.Text.send
import tech.thatgravyboat.skyblockpv.api.ProfileAPI
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.screens.elements.ExtraConstants
import tech.thatgravyboat.skyblockpv.utils.LayoutBuild
import tech.thatgravyboat.skyblockpv.utils.LayoutBuilder.Companion.setPos
import tech.thatgravyboat.skyblockpv.utils.Utils
import tech.thatgravyboat.skyblockpv.utils.displays.DisplayWidget
import tech.thatgravyboat.skyblockpv.utils.displays.Displays
import tech.thatgravyboat.skyblockpv.utils.displays.asWidget
import java.io.File

private const val ASPECT_RATIO = 9.0 / 16.0

abstract class BasePvScreen(val name: String, val gameProfile: GameProfile, var profile: SkyBlockProfile? = null) : BaseCursorScreen(CommonText.EMPTY) {

    var profiles: List<SkyBlockProfile> = emptyList()

    val uiWidth get() = (this.width * 0.65).toInt()
    val uiHeight get() = (uiWidth * ASPECT_RATIO).toInt()

    var initedWithProfile = false

    open val tabTitle: Component get() = Text.translatable("skyblockpv.tab.${name.lowercase()}")

    init {
        CoroutineScope(Dispatchers.IO).launch {
            profiles = ProfileAPI.getProfiles(gameProfile.id)
            profile = profile ?: profiles.find { it.selected }
            if (!initedWithProfile) {
                McClient.tell { rebuildWidgets() }
            }
        }
    }

    abstract fun create(bg: DisplayWidget)

    override fun init() {
        val screen = this@BasePvScreen
        val bg = Displays.background(UIConstants.BUTTON.enabled, uiWidth, uiHeight).asWidget()
        val loading = Displays.loading().asWidget()

        FrameLayout.centerInRectangle(bg, 0, 0, screen.width, screen.height)
        FrameLayout.centerInRectangle(loading, 0, 0, screen.width, screen.height)

        bg.visitWidgets(screen::addRenderableOnly)
        loading.visitWidgets(screen::addRenderableOnly)

        if (profile == null) return
        initedWithProfile = true

        try {
            create(bg)
        } catch (e: Exception) {
            Text.of("An error occurred while rendering the screen: ${e.message}").send()
        }

        val tabs = createTabs().setPos(bg.x + bg.width - 9, bg.y + 5)
        tabs.visitWidgets(screen::addRenderableWidget)

        val username = createSearch(bg)
        username.visitWidgets(screen::addRenderableWidget)
        val dropdown = createProfileDropdown(bg)
        dropdown.visitWidgets(screen::addRenderableWidget)

        loading.visible = false


        if (McClient.isDev /* || Config.devMode */) createDevRow(bg).visitWidgets(screen::addRenderableWidget)


        addRenderableOnly(
            Widgets.text(this.tabTitle)
                .withCenterAlignment()
                .withSize(this.uiWidth, 20)
                .withPosition(bg.x, bg.bottom + 2),
        )
    }

    private fun createDevRow(bg: DisplayWidget) = LayoutBuild.horizontal(5) {
        // Useful for hotswaps
        val refreshButton = Button()
            .withRenderer(WidgetRenderers.text(Text.of("Refresh Screen")))
            .withSize(60, 20)
            .withTexture(ExtraConstants.BUTTON_DARK)
            .withCallback { this@BasePvScreen.rebuildWidgets() }

        val hoverText = Text.join(
            "Screen: ${this@BasePvScreen.width}x${this@BasePvScreen.height}\n",
            "UI: ${uiWidth}x${uiHeight}\n",
            "BG: ${bg.width}x${bg.height}",
        )
        val screenSizeText = Button()
            .withRenderer(WidgetRenderers.text(Text.of("Screen Size")))
            .withSize(60, 20)
            .withTexture(ExtraConstants.BUTTON_DARK)
            .withTooltip(hoverText)

        val saveButton = Button()
            .withRenderer(WidgetRenderers.text(Text.of("Save Profiles")))
            .withSize(60, 20)
            .withTexture(ExtraConstants.BUTTON_DARK)
            .withCallback { saveProfiles() }


        widget(refreshButton)
        widget(screenSizeText)
        widget(saveButton)
    }

    private fun saveProfiles() {
        val dir = File(McClient.self.gameDirectory, "config/skyblockpv/")

        if (!dir.exists()) dir.mkdirs()

        val file = File(dir, "profiles-${gameProfile.id}.json")
        file.writeText(profiles.toString())
        Text.of("Profiles saved to .minecraft/config/skyblockpv/").send()
    }

    private fun createTabs() = LayoutBuild.vertical(2) {
        // as you can see, maya has no idea what she is doing
        PvTabs.entries.forEach { tab ->
            val button = Button()
            button.setSize(31, 20)
            if (tab.isSelected()) {
                button.withTexture(ExtraConstants.TAB_RIGHT_SELECTED)
            } else {
                button.withCallback { McClient.tell { McClient.setScreen(tab.create(gameProfile, profile)) } }
                button.withTexture(ExtraConstants.TAB_RIGHT)
            }
            // Don't bother actually aligning the icon yet, design will change anyway :3
            button.withRenderer(
                WidgetRenderers.padded(
                    0, 3 - if (tab.isSelected()) 1 else 0, 0, 9,
                    WidgetRenderers.center(16, 16) { gr, ctx, _ -> gr.renderItem(tab.getIcon(gameProfile), ctx.x, ctx.y) },
                ),
            )
            button.withTooltip(Text.translatable("skyblockpv.tab.${tab.name.lowercase()}"))
            widget(button)
        }
    }

    private fun createSearch(bg: DisplayWidget): LayoutElement {
        val width = 100

        val usernameState = State.of<String>(gameProfile.name)
        val username = Widgets.textInput(usernameState) { box ->
            box.withEnterCallback {
                Utils.fetchGameProfile(box.value) { profile ->
                    profile?.let {
                        McClient.tell {
                            McClient.setScreen(PvTabs.MAIN.create(it))
                        }
                    }
                }
            }
        }
        username.withPlaceholder("Username...")
        username.withSize(width, 20)
        username.setPosition(bg.x + bg.width - width, bg.y + bg.height)
        return username
    }

    private fun createProfileDropdown(bg: DisplayWidget): LayoutElement {
        val width = 100

        val dropdownState = DropdownState<SkyBlockProfile>.of<SkyBlockProfile>(profile)
        val dropdown = Widgets.dropdown(
            dropdownState,
            profiles,
            { profile ->
                Text.of(
                    profile.id.name + when (profile.profileType) {
                        ProfileType.NORMAL -> ""
                        ProfileType.BINGO -> " §9Ⓑ"
                        ProfileType.IRONMAN -> " ♻"
                        ProfileType.STRANDED -> " §a☀"
                        ProfileType.UNKNOWN -> " §c§ka"
                    },
                )
            },
            { button -> button.withSize(width, 20) },
            { builder ->
                builder.withCallback { profile ->
                    this.profile = profile
                    this.rebuildWidgets()
                }
                builder.withAlignment(OverlayAlignment.TOP_LEFT)
            },
        )
        dropdown.setPosition(bg.x, bg.y + bg.height)

        return dropdown
    }

    override fun renderBackground(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        this.renderBlurredBackground()
        this.renderTransparentBackground(guiGraphics)
    }
}
