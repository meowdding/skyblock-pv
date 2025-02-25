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
import kotlinx.coroutines.runBlocking
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.layouts.FrameLayout
import net.minecraft.client.gui.layouts.LinearLayout
import tech.thatgravyboat.skyblockapi.api.profile.profile.ProfileType
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.text.CommonText
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockpv.api.MojangAPI
import tech.thatgravyboat.skyblockpv.api.ProfileAPI
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.utils.displays.DisplayWidget
import tech.thatgravyboat.skyblockpv.utils.displays.Displays
import tech.thatgravyboat.skyblockpv.utils.displays.asWidget

private const val ASPECT_RATIO = 9.0 / 16.0

abstract class BasePvScreen(val name: String, val gameProfile: GameProfile, var profile: SkyBlockProfile? = null) : BaseCursorScreen(CommonText.EMPTY) {

    var profiles: List<SkyBlockProfile> = emptyList()

    val uiWidth get() = (this.width * 0.75).toInt()
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

        val tabs = LinearLayout.vertical().spacing(2)

        // as you can see, maya has no idea what she is doing
        PvTabs.entries.forEach { tab ->
            val button = Button()
            button.setSize(20, 20)
            if (tab.name == name) {
                button.withTexture(UIConstants.PRIMARY_BUTTON)
            } else {
                button.withCallback { McClient.tell { McClient.setScreen(tab.create(gameProfile, profile)) } }
                button.withTexture(UIConstants.BUTTON)
            }
            // Don't bother actually aligning the icon yet, design will change anyway :3
            button.withRenderer(WidgetRenderers.center(16, 16) { gr, ctx, _ -> gr.renderItem(tab.icon, ctx.x, ctx.y) })
            button.withTooltip(Text.of(tab.name))
            tabs.addChild(button)
        }

        tabs.arrangeElements()
        tabs.setPosition(bg.x + bg.width, bg.y + 5)

        bg.visitWidgets(screen::addRenderableOnly)
        tabs.visitWidgets(screen::addRenderableWidget)
        loading.visitWidgets(screen::addRenderableOnly)

        if (profile == null) return
        initedWithProfile = true
        create(bg)

        val profileWidth = 100

        val usernameState = State.of<String>(gameProfile.name)
        val username = Widgets.textInput(usernameState) { box ->
            box.withEnterCallback {
                runBlocking {
                    // TODO: dedupe code with command
                    val uuid = MojangAPI.getUUID(box.value)
                    if (uuid != null) {
                        McClient.tell {
                            McClient.setScreen(PvTabs.MAIN.create(McClient.self.minecraftSessionService.fetchProfile(uuid, false)?.profile ?: return@tell))
                        }
                    }
                }
            }
        }
        username.withPlaceholder("Username...")
        username.withSize(profileWidth, 20)
        username.setPosition(bg.x + bg.width - profileWidth, bg.y + bg.height)
        username.visitWidgets(screen::addRenderableWidget)

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
            { button -> button.withSize(profileWidth, 20) },
            { builder ->
                builder.withCallback { profile ->
                    this.profile = profile
                    this.rebuildWidgets()
                }
                builder.withAlignment(OverlayAlignment.TOP_LEFT)
            },
        )
        dropdown.setPosition(bg.x, bg.y + bg.height)
        dropdown.visitWidgets(screen::addRenderableWidget)

        loading.visible = false
    }

    override fun renderBackground(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        this.renderBlurredBackground()
        this.renderTransparentBackground(guiGraphics)
    }
}
