package me.owdding.skyblockpv.screens.windowed

import com.mojang.authlib.GameProfile
import earth.terrarium.olympus.client.components.Widgets
import earth.terrarium.olympus.client.components.buttons.Button
import earth.terrarium.olympus.client.components.dropdown.DropdownState
import earth.terrarium.olympus.client.components.renderers.WidgetRenderers
import earth.terrarium.olympus.client.constants.MinecraftColors
import earth.terrarium.olympus.client.ui.OverlayAlignment
import earth.terrarium.olympus.client.ui.UIIcons
import me.owdding.lib.builder.LayoutBuilder
import me.owdding.lib.displays.Alignment
import me.owdding.lib.displays.DisplayWidget
import me.owdding.lib.displays.Displays
import me.owdding.lib.displays.asWidget
import me.owdding.lib.extensions.getStackTraceString
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.api.CachedApis
import me.owdding.skyblockpv.api.PlayerAPI
import me.owdding.skyblockpv.api.data.SocialEntry
import me.owdding.skyblockpv.api.data.profile.EmptySkyBlockProfile
import me.owdding.skyblockpv.api.data.profile.EmptySkyBlockProfile.Reason
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.config.Config
import me.owdding.skyblockpv.screens.BasePvScreen
import me.owdding.skyblockpv.screens.PvTab
import me.owdding.skyblockpv.screens.windowed.elements.ExtraConstants
import me.owdding.skyblockpv.screens.windowed.tabs.general.NetworthDisplay
import me.owdding.skyblockpv.utils.Utils
import me.owdding.skyblockpv.utils.Utils.asTranslated
import me.owdding.skyblockpv.utils.Utils.multiLineDisplay
import me.owdding.skyblockpv.utils.Utils.unaryPlus
import me.owdding.skyblockpv.utils.components.PvLayouts
import me.owdding.skyblockpv.utils.components.PvToast
import me.owdding.skyblockpv.utils.components.PvWidgets
import me.owdding.skyblockpv.utils.theme.ThemeSupport
import net.minecraft.Util
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.layouts.FrameLayout
import net.minecraft.client.gui.layouts.LayoutElement
import net.minecraft.util.TriState
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.platform.applyBackgroundBlur
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import tech.thatgravyboat.skyblockapi.utils.text.TextUtils.splitLines

private const val ASPECT_RATIO = 16.0 / 9.0

abstract class BaseWindowedPvScreen(name: String, gameProfile: GameProfile, profile: SkyBlockProfile?) : BasePvScreen(name, gameProfile, profile) {

    override val uiWidth get() = (uiHeight * ASPECT_RATIO).toInt()
    override val uiHeight get() = (this.height * 0.65).toInt()

    abstract fun create(bg: DisplayWidget)

    override fun init() {
        val bg = Displays.background(ThemeSupport.texture(SkyBlockPv.backgroundTexture), uiWidth, uiHeight).asWidget()

        FrameLayout.centerInRectangle(bg, 0, 0, this.width, this.height)
        bg.applyLayout()

        when (val profile = profile) {
            is EmptySkyBlockProfile if profile.reason == Reason.LOADING -> addLoader()
            is EmptySkyBlockProfile if profile.reason == Reason.NO_PROFILES -> addNoProfiles()
            is EmptySkyBlockProfile if profile.reason == Reason.ERROR -> addParsingError(profile.throwable!!)
        }
        createTopRow(bg).applyLayout(5, 5)

        if (!isProfileInitialized()) return
        initedWithProfile = true

        try {
            create(bg)
        } catch (e: Exception) {
            SkyBlockPv.error("Failed to display profile ${this::class.simpleName} from player ${gameProfile.name} (${gameProfile.id}) and profile ${profile.id.name}")

            val errorWidget = PvLayouts.vertical {
                val text = "widgets.error.stacktrace".asTranslated(
                    name,
                    gameProfile.name,
                    gameProfile.id,
                    profile.id.name,
                    e.javaClass.name,
                    e.message,
                    e.getStackTraceString(7),
                )


                text.splitLines().forEach {
                    widget(PvWidgets.text(it).withCenterAlignment().withSize(uiWidth, 10))
                }
            }
            FrameLayout.centerInRectangle(errorWidget, 0, 0, this.width, this.height)
            errorWidget.applyLayout()
        }

        createTabs().applyLayout(bg.x + 20, bg.y - 22)
        getSearch(bg).applyLayout()
        getProfileDropdown(bg).let {
            it.applyLayout()

            if (!Config.socials) return@let
            val button = createSocialDropdown()
            button.setPosition(it.x + it.width + 5, it.y)
            button.applyLayout()
        }

        addRenderableOnly(
            PvWidgets.text(this.tabTitle)
                .withCenterAlignment()
                .withSize(this.uiWidth, 20)
                .withPosition(bg.x, bg.bottom + 2),
        )
    }

