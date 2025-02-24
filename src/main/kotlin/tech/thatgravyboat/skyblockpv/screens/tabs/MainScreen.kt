package tech.thatgravyboat.skyblockpv.screens.tabs

import com.mojang.authlib.GameProfile
import earth.terrarium.olympus.client.components.Widgets
import earth.terrarium.olympus.client.components.buttons.Button
import earth.terrarium.olympus.client.components.renderers.WidgetRenderers
import kotlinx.coroutines.runBlocking
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.client.gui.layouts.LinearLayout
import net.minecraft.client.gui.layouts.SpacerElement
import net.minecraft.resources.ResourceLocation
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockpv.SkyBlockPv
import tech.thatgravyboat.skyblockpv.api.StatusAPI
import tech.thatgravyboat.skyblockpv.api.data.PlayerStatus
import tech.thatgravyboat.skyblockpv.api.data.SkyblockProfile
import tech.thatgravyboat.skyblockpv.data.*
import tech.thatgravyboat.skyblockpv.screens.BasePvScreen
import tech.thatgravyboat.skyblockpv.screens.elements.ExtraConstants
import tech.thatgravyboat.skyblockpv.utils.FakePlayer
import tech.thatgravyboat.skyblockpv.utils.Utils.centerHorizontally
import tech.thatgravyboat.skyblockpv.utils.displays.*


class MainScreen(gameProfile: GameProfile, profile: SkyblockProfile? = null) : BasePvScreen("MAIN", gameProfile, profile) {

    private var cachedX = 0.0F
    private var cachedY = 0.0F

    override fun create(bg: DisplayWidget) {
        val middleColumnWidth = (uiWidth * 0.2).toInt()
        val sideColumnWidth = (uiWidth - middleColumnWidth) / 2

        val cols = LinearLayout.horizontal()

        cols.addChild(createLeftColumn(profile!!, sideColumnWidth))
        cols.addChild(createMiddleColumn(profiles, middleColumnWidth))
        cols.addChild(createRightColumn(profile!!, sideColumnWidth))

        cols.arrangeElements()
        cols.setPosition(bg.x, bg.y)
        cols.visitWidgets(this::addRenderableWidget)
    }

    private fun createLeftColumn(profile: SkyblockProfile, width: Int): Layout {
        val column = LinearLayout.vertical()
        column.addChild(SpacerElement.height(5))

        column.addChild(getTitleWidget("Info", width))

        val infoColumn = LinearLayout.vertical().spacing(2)
        infoColumn.addChild(SpacerElement.height(5))
        infoColumn.addChild(Widgets.text("Purse: ${profile.currency.purse.toFormattedString()}"))
        infoColumn.addChild(Widgets.text("Motes: ${profile.currency.motes.toFormattedString()}"))
        infoColumn.addChild(
            Widgets.text(
                buildString {
                    append("Bank: ")
                    val soloBank = profile.currency.soloBank.takeIf { it != 0L }?.toFormattedString()
                    val mainBank = profile.currency.mainBank.takeIf { it != 0L }?.toFormattedString()

                    if (soloBank != null && mainBank != null) append("$soloBank/$mainBank")
                    else append(soloBank ?: mainBank ?: "0")
                },
            ),
        )
        infoColumn.addChild(Widgets.text("Cookie Active: ${profile.currency.cookieBuffActive}"))
        infoColumn.addChild(SpacerElement.height(5))

        infoColumn.arrangeElements()

        column.addChild(getMainContentWidget(infoColumn, width).centerHorizontally(width))

        return column
    }

