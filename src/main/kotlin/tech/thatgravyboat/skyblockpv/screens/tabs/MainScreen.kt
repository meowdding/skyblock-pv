package tech.thatgravyboat.skyblockpv.screens.tabs

import com.mojang.authlib.GameProfile
import earth.terrarium.olympus.client.components.buttons.Button
import earth.terrarium.olympus.client.components.renderers.WidgetRenderers
import kotlinx.coroutines.runBlocking
import net.minecraft.client.gui.layouts.LinearLayout
import net.minecraft.client.gui.layouts.SpacerElement
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockpv.SkyBlockPv
import tech.thatgravyboat.skyblockpv.api.PronounsDbAPI
import tech.thatgravyboat.skyblockpv.api.SkillAPI
import tech.thatgravyboat.skyblockpv.api.SkillAPI.getIconFromSkillName
import tech.thatgravyboat.skyblockpv.api.SkillAPI.getSkillLevel
import tech.thatgravyboat.skyblockpv.api.StatusAPI
import tech.thatgravyboat.skyblockpv.api.data.PlayerStatus
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.data.SlayerTypeData
import tech.thatgravyboat.skyblockpv.data.getIconFromSlayerName
import tech.thatgravyboat.skyblockpv.data.getSlayerLevel
import tech.thatgravyboat.skyblockpv.screens.BasePvScreen
import tech.thatgravyboat.skyblockpv.screens.elements.ExtraConstants
import tech.thatgravyboat.skyblockpv.utils.FakePlayer
import tech.thatgravyboat.skyblockpv.utils.LayoutBuild
import tech.thatgravyboat.skyblockpv.utils.Utils.centerHorizontally
import tech.thatgravyboat.skyblockpv.utils.Utils.getMainContentWidget
import tech.thatgravyboat.skyblockpv.utils.Utils.getTitleWidget
import tech.thatgravyboat.skyblockpv.utils.Utils.pushPop
import tech.thatgravyboat.skyblockpv.utils.Utils.round
import tech.thatgravyboat.skyblockpv.utils.displays.*
import java.text.SimpleDateFormat


class MainScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BasePvScreen("MAIN", gameProfile, profile) {

    private var cachedX = 0.0F
    private var cachedY = 0.0F

    override fun create(bg: DisplayWidget) {
        val middleColumnWidth = (uiWidth * 0.2).toInt()
        val sideColumnWidth = (uiWidth - middleColumnWidth) / 2

        val cols = LayoutBuild.horizontal {
            widget(createLeftColumn(profile!!, sideColumnWidth))
            widget(createMiddleColumn(profile!!, middleColumnWidth))
            widget(createRightColumn(profile!!, sideColumnWidth))
        }

        cols.arrangeElements()
        cols.setPosition(bg.x, bg.y)
        cols.visitWidgets(this::addRenderableWidget)
    }

    private fun createLeftColumn(profile: SkyBlockProfile, width: Int) = LayoutBuild.vertical {
        spacer(height = 5)

        val irrelevantSkills = listOf(
            "SKILL_RUNECRAFTING",
            "SKILL_SOCIAL",
        )

        val skillAvg = profile.skill
            .filterNot { it.key in irrelevantSkills }
            .map { getSkillLevel(it.key, it.value) }
            .average()

        widget(getTitleWidget("Info", width))

        val infoColumn = LayoutBuild.vertical(2) {
            fun grayText(text: String) = Displays.text(text, color = { 0x555555u }, shadow = false)

            string("Purse: ${profile.currency.purse.toFormattedString()}")
            string("Motes: ${profile.currency.motes.toFormattedString()}")
            string(
                buildString {
                    append("Bank: ")
                    val soloBank = profile.currency.soloBank.takeIf { it != 0L }?.toFormattedString()
                    val mainBank = profile.currency.mainBank.takeIf { it != 0L }?.toFormattedString()

                    if (soloBank != null && mainBank != null) append("$soloBank/$mainBank")
                    else append(soloBank ?: mainBank ?: "0")
                },
            )
            string("Cookie Buff: ${"§aActive".takeIf { profile.currency.cookieBuffActive } ?: "§cInactive"}")
            display(
                grayText("SkyBlock Level: ${profile.skyBlockLevel.first}")
                    .withTooltip("Progress: ${profile.skyBlockLevel.second}/100"),
            )
            display(
                grayText("First Join: ${SimpleDateFormat("yyyy.MM.dd").format(profile.firstJoin)}")
                    .withTooltip(SimpleDateFormat("yyyy.MM.dd HH:mm").format(profile.firstJoin)),
            )
            display(
                grayText("Skill Avg: ${skillAvg.round()}")
                    .withTooltip("HypixelAPI doesn't provide your actual max Taming Level,", "so we just assumes that it's 60."),
            )

            display(
                listOf(
                    grayText("Pronouns: "),
                    PronounsDbAPI.getDisplay(gameProfile.id),
                ).toRow().withTooltip("Provided by https://pronoundb.org/"),
            )
            string("Fairy Souls: ${profile.fairySouls}")
        }

        widget(getMainContentWidget(infoColumn, width).centerHorizontally(width))
    }

