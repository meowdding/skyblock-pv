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
import tech.thatgravyboat.skyblockapi.api.profile.profile.ProfileType
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McScreen
import tech.thatgravyboat.skyblockapi.utils.text.CommonText
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockpv.api.ProfileAPI
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.screens.elements.ExtraConstants
import tech.thatgravyboat.skyblockpv.utils.LayoutBuild
import tech.thatgravyboat.skyblockpv.utils.Utils
import tech.thatgravyboat.skyblockpv.utils.displays.DisplayWidget
import tech.thatgravyboat.skyblockpv.utils.displays.Displays
import tech.thatgravyboat.skyblockpv.utils.displays.asWidget
import kotlin.reflect.full.isSubclassOf

private const val ASPECT_RATIO = 9.0 / 16.0

abstract class BasePvScreen(val name: String, val gameProfile: GameProfile, var profile: SkyBlockProfile? = null) : BaseCursorScreen(CommonText.EMPTY) {

    var profiles: List<SkyBlockProfile> = emptyList()

    val uiWidth get() = (this.width * 0.65).toInt()
    val uiHeight get() = (uiWidth * ASPECT_RATIO).toInt()

    var initedWithProfile = false

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
        val loading = Widgets.text("Loading...")

        FrameLayout.centerInRectangle(bg, 0, 0, screen.width, screen.height)

        loading.withCenterAlignment()
        FrameLayout.centerInRectangle(loading, 0, 0, screen.width, screen.height)

        val tabs = createTabs()
        tabs.setPosition(bg.x + bg.width, bg.y + 5)

        bg.visitWidgets(screen::addRenderableOnly)
        tabs.visitWidgets(screen::addRenderableWidget)
        loading.visitWidgets(screen::addRenderableOnly)

        if (profile == null) return
        initedWithProfile = true
        create(bg)

        val username = createSearch(bg)
        username.visitWidgets(screen::addRenderableWidget)
        val dropdown = createProfileDropdown(bg)
        dropdown.visitWidgets(screen::addRenderableWidget)

        loading.visible = false


        val refreshButton = Button()
            .withRenderer(WidgetRenderers.text(Text.of("Refresh Screen")))
        refreshButton.withSize(40, 20)
        refreshButton.withTexture(ExtraConstants.BUTTON_DARK)
        refreshButton.setPosition(0, 0)
        refreshButton.withCallback { this.rebuildWidgets() }

        addRenderableWidget(refreshButton)


        addRenderableOnly(
            Widgets.text(Text.join("${gameProfile.name} (${profile?.id?.name}) - ", Text.translatable("skyblockpv.tab.${name.lowercase()}")))
                .withCenterAlignment()
                .withSize(this.uiWidth, 20)
                .withPosition(bg.x, 5),
        )
    }

    private fun createTabs() = LayoutBuild.vertical(2) {
        // as you can see, maya has no idea what she is doing
        PvTabs.entries.forEach { tab ->
            val selected = tab.screen.isSubclassOf(McScreen.self!!::class)
            val button = Button()
            button.setSize(22, 20)
            if (selected) {
                button.withTexture(ExtraConstants.TAB_RIGHT_SELECTED)
            } else {
                button.withCallback { McClient.tell { McClient.setScreen(tab.create(gameProfile, profile)) } }
                button.withTexture(ExtraConstants.TAB_RIGHT)
            }
            // Don't bother actually aligning the icon yet, design will change anyway :3
            button.withRenderer(
                WidgetRenderers.padded(
                    0, 3 - if (selected) 1 else 0, 0, 0,
                    WidgetRenderers.center(16, 16) { gr, ctx, _ -> gr.renderItem(tab.icon, ctx.x, ctx.y) },
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
