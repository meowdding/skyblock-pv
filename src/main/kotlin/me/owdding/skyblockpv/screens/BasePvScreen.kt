package me.owdding.skyblockpv.screens

import com.google.gson.*
import com.mojang.authlib.GameProfile
import com.mojang.serialization.JsonOps
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
import me.owdding.lib.builder.LayoutFactory
import me.owdding.lib.displays.Alignment
import me.owdding.lib.displays.asWidget
import me.owdding.lib.layouts.setPos
import me.owdding.lib.platform.screens.MouseButtonEvent
import me.owdding.lib.platform.screens.mouseClicked
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.api.PlayerAPI
import me.owdding.skyblockpv.api.ProfileAPI
import me.owdding.skyblockpv.api.data.profile.EmptySkyBlockProfile
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.command.SkyBlockPlayerSuggestionProvider
import me.owdding.skyblockpv.screens.fullscreen.TestFullScreen
import me.owdding.skyblockpv.screens.windowed.elements.ExtraConstants
import me.owdding.skyblockpv.utils.ChatUtils
import me.owdding.skyblockpv.utils.ChatUtils.sendWithPrefix
import me.owdding.skyblockpv.utils.ExtraWidgetRenderers
import me.owdding.skyblockpv.utils.Utils
import me.owdding.skyblockpv.utils.Utils.multiLineDisplay
import me.owdding.skyblockpv.utils.Utils.unaryPlus
import me.owdding.skyblockpv.utils.components.PvLayouts
import me.owdding.skyblockpv.utils.displays.ExtraDisplays
import me.owdding.skyblockpv.utils.theme.PvColors
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.util.Util
import net.minecraft.client.gui.layouts.FrameLayout
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.client.gui.layouts.LayoutElement
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.profile.profile.ProfileType
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.helpers.McPlayer
import tech.thatgravyboat.skyblockapi.platform.id
import tech.thatgravyboat.skyblockapi.platform.name
import tech.thatgravyboat.skyblockapi.utils.text.CommonText
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.underlined
import java.lang.reflect.Type
import java.nio.file.Files
import java.util.concurrent.CompletableFuture

abstract class BasePvScreen(val name: String, val gameProfile: GameProfile, initProfile: SkyBlockProfile?) : BaseCursorScreen(CommonText.EMPTY) {

    val starttime = System.currentTimeMillis()
    var profiles: List<SkyBlockProfile> = emptyList()

    abstract val uiWidth: Int
    abstract val uiHeight: Int

    var initedWithProfile = false
    private var requireRebuild = false

    open val tabTitle: Component get() = +"tab.${name.lowercase()}"

    var profile: SkyBlockProfile = EmptySkyBlockProfile(gameProfile.id, EmptySkyBlockProfile.Reason.LOADING)

    init {
        PlayerAPI.getPlayer(gameProfile) { /*Load player data in the background*/ }
        ProfileAPI.getProfiles(gameProfile, "screen") { profiles ->
            this.profiles = profiles
            if (profiles.isEmpty()) {
                profile = EmptySkyBlockProfile(gameProfile.id, EmptySkyBlockProfile.Reason.NO_PROFILES)
                requireRebuild = true
                return@getProfiles
            }
            if (initedWithProfile) return@getProfiles
            val selected = initProfile ?: profiles.find { it.id.id == Utils.preferedProfileId } ?: profiles.find { it.selected } ?: return@getProfiles
            onProfileSwitch(selected)
            Utils.preferedProfileId = null
            profile = selected
            requireRebuild = true
            selected.dataFuture.whenComplete { _, throwable ->
                if (throwable != null) {
                    profile = EmptySkyBlockProfile(gameProfile.id, EmptySkyBlockProfile.Reason.ERROR, throwable)
                }
                requireRebuild = true
            }
        }
    }

    override fun tick() {
        super.tick()
        if (requireRebuild) safelyRebuild()
    }

    fun isProfileInitialized() = profile !is EmptySkyBlockProfile
    fun isProfileOfUser() = gameProfile.id == McPlayer.uuid

    protected fun safelyRebuild() {
        if (this.minecraft == null) return
        if (isProfileInitialized()) onProfileSwitch(profile)
        requireRebuild = false
        rebuildWidgets()
    }

    fun LayoutElement.applyLayout() = this.visitWidgets(::addRenderableWidget)
    fun Layout.applyLayout(x: Int, y: Int) = this.setPos(x, y).applyLayout()

