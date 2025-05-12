package me.owdding.skyblockpv.screens

import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
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
import earth.terrarium.olympus.client.utils.State
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.owdding.lib.builder.LayoutBuilder
import me.owdding.lib.builder.LayoutBuilder.Companion.setPos
import me.owdding.lib.builder.LayoutFactory
import me.owdding.lib.displays.DisplayWidget
import me.owdding.lib.displays.Displays
import me.owdding.lib.displays.asWidget
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.api.GardenApi
import me.owdding.skyblockpv.api.ProfileAPI
import me.owdding.skyblockpv.api.data.SkyBlockProfile
import me.owdding.skyblockpv.screens.elements.ExtraConstants
import me.owdding.skyblockpv.utils.ChatUtils
import me.owdding.skyblockpv.utils.Utils
import me.owdding.skyblockpv.utils.displays.ExtraDisplays
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
import tech.thatgravyboat.skyblockapi.utils.Scheduling
import tech.thatgravyboat.skyblockapi.utils.text.CommonText
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.underlined
import java.lang.reflect.Type
import java.nio.file.Files
import kotlin.time.Duration.Companion.seconds

private const val ASPECT_RATIO = 16.0 / 9.0

abstract class BasePvScreen(val name: String, val gameProfile: GameProfile, profile: SkyBlockProfile?) : BaseCursorScreen(CommonText.EMPTY) {

    val starttime = System.currentTimeMillis()
    var profiles: List<SkyBlockProfile> = emptyList()

    val uiWidth get() = (uiHeight * ASPECT_RATIO).toInt()
    val uiHeight get() = (this.height * 0.6).toInt()

    var initedWithProfile = false

    open val tabTitle: Component get() = Text.translatable("skyblockpv.tab.${name.lowercase()}")

    lateinit var profile: SkyBlockProfile

    init {
        CoroutineScope(Dispatchers.IO).launch {
            profiles = ProfileAPI.getProfiles(gameProfile.id)
            (profile ?: profiles.find { it.selected })?.let {
                this@BasePvScreen.profile = it
            }
            if (!initedWithProfile) {
                McClient.tell { safelyRebuild() }
            }
        }

        Scheduling.schedule(10.seconds) {
            if (profile == null) {
                McClient.tell { safelyRebuild() }
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
        val bg = Displays.background(SkyBlockPv.backgroundTexture, uiWidth, uiHeight).asWidget()

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

            val errorWidget = LayoutFactory.vertical {
                val text = mutableListOf(
                    "Failed building screen $name",
                    "Report this in the discord",
                    "",
                    "User: ${gameProfile.name} - ${gameProfile.id}",
                    "Profile: ${profile.id.name}",
                    "",
                    "Exception: ${e.message}",
                    "Stacktrace:",
                )

                text += e.stackTrace.take(7).map { it.toString().replace("knot//me.owdding.skyblockpv", "PV") }

                text.forEach {
                    widget(Widgets.text(it).withCenterAlignment().withSize(uiWidth, 10))
                }
            }
            FrameLayout.centerInRectangle(errorWidget, 0, 0, this.width, this.height)
            errorWidget.applyLayout()
        }

        createTabs().applyLayout(bg.x + 20, bg.y - 22)
        createSearch(bg).applyLayout()
        createProfileDropdown(bg).applyLayout()

        addRenderableOnly(
            Widgets.text(this.tabTitle)
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

        val errorWidget = LayoutFactory.vertical(alignment = 0.5f) {
            widget(loading)
            spacer(height = 20)

            widget(Widgets.text("Is the API down?"))
            widget(Widgets.text("Did data parsing fail?"))
            widget(Widgets.text("Or is the API Key expired?"))
            widget(Widgets.text("Report this on the discord with your /logs/latest.log"))

            spacer(height = 20)

            widget(
                Widgets.button()
                    .withRenderer(WidgetRenderers.text(Text.of("Open Logs")))
                    .withSize(100, 20)
                    .withCallback {
                        Util.getPlatform().openPath(FabricLoader.getInstance().gameDir.resolve("logs"))
                    },
            )
        }

        FrameLayout.centerInRectangle(errorWidget, 0, 0, this.width, this.height)
        errorWidget.applyLayout()
    }

    private fun createTopRow(bg: DisplayWidget) = LayoutFactory.horizontal(5) {
        createUserRow()
        if (SkyBlockPv.isDevMode) createDevRow(bg)
    }

    private fun LayoutBuilder.createUserRow() = horizontal(5) {
        val settingsButton = Button()
            .withSize(20, 20)
            .withRenderer(WidgetRenderers.icon<AbstractWidget>(SkyBlockPv.olympusId("icons/edit")).withColor(MinecraftColors.WHITE))
            .withTexture(null)
            .withCallback { McClient.setScreenAsync(ResourcefulConfigScreen.getFactory("sbpv").apply(this@BasePvScreen)) }
            .withTooltip(Text.multiline("Open Settings.", "You can also use /sbpv"))

        widget(settingsButton)
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
            .withCallback {
                ProfileAPI.clearCache()
                GardenApi.clearCache()
            }


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
                                return com.google.gson.JsonNull.INSTANCE
                            }

                            val encodeStart = ItemStack.CODEC.encodeStart(JsonOps.INSTANCE, src)
                            if (encodeStart.isError) {
                                return com.google.gson.JsonPrimitive(encodeStart.error().get().messageSupplier.get())
                            }
                            return encodeStart.getOrThrow()
                        }
                    },
                ).create().toJson(profiles),
        )

        ChatUtils.chat("Profiles saved to .minecraft/config/skyblockpv/")
    }

    private fun createTabs() = LayoutFactory.horizontal(2) {
        // as you can see, maya has no idea what she is doing
        PvTab.entries.forEach { tab ->
            if (tab.getTabState(profile) == TriState.FALSE) return@forEach

            val button = Button()
            button.setSize(20, 31)
            if (tab.isSelected()) {
                button.withTexture(ExtraConstants.TAB_TOP_SELECTED)
            } else {
                button.withCallback { McClient.setScreenAsync(tab.create(gameProfile, profile)) }
                button.withTexture(ExtraConstants.TAB_TOP)
            }
            // Don't bother actually aligning the icon yet, design will change anyway :3
            button.withRenderer(
                WidgetRenderers.padded(
                    4 - (1.takeIf { tab.isSelected() } ?: 0), 0, 9, 0,
                    WidgetRenderers.center(16, 16) { gr, ctx, _ -> gr.renderItem(tab.getIcon(gameProfile), ctx.x, ctx.y) },
                ),
            )
            button.withTooltip(Text.translatable("skyblockpv.tab.${tab.name.lowercase()}"))
            widget(button)
        }
    }

    private fun createSearch(bg: DisplayWidget): LayoutElement {
        val width = 100

        val usernameState = State.of(gameProfile.name)
        val username = Widgets.textInput(usernameState) { box ->
            box.withEnterCallback {
                Utils.fetchGameProfile(box.value) { profile ->
                    profile?.let {
                        McClient.setScreenAsync(PvTab.MAIN.create(it))
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
                Text.of {
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
        )
        dropdown.setPosition(bg.x, bg.y + bg.height)

        return dropdown
    }

    open fun onProfileSwitch(profile: SkyBlockProfile) {}

    override fun renderBackground(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        this.renderBlurredBackground()
        this.renderTransparentBackground(guiGraphics)
    }
}