    private fun addNoProfiles() {
        val errorWidget = PvLayouts.vertical(alignment = 0.5f) {
            display("widgets.error.no_profiles".asTranslated(gameProfile.name).multiLineDisplay(Alignment.CENTER))
        }

        FrameLayout.centerInRectangle(errorWidget, 0, 0, this.width, this.height)
        errorWidget.applyLayout()
    }

    private fun addParsingError(throwable: Throwable) {
        SkyBlockPv.error("Failed to parse profile!", throwable)

        val errorWidget = PvLayouts.vertical {
            val text = "widgets.error.parsing_error".asTranslated(
                name,
                gameProfile.name,
                gameProfile.id,
                throwable.javaClass.name,
                throwable.message,
                throwable.getStackTraceString(7),
            )

            text.splitLines().forEach {
                widget(PvWidgets.text(it).withCenterAlignment().withSize(uiWidth, 10))
            }
        }
        FrameLayout.centerInRectangle(errorWidget, 0, 0, this.width, this.height)
        errorWidget.applyLayout()
    }

    private fun createTopRow(bg: DisplayWidget) = PvLayouts.horizontal(5) {
        createUserRow()
        if (SkyBlockPv.isDevMode) createDevRow(bg)
    }

    private fun LayoutBuilder.createUserRow() = horizontal(5) {
        val settingsButton = Button()
            .withSize(20, 20)
            .withRenderer(WidgetRenderers.icon<AbstractWidget>(SkyBlockPv.olympusId("icons/edit")).withColor(MinecraftColors.WHITE))
            .withTexture(null)
            .withCallback { Utils.openConfig(this@BaseWindowedPvScreen) }
            .withTooltip(+"widgets.open_settings")

        val themeSwitcher = Widgets.button()
            .withRenderer(WidgetRenderers.icon<AbstractWidget>(UIIcons.EYE_DROPPER).withColor(MinecraftColors.WHITE))
            .withSize(20, 20)
            .withTexture(null)
            .withCallback {
                ThemeSupport.nextTheme()
                safelyRebuild()
                SkyBlockPv.config.save()
            }
            .withTooltip("widgets.theme_switcher".asTranslated(ThemeSupport.currentTheme.translation))

        widget(settingsButton)
        widget(themeSwitcher)
    }