    protected fun addLoader() {
        if (isProfileInitialized()) return

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

    protected fun createProfileDropdown(width: Int): LayoutElement {
        if (!isProfileInitialized()) {
            return Widgets.button()
                .withTexture(ExtraConstants.BUTTON_DARK)
                .withRenderer(WidgetRenderers.text(+"widgets.profile.loading"))
                .withSize(width, 20)
        }

        val dropdownState = DropdownState<SkyBlockProfile>.of(profile)
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
        }

        return dropdown
    }

    private var coopDropdownVisible = false
    protected open fun createSearch(x: Int, y: Int, bgWidth: Int): LayoutElement {
        var width = 100

        return LayoutFactory.horizontal {
            val coopDropdown = Button().apply {
                withSize(12, 20)
                withTexture(null)
                withRenderer(
                    WidgetRenderers.padded(
                        4, 0, 4, 0,
                        WidgetRenderers.icon<AbstractWidget>(UIIcons.CHEVRON_UP).withColor(MinecraftColors.WHITE),
                    ),
                )
                withTooltip(+"widgets.coop_search_tooltip")
                withCallback {
                    coopDropdownVisible = !coopDropdownVisible
                    safelyRebuild()
                }
            }

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


            val coopMemberDropdownState = DropdownState(null, State.of(profile.userId), true)
            val coopMemberDropdown = Widgets.dropdown(
                coopMemberDropdownState,
                profile.coopMembers.keys.toList(),
                { _ -> CommonComponents.EMPTY },
                { button ->
                    button.withSize(width, 20)
                    button.withRenderer(
                        WidgetRenderers.text<Button>(
                            Text.of {
                                color = PvColors.WHITE
                                append("◆ ")
                                append(gameProfile.name)
                            },
                        ).withPadding(4, 6),
                    )
                },
                { builder ->
                    builder.withCallback { coopProfile ->
                        val profile = profile.coopMembers[coopProfile]

                        if (profile == null) {
                            Text.of("Unknown member!") { color = TextColor.RED }.sendWithPrefix()
                            return@withCallback
                        }

                        val gameProfile = profile.getNow(null)
                        if (gameProfile != null) {
                            Utils.preferedProfileId = this@BasePvScreen.profile.id.id
                            McClient.setScreenAsync { PvTab.MAIN.create(gameProfile) }
                        } else if (profile.isCompletedExceptionally) {
                            Text.of("Failed to fetch username!").sendWithPrefix()
                        } else {
                            Text.of("Still fetching!").sendWithPrefix()
                        }

                    }

                    val loadingRenderer = WidgetRenderers.text<Button>(Text.of("Loading...") { this.color = TextColor.RED })
                        .withLeftAlignment()
                        .withPadding(0, 4)
                    val failedToLoad = WidgetRenderers.text<Button>(Text.of("Error!") { this.color = TextColor.RED })
                        .withLeftAlignment()
                        .withPadding(0, 4)
                    builder.withEntryRenderer { id ->
                        val future = profile.coopMembers[id] ?: return@withEntryRenderer WidgetRenderers.text<Button>(
                            Text.of("Unknown!") {
                                this.color = TextColor.DARK_RED
                            },
                        ).withLeftAlignment().withPadding(0, 4)
                        val widgetRenderer = future.thenApply { profile ->
                            if (profile == null) return@thenApply null

                            WidgetRenderers.text<Button>(
                                Text.of {
                                    color = PvColors.WHITE
                                    if (id == profile) {
                                        underlined = true
                                        append("◆ ")
                                    } else {
                                        append("◇ ")
                                    }
                                    append(profile.name.toString())
                                },
                            ).withLeftAlignment().withPadding(0, 4)
                        }

                        ExtraWidgetRenderers.supplied {
                            widgetRenderer.getNow(loadingRenderer) ?: failedToLoad
                        }
                    }
                    builder.withAlignment(OverlayAlignment.TOP_LEFT)
                },
            ).apply {
                withTexture(ExtraConstants.BUTTON_DARK)
            }

            if (profile.coopMembers.isNotEmpty()) {
                widget(coopDropdown)
                spacer(5)
                width += coopDropdown.width + 5
            }
            if (coopDropdownVisible) {
                widget(coopMemberDropdown)
                McClient.runNextTick {
                    coopMemberDropdown.mouseClicked(MouseButtonEvent(coopMemberDropdown.x + 1.0, coopMemberDropdown.y + 1.0, 1), false)
                }
                coopDropdownVisible = false
            } else widget(username)

        }.setPos(x + bgWidth - width, y)
    }

    protected fun saveProfiles() {
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

    open fun onProfileSwitch(profile: SkyBlockProfile) {}
    override fun isPauseScreen() = false
}
