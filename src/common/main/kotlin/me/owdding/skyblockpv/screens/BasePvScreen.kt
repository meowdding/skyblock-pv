package me.owdding.skyblockpv.screens

import com.google.gson.*
import com.mojang.authlib.GameProfile
import com.mojang.serialization.JsonOps
import com.teamresourceful.resourcefulconfig.api.client.ResourcefulConfigScreen
import com.teamresourceful.resourcefullib.client.screens.BaseCursorScreen
import earth.terrarium.olympus.client.components.Widgets
import earth.terrarium.olympus.client.components.buttons.Button
import earth.terrarium.olympus.client.components.dropdown.DropdownState
import earth.terrarium.olympus.client.components.renderers.WidgetRenderers
import earth.terrarium.olympus.client.constants.MinecraftColors
import earth.terrarium.olympus.client.ui.OverlayAlignment
import earth.terrarium.olympus.client.ui.UIIcons
import earth.terrarium.olympus.client.utils.State
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.owdding.lib.builder.LayoutBuilder
import me.owdding.lib.displays.Alignment
import me.owdding.lib.displays.DisplayWidget
import me.owdding.lib.displays.Displays
import me.owdding.lib.displays.asWidget
import me.owdding.lib.extensions.getStackTraceString
import me.owdding.lib.layouts.setPos
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.api.CachedApis
import me.owdding.skyblockpv.api.ProfileAPI
import me.owdding.skyblockpv.api.data.SkyBlockProfile
import me.owdding.skyblockpv.command.SkyBlockPlayerSuggestionProvider
import me.owdding.skyblockpv.screens.elements.ExtraConstants
import me.owdding.skyblockpv.utils.ChatUtils
import me.owdding.skyblockpv.utils.Utils
import me.owdding.skyblockpv.utils.Utils.asTranslated
import me.owdding.skyblockpv.utils.Utils.multiLineDisplay
import me.owdding.skyblockpv.utils.Utils.unaryPlus
import me.owdding.skyblockpv.utils.components.PvLayouts
import me.owdding.skyblockpv.utils.components.PvWidgets
import me.owdding.skyblockpv.utils.displays.ExtraDisplays
import me.owdding.skyblockpv.utils.theme.PvColors
import me.owdding.skyblockpv.utils.theme.ThemeSupport
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.Util
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.layouts.FrameLayout
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.client.gui.layouts.LayoutElement
import net.minecraft.network.chat.Component
import net.minecraft.util.TriState
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.profile.profile.ProfileType
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McPlayer
import tech.thatgravyboat.skyblockapi.platform.applyBackgroundBlur
import tech.thatgravyboat.skyblockapi.utils.Scheduling
import tech.thatgravyboat.skyblockapi.utils.text.CommonText
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.underlined
import tech.thatgravyboat.skyblockapi.utils.text.TextUtils.splitLines
import java.lang.reflect.Type
import java.nio.file.Files
import java.util.concurrent.CompletableFuture
import kotlin.time.Duration.Companion.seconds

private const val ASPECT_RATIO = 16.0 / 9.0

abstract class BasePvScreen(val name: String, val gameProfile: GameProfile, profile: SkyBlockProfile?) : BaseCursorScreen(CommonText.EMPTY) {

    val starttime = System.currentTimeMillis()
    var profiles: List<SkyBlockProfile> = emptyList()

    val uiWidth get() = (uiHeight * ASPECT_RATIO).toInt()
    val uiHeight get() = (this.height * 0.65).toInt()

    var initedWithProfile = false

    open val tabTitle: Component get() = +"tab.${name.lowercase()}"

    lateinit var profile: SkyBlockProfile

    init {
        CoroutineScope(Dispatchers.IO).launch {
            profiles = ProfileAPI.getProfiles(gameProfile)
            (profile ?: profiles.find { it.selected })?.let {
                onProfileSwitch(it)
                this@BasePvScreen.profile = it
            }
            if (!initedWithProfile) {
                McClient.runNextTick { safelyRebuild() }
            }
        }

        Scheduling.schedule(10.seconds) {
            if (profile == null) {
                McClient.runNextTick { safelyRebuild() }
            }
        }
    }

    fun isProfileOfUser() = gameProfile.id == McPlayer.uuid

    abstract fun create(bg: DisplayWidget)

    protected fun safelyRebuild() {
        if (this.minecraft == null) return
        rebuildWidgets()
    }

    fun LayoutElement.applyLayout() = this.visitWidgets(::addRenderableWidget)
    fun Layout.applyLayout(x: Int, y: Int) = this.setPos(x, y).applyLayout()