    private fun createMiddleColumn(profiles: List<SkyblockProfile>, width: Int): LinearLayout {
        val playerWidget = Displays.placeholder(width, width).asWidget().withRenderer { gr, ctx, _ ->
            val eyesX = (ctx.mouseX - ctx.x).toFloat().takeIf { ctx.mouseX >= 0 }?.also { cachedX = it } ?: cachedX
            val eyesY = (ctx.mouseY - ctx.y).toFloat().takeIf { ctx.mouseY >= 0 }?.also { cachedY = it } ?: cachedY
            Displays.entity(
                FakePlayer(gameProfile),
                width, width,
                width / 2,
                eyesX, eyesY,
            ).withBackground(0xD0000000u).render(gr, ctx.x, ctx.y)
        }

        val statusButtonWidget = Button()
        statusButtonWidget.withRenderer(WidgetRenderers.text(Text.of("§fLoad Status")))
        statusButtonWidget.setSize(width, 20)
        statusButtonWidget.withTexture(ExtraConstants.BUTTON_DARK)
        statusButtonWidget.withCallback {
            statusButtonWidget.withRenderer(WidgetRenderers.text(Text.of("§fLoading...")))
            runBlocking {
                val status = StatusAPI.getStatus(gameProfile.id)
                if (status == null) {
                    statusButtonWidget.withRenderer(WidgetRenderers.text(Text.of("§4ERROR")))
                    return@runBlocking
                }
                val statusText = when (status.status) {
                    PlayerStatus.Status.ONLINE -> "§aONLINE - "
                    PlayerStatus.Status.OFFLINE -> "§cOFFLINE - "
                    PlayerStatus.Status.ERROR -> "§4ERROR"
                }
                val locationText = status.location ?: "Unknown"
                statusButtonWidget.withRenderer(WidgetRenderers.text(Text.of(statusText + locationText)))
                statusButtonWidget.asDisabled()
            }
        }

        val layout = LinearLayout.vertical()
        layout.addChild(SpacerElement.height((uiHeight - playerWidget.height) / 2))
        layout.addChild(playerWidget)
        layout.addChild(SpacerElement.height(5))
        layout.addChild(statusButtonWidget)

        return layout
    }

    private fun createRightColumn(profile: SkyblockProfile, width: Int): Layout {
        val skillDisplayElementWidth = 30
        val skillElementsPerRow = width / skillDisplayElementWidth
        val column = LinearLayout.vertical()
        column.addChild(SpacerElement.height(5))

        fun <T> addSection(title: String, data: Sequence<Pair<String, T>>, getIcon: (String) -> ResourceLocation, getLevel: (String, T) -> Int) {
            column.addChild(getTitleWidget(title, width))

            val mainContent = LinearLayout.vertical().spacing(5)
            mainContent.addChild(SpacerElement.height(5))

            data.chunked(skillElementsPerRow).forEach { chunk ->
                val element = LinearLayout.horizontal().spacing(5)
                chunk.forEach { (name, data) ->
                    val level = getLevel(name, data)
                    val widget = listOf(
                        Displays.sprite(getIcon(name), 12, 12),
                        Displays.text("$level"),
                    ).toRow(1).asWidget().withTooltip(Text.of("$name: $level"))
                    element.addChild(widget)
                }
                mainContent.addChild(element.centerHorizontally(width))
            }
            mainContent.addChild(SpacerElement.height(5))

            mainContent.arrangeElements()

            column.addChild(getMainContentWidget(mainContent, width))
        }

        addSection<Long>("Skills", profile.skill.asSequence().map { it.toPair() }, ::getIconFromSkillName, ::getSkillLevel)
        column.addChild(Widgets.text(""))
        addSection<SlayerTypeData>("Slayer", profile.slayer.asSequence().map { it.toPair() }, ::getIconFromSlayerName) { name, data ->
            getSlayerLevel(name, data.exp)
        }

        return column
    }

    private fun getTitleWidget(title: String, width: Int) = Widgets.frame { compoundWidget ->
        compoundWidget.withContents { contents ->
            contents.addChild(Displays.background(SkyBlockPv.id("box/title"), width - 10, 20).asWidget())
            contents.addChild(Widgets.text(title).centerHorizontally(width))
        }
        compoundWidget.withStretchToContentSize()
    }

    private fun getMainContentWidget(content: Layout, width: Int) = Widgets.frame { compoundWidget ->
        compoundWidget.withContents { contents ->
            contents.addChild(Displays.background(SkyBlockPv.id("box/box"), width - 10, content.height).asWidget())
            contents.addChild(content)
        }
        compoundWidget.withStretchToContentSize()
    }

}
