package me.owdding.skyblockpv.screens.fullscreen

import com.mojang.authlib.GameProfile
import earth.terrarium.olympus.client.components.Widgets
import earth.terrarium.olympus.client.components.buttons.Button
import earth.terrarium.olympus.client.components.dropdown.DropdownState
import earth.terrarium.olympus.client.components.renderers.WidgetRenderers
import earth.terrarium.olympus.client.constants.MinecraftColors
import earth.terrarium.olympus.client.ui.UIIcons
import earth.terrarium.olympus.client.utils.State
import me.owdding.lib.builder.FrameLayoutBuilder
import me.owdding.lib.builder.LayoutFactory
import me.owdding.lib.displays.Alignment
import me.owdding.lib.displays.Displays
import me.owdding.lib.displays.asWidget
import me.owdding.lib.displays.withPadding
import me.owdding.lib.extensions.getStackTraceString
import me.owdding.lib.layouts.setPos
import me.owdding.lib.layouts.withPadding
import me.owdding.lib.platform.screens.MouseButtonEvent
import me.owdding.lib.platform.screens.mouseClicked
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.api.CachedApis
import me.owdding.skyblockpv.api.data.profile.EmptySkyBlockProfile
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.command.SkyBlockPlayerSuggestionProvider
import me.owdding.skyblockpv.config.Config
import me.owdding.skyblockpv.config.DevConfig
import me.owdding.skyblockpv.screens.BasePvScreen
import me.owdding.skyblockpv.screens.PvTab
import me.owdding.skyblockpv.screens.fullscreen.tabs.main.MainTab
import me.owdding.skyblockpv.screens.windowed.elements.ExtraConstants
import me.owdding.skyblockpv.screens.windowed.tabs.general.NetworthDisplay
import me.owdding.skyblockpv.utils.Utils
import me.owdding.skyblockpv.utils.Utils.asTranslated
import me.owdding.skyblockpv.utils.Utils.unaryPlus
import me.owdding.skyblockpv.utils.components.PvLayouts
import me.owdding.skyblockpv.utils.components.PvWidgets
import me.owdding.skyblockpv.utils.components.PvWidgets.centerIn
import me.owdding.skyblockpv.utils.components.PvWidgets.getPlayerWidget
import me.owdding.skyblockpv.utils.components.PvWidgets.getStatusButton
import me.owdding.skyblockpv.utils.displays.ExtraDisplays
import me.owdding.skyblockpv.utils.theme.ThemeSupport
import me.owdding.skyblockpv.widgets.PronounWidget
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.Renderable
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.gui.layouts.LayoutElement
import net.minecraft.client.gui.narration.NarratableEntry
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.Text.asComponent
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import tech.thatgravyboat.skyblockapi.utils.text.TextUtils.splitLines

class BaseFullScreenPvScreen(gameProfile: GameProfile, profile: SkyBlockProfile?) : BasePvScreen("", gameProfile, profile) {

    override val uiWidth: Int get() = McClient.window.guiScaledWidth
    override val uiHeight: Int get() = McClient.window.guiScaledHeight
    private var currentTab: FullScreenTab = MainTab
        set(value) {
            field = value
            safelyRebuild()
        }