    override fun init() {
        val bg = Displays.background(ThemeSupport.texture(SkyBlockPv.backgroundTexture), uiWidth, uiHeight).asWidget()

        FrameLayout.centerInRectangle(bg, 0, 0, this.width, this.height)
        bg.applyLayout()

        addLoader()
        createTopRow(bg).applyLayout(5, 5)

        if (!this::profile.isInitialized) return
        initedWithProfile = true

        try {
            create(bg)
        } catch (e: Exception) {
            e.printStackTrace()

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
        createSearch(bg).applyLayout()
        createProfileDropdown(bg).applyLayout()

        addRenderableOnly(
            PvWidgets.text(this.tabTitle)
                .withCenterAlignment()
                .withSize(this.uiWidth, 20)
                .withPosition(bg.x, bg.bottom + 2),
        )
    }

    override fun isPauseScreen() = false

    private fun addLoader() {
        if (this::profile.isInitialized) return

        val loading = ExtraDisplays.loading().asWidget()
        FrameLayout.centerInRectangle(loading, 0, 0, this.width, this.height)

        if (starttime + 8000 > System.currentTimeMillis()) return loading.visitWidgets(this::addRenderableOnly)

        val errorWidget = PvLayouts.vertical(alignment = 0.5f) {
            widget(loading)
            spacer(height = 20)

            display((+"widgets.error.loading").multiLineDisplay(Alignment.CENTER))

            spacer(height = 20)

            widget(
                Widgets.button()
                    .withRenderer(WidgetRenderers.text(+"widgets.open_logs"))
                    .withSize(100, 20)
                    .withCallback {
                        Util.getPlatform().openPath(FabricLoader.getInstance().gameDir.resolve("logs"))
                    },
            )
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
            .withCallback { McClient.setScreenAsync { ResourcefulConfigScreen.getFactory(SkyBlockPv.MOD_ID).apply(this@BasePvScreen) } }
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
            .withCallback { this@BasePvScreen.safelyRebuild() }

        val hoverText = Text.multiline(
            "Screen: ${this@BasePvScreen.width}x${this@BasePvScreen.height}",
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


        widget(refreshButton)
        widget(screenSizeText)
        widget(saveButton)
        widget(clearCache)
    }

    private fun saveProfiles() {
        val file = SkyBlockPv.configDir.resolve("profiles-${gameProfile.id}.json")

        Files.createDirectories(file.parent)
        Files.writeString(
            file,
            GsonBuilder()
                .registerTypeAdapter(
                    ItemStack::class.java,
                    object : JsonSerializer<ItemStack> {
                        override fun serialize(src: ItemStack?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
                            if (src == null) {
                                return JsonNull.INSTANCE
                            }

                            val encodeStart = ItemStack.CODEC.encodeStart(JsonOps.INSTANCE, src)
                            if (encodeStart.isError) {
                                return JsonPrimitive(encodeStart.error().get().messageSupplier().get())
                            }
                            return encodeStart.getOrThrow()
                        }
                    },
                )
                .registerTypeAdapter(
                    CompletableFuture::class.java,
                    object : JsonSerializer<CompletableFuture<*>> {
                        override fun serialize(src: CompletableFuture<*>?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
                            if (src == null) {
                                return JsonNull.INSTANCE
                            }

                            return JsonPrimitive("Future: ${src.get()}")
                        }
                    },
                )
                .create().toJson(profiles),
        )

        ChatUtils.chat("Profiles saved to .minecraft/config/skyblockpv/")
    }

    private fun createTabs() = PvLayouts.horizontal(2) {
        // as you can see, maya has no idea what she is doing
        PvTab.entries.forEach { tab ->
            if (tab.getTabState(profile) == TriState.FALSE) return@forEach

            val button = Button()
            button.setSize(20, 31)
            button.withTexture(null)
            if (!tab.isSelected()) {
                button.withCallback { McClient.setScreenAsync { tab.create(gameProfile, profile) } }
            }
            // Don't bother actually aligning the icon yet, design will change anyway :3
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

    private fun createSearch(bg: DisplayWidget): LayoutElement {
        val width = 100

        val usernameState = State.of(gameProfile.name)
        val username = Widgets.autocomplete<String>(usernameState) { box ->
            box.withEnterCallback {
                Utils.fetchGameProfile(box.value) { profile ->
                    profile?.let {
                        McClient.setScreenAsync { PvTab.MAIN.create(it) }
                    }
                }
            }
            box.withTexture(ExtraConstants.TEXTBOX)
        }
        username.withAlwaysShow(true)
        username.withSuggestions { SkyBlockPlayerSuggestionProvider.getSuggestions(it) }
        username.withPlaceholder((+"widgets.username_input").stripped)
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
                Text.of {
                    color = PvColors.WHITE
                    if (profile.selected) {
                        underlined = true
                        append("◆ ")
                    } else {
                        append("◇ ")
                    }
                    append(profile.id.name)
                    append(
                        when (profile.profileType) {
                            ProfileType.NORMAL -> ""
                            ProfileType.BINGO -> " §9Ⓑ"
                            ProfileType.IRONMAN -> " ♻"
                            ProfileType.STRANDED -> " §a☀"
                            ProfileType.UNKNOWN -> " §c§ka"
                        },
                    )
                }
            },
            { button -> button.withSize(width, 20) },
            { builder ->
                builder.withCallback { profile ->
                    this.profile = profile ?: return@withCallback
                    this.onProfileSwitch(profile)
                    this.safelyRebuild()
                }
                builder.withAlignment(OverlayAlignment.TOP_LEFT)
            },
        ).apply {
            withTexture(ExtraConstants.BUTTON_DARK)
            setPosition(bg.x, bg.y + bg.height)
        }

        return dropdown
    }

    open fun onProfileSwitch(profile: SkyBlockProfile) {}

    override fun renderBackground(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        if (ThemeSupport.currentTheme.backgroundBlur) {
            guiGraphics.applyBackgroundBlur()
            this.renderTransparentBackground(guiGraphics)
        } else {
            this.renderTransparentBackground(guiGraphics)
        }
    }
}