    private fun createMiddleColumn(profile: SkyBlockProfile, width: Int): LinearLayout {
        val height = (width * 1.1).toInt()
        val playerWidget = Displays.background(SkyBlockPv.id("buttons/dark/disabled"), width, height).asWidget().withRenderer { gr, ctx, _ ->
            val eyesX = (ctx.mouseX - ctx.x).toFloat().takeIf { ctx.mouseX >= 0 }?.also { cachedX = it } ?: cachedX
            val eyesY = (ctx.mouseY - ctx.y).toFloat().takeIf { ctx.mouseY >= 0 }?.also { cachedY = it } ?: cachedY
            gr.pushPop {
                translate(0f, 0f, 100f)
                Displays.entity(
                    FakePlayer(gameProfile, Text.join("[", profile.skyBlockLevel.first.toString(), "] ", gameProfile.name)),
                    width, height,
                    (width / 2.5).toInt(),
                    eyesX, eyesY,
                ).render(gr, ctx.x, ctx.y + height / 10)
            }
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
                val locationText = SkyBlockIsland.entries.find { it.id == status.location }?.toString() ?: status.location ?: "Unknown"
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

    private fun createRightColumn(profile: SkyBlockProfile, width: Int) = LayoutBuild.vertical {
        spacer(height = 5)

        fun <T> addSection(
            title: String,
            data: Sequence<Pair<String, T>>,
            getToolTip: (String, T) -> Component?,
            getIcon: (String) -> ResourceLocation,
            getLevel: (String, T) -> Any,
        ) {
            widget(getTitleWidget(title, width))
            val mainContent = LinearLayout.vertical().spacing(5)

            val convertedElements = data.map { (name, data) ->
                val level = getLevel(name, data).toString()
                val widget = listOf(
                    Displays.sprite(getIcon(name), 12, 12),
                    Displays.text(level, color = { 0x555555u }, shadow = false),
                ).toRow(1).asWidget()
                getToolTip(name, data)?.let { widget.withTooltip(it) }
                widget
            }.toList()

            val elementsPerRow = width / (convertedElements.first().width + 10)
            if (elementsPerRow < 1) return

            convertedElements.chunked(elementsPerRow).forEach { chunk ->
                val element = LinearLayout.horizontal().spacing(5)
                chunk.forEach { element.addChild(it) }
                mainContent.addChild(element.centerHorizontally(width))
            }

            mainContent.arrangeElements()

            widget(getMainContentWidget(mainContent, width))
        }

        addSection<Long>(
            "Skills", profile.skill.asSequence().map { it.toPair() },
            { name, num ->
                SkillAPI.getProgressToNextLevel(name, num).let { progress ->
                    if (progress == 1f) Text.of("§cMaxed!")
                    else Text.of("§a${(progress * 100).round()}% to next level")
                }
            },
            ::getIconFromSkillName, ::getSkillLevel,
        )

        spacer(height = 10)

        addSection<SlayerTypeData>("Slayer", profile.slayer.asSequence().map { it.toPair() }, { a, b -> null }, ::getIconFromSlayerName) { name, data ->
            getSlayerLevel(name, data.exp)
        }

        spacer(height = 10)

        addSection<Long>(
            "Essence",
            profile.currency.essence.asSequence().map { it.toPair() },
            { a, b -> null },
            { SkyBlockPv.id("icon/essence/${it.lowercase()}") },
        ) { _, amount -> amount.toFormattedString() }
    }

}