    override fun init() {
        val leftSidePadding = 5
        val leftSideWidth = (uiWidth * 0.2).toInt()
        val leftSideWidthInnerWidth = leftSideWidth - leftSidePadding * 2 - 2
        val rightSideWidth = 25
        val topBarHeight = 20

        val backgroundWidget = Displays.background(
            ThemeSupport.texture(SkyBlockPv.backgroundTexture),
            uiWidth - leftSideWidth - rightSideWidth,
            uiHeight - topBarHeight - 2,
        ).asWidget()

        LayoutFactory.horizontal {
            LayoutFactory.frame(leftSideWidthInnerWidth, uiHeight) {
                widget(
                    ExtraDisplays.wrappedText(
                        Text.of("SkyBlockPv v${SkyBlockPv.buildInfo.version}"),
                        textAlignment = Alignment.CENTER,
                        maxWidth = leftSideWidthInnerWidth,
                    ).withPadding(0, top = 15).asWidget(),
                ) {
                    alignVerticallyTop()
                    alignHorizontallyCenter()
                }
                LayoutFactory.vertical(3) {
                    widget(getPlayerWidget(leftSideWidthInnerWidth))
                    widget(getStatusButton(leftSideWidthInnerWidth))
                    if (Config.showPronouns) {
                        widget(PronounWidget.getPronounDisplay(gameProfile.id, leftSideWidthInnerWidth).asWidget())
                    }
                }.add {
                    alignVertically(0.4f)
                }
                val search = createSearch(leftSideWidthInnerWidth).withPadding(leftSidePadding)
                widget(createProfileDropdown(leftSideWidthInnerWidth).withPadding(leftSidePadding)) {
                    alignVerticallyBottom()
                    paddingBottom(search.height)
                }
                widget(search) {
                    alignVerticallyBottom()
                }
            }.add()
            display(Displays.background(0xFF202020u, Displays.empty(2, uiHeight)))

            LayoutFactory.frame(height = uiHeight) {
                widget(backgroundWidget) {
                    alignVerticallyBottom()
                }
                LayoutFactory.vertical {
                    spacer(height = topBarHeight)
                    display(Displays.background(0xFF303030u, Displays.empty(uiWidth - leftSideWidth - rightSideWidth, 2)))
                }.add {
                    alignVerticallyTop()
                }
                LayoutFactory.frame(uiWidth - leftSideWidth - rightSideWidth, topBarHeight) {

                    PvLayouts.horizontal {
                        FullScreenTabs.entries.forEach {
                            val selected = currentTab == it.tab
                            val button = Button()
                            button.setSize(20, 31)
                            button.withTexture(null)
                            if (!selected) {
                                button.withCallback {
                                    currentTab = it.tab
                                }
                            }
                            button.withRenderer(
                                WidgetRenderers.layered(
                                    WidgetRenderers.sprite(if (selected) ExtraConstants.TAB_TOP_SELECTED else ExtraConstants.TAB_TOP),
                                    WidgetRenderers.padded(
                                        4 - (1.takeIf { selected } ?: 0), 0, 9, 0,
                                        WidgetRenderers.center(16, 16) { gr, ctx, _ ->
                                            gr.renderItem(it.item, ctx.x, ctx.y)
                                        },
                                    ),
                                ),
                            )
                            button.withTooltip(it.displayName)
                            widget(button) {
                                alignVerticallyTop()
                            }
                        }
                    }.add {
                        alignHorizontallyCenter()
                    }
                }.add {
                    alignVerticallyTop()
                }
            }.add()


            display(Displays.background(0xFF202020u, Displays.empty(2, uiHeight)))
            PvLayouts.frame(height = uiHeight) {
                // screen size maybe?
                // networth debug
                PvLayouts.vertical(3) {
                    button(UIIcons.COLOR_PICKER, "widgets.theme_switcher".asTranslated(ThemeSupport.currentTheme.translation)) {
                        ThemeSupport.nextTheme()
                        safelyRebuild()
                        SkyBlockPv.config.save()
                    }.add()
                    button(UIIcons.PENCIL, +"widgets.open_settings") {
                        Utils.openConfig(this@BaseFullScreenPvScreen)
                    }.add()
                }.add {
                    alignVerticallyTop()
                }

                if (DevConfig.devMode) {
                    PvLayouts.vertical(3) {
                        button(UIIcons.CHAIN, "Refresh Screen") { safelyRebuild() }.add() // better icon
                        button(UIIcons.SAVE, "Save Profiles") { saveProfiles() }.add()
                        button(UIIcons.USER_X, "Clear Cache", CachedApis::clearCaches).add() // probably better icon..?
                        button(UIIcons.TAG, "Networth") {
                            McClient.clipboard = NetworthDisplay.networthDebug(profile).joinToString("\n")
                        }.add() // probably better icon..?
                    }.add {
                        alignVerticallyBottom()
                    }
                }
            }.add()
        }.applyLayout()

        try {
            when (val profile = this.profile) {
                is EmptySkyBlockProfile -> when (profile.reason) {
                    EmptySkyBlockProfile.Reason.LOADING -> addLoader(
                        backgroundWidget.x + 5,
                        backgroundWidget.y + 5,
                        backgroundWidget.width - 10,
                        backgroundWidget.height - 10,
                    )

                    EmptySkyBlockProfile.Reason.NO_PROFILES -> addNoProfiles(
                        backgroundWidget.x + 5,
                        backgroundWidget.y + 5,
                        backgroundWidget.width - 10,
                        backgroundWidget.height - 10,
                    )

                    EmptySkyBlockProfile.Reason.ERROR -> addParsingError(
                        profile.throwable!!,
                        backgroundWidget.x + 5,
                        backgroundWidget.y + 5,
                        backgroundWidget.width - 10,
                        backgroundWidget.height - 10,
                    )
                }

                else -> {
                    context(profile) {
                        currentTab.create(backgroundWidget.x + 5, backgroundWidget.y + 5, backgroundWidget.width - 10, backgroundWidget.height - 10)
                    }
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()

            PvLayouts.vertical {
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
            }.centerIn(backgroundWidget).applyLayout()
        }

    }

    var coopMemberDropdown: AbstractWidget? = null
    private fun createSearch(width: Int): LayoutElement {
        var width = width
        return LayoutFactory.horizontal {
            val coopDropdown = createCoopDropdownTrigger()
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

            if (profile.coopMembers.isNotEmpty()) {
                widget(coopDropdown)
                spacer(5)
                width -= coopDropdown.width + 5
            }

            username.withSize(width, 20)
            username.withAlwaysShow(true)
            username.withSuggestions { SkyBlockPlayerSuggestionProvider.getSuggestions(it) }
            username.withPlaceholder((+"widgets.username_input").stripped)


            val coopMemberDropdownState = DropdownState(null, State.of(profile.userId), true)
            val coopMemberDropdown = createCoopDropdown(width, coopMemberDropdownState)

            this@BaseFullScreenPvScreen.coopMemberDropdown = coopMemberDropdown
            widget(if (coopDropdownVisible) coopMemberDropdown else username)
        }.setPos(10, 10)
    }

    fun openDropdownIfNeeded() {
        val coopMemberDropdown = coopMemberDropdown ?: return
        if (coopDropdownVisible) {
            McClient.runNextTick {
                coopMemberDropdown.mouseClicked(MouseButtonEvent(coopMemberDropdown.x + 1.0, coopMemberDropdown.y + 1.0, 1), false)
            }
            coopDropdownVisible = false
        }
    }

    override fun tick() {
        super.tick()
        openDropdownIfNeeded()
        if (currentTab.requiresRebuild()) safelyRebuild()
    }

    private fun button(icon: Identifier, tooltip: String? = null, callback: () -> Unit) = button(icon, tooltip?.asComponent(), callback)
    private fun button(icon: Identifier, tooltip: Component? = null, callback: () -> Unit): AbstractWidget = Widgets.button {
        it.withRenderer(
            WidgetRenderers.layered(
                WidgetRenderers.sprite(ExtraConstants.BUTTON_DARK),
                WidgetRenderers.icon<Button>(icon).withColor(MinecraftColors.WHITE).withPadding(2),
            ),
        )
        it.withTexture(null)
        it.withSize(22, 22)
        it.withCallback(callback)
        tooltip?.let { tooltip -> it.withTooltip(tooltip) }
    }

    public override fun <T> addRenderableWidget(widget: T?): T? where T : GuiEventListener?, T : Renderable?, T : NarratableEntry? {
        return super.addRenderableWidget(widget)
    }

    override fun openPlayer(gameProfile: GameProfile): Screen {
        return BaseFullScreenPvScreen(gameProfile, null)
    }
}