    private fun LayoutBuilder.createDevRow(bg: DisplayWidget) = horizontal(5) {
        // Useful for hotswaps
        val refreshButton = Button()
            .withRenderer(WidgetRenderers.text(Text.of("Refresh Screen")))
            .withSize(60, 20)
            .withTexture(ExtraConstants.BUTTON_DARK)
            .withCallback { this@BaseWindowedPvScreen.safelyRebuild() }

        val hoverText = Text.multiline(
            "Screen: ${this@BaseWindowedPvScreen.width}x${this@BaseWindowedPvScreen.height}",
            "UI: ${uiWidth}x${uiHeight}",
            "BG: ${bg.width}x${bg.height}",
        )
        val screenSizeText = Button()
            .withRenderer(WidgetRenderers.text(Text.of("Screen Size")))
            .withSize(60, 20)
            .withTexture(ExtraConstants.BUTTON_DARK)
            .withCallback { McClient.self.keyboardHandler.clipboard = hoverText.stripped }
            .withTooltip(hoverText)

        val saveButton = Button()
            .withRenderer(WidgetRenderers.text(Text.of("Save Profiles")))
            .withSize(60, 20)
            .withTexture(ExtraConstants.BUTTON_DARK)
            .withCallback { saveProfiles() }

        val clearCache = Button()
            .withRenderer(WidgetRenderers.text(Text.of("Clear Cache")))
            .withSize(60, 20)
            .withTexture(ExtraConstants.BUTTON_DARK)
            .withCallback(CachedApis::clearCaches)

        val networthDebug = Button()
            .withRenderer(WidgetRenderers.text(Text.of("Networth")))
            .withSize(60, 20)
            .withTexture(ExtraConstants.BUTTON_DARK)
            .withCallback { McClient.clipboard = NetworthDisplay.networthDebug(profile).joinToString("\n") }


        widget(refreshButton)
        widget(screenSizeText)
        widget(saveButton)
        widget(clearCache)
        widget(networthDebug)
    }

    private fun createTabs() = PvLayouts.horizontal(2) {
        // as you can see, maya has no idea what she is doing
        PvTab.entries.forEach { tab ->
            if (tab.getTabState(profile) == TriState.FALSE) return@forEach
            if (!tab.canDisplay(profile)) return@forEach

            val button = Button()
            button.setSize(20, 31)
            button.withTexture(null)
            if (!tab.isSelected()) {
                button.withCallback { McClient.setScreenAsync { tab.create(gameProfile, profile) } }
            }
            button.withRenderer(
                WidgetRenderers.layered(
                    WidgetRenderers.sprite(if (tab.isSelected()) ExtraConstants.TAB_TOP_SELECTED else ExtraConstants.TAB_TOP),
                    WidgetRenderers.padded(
                        4 - (1.takeIf { tab.isSelected() } ?: 0), 0, 9, 0,
                        WidgetRenderers.center(16, 16) { gr, ctx, _ -> gr.renderItem(tab.getIcon(gameProfile), ctx.x, ctx.y) },
                    ),
                ),
            )
            button.withTooltip(+"tab.${tab.name.lowercase()}")
            widget(button)
        }
    }

    private fun getSearch(bg: DisplayWidget): LayoutElement = createSearch(100).apply {
        setPosition(bg.x + bg.width - this.width, bg.y + bg.height)
    }

    private fun getProfileDropdown(bg: DisplayWidget): LayoutElement = createProfileDropdown(100).apply {
        setPosition(bg.x, bg.y + bg.height)
    }

    private fun createSocialDropdown(): LayoutElement {
        val entries = listOf(
            SocialEntry("SkyCrypt", "https://sky.shiiyu.moe/stats/${gameProfile.name}/${profile.id.name}"),
            SocialEntry("EliteBot", "https://elitebot.dev/@${gameProfile.name}/${profile.id.name}"),
            *PlayerAPI.getCached(gameProfile.id)?.socials?.map { it.key.toEntry(it.value) }.orEmpty().toTypedArray(),
        )

        val button = Widgets.dropdown(
            DropdownState<String>.empty(),
            entries,
            { Text.of(it.name) },
            { button ->
                button.withSize(100, 20)
                button.withRenderer(WidgetRenderers.text(+"widgets.socials"))
            },
            { builder ->
                builder.withCallback {
                    if (it == null) return@withCallback
                    if (it.shouldCopy) {
                        McClient.clipboard = it.url
                        PvToast.addSocialsCopiedToast(it.url)
                    } else {
                        Util.getPlatform().openUri(it.url)
                    }
                }
                builder.withAlignment(OverlayAlignment.TOP_LEFT)
            },
        ).apply {
            withTexture(ExtraConstants.BUTTON_DARK)
        }

        return button
    }

    override fun renderBackground(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        if (ThemeSupport.currentTheme.backgroundBlur) {
            guiGraphics.applyBackgroundBlur()
            this.renderTransparentBackground(guiGraphics)
        } else {
            this.renderTransparentBackground(guiGraphics)
        }
    }
}
