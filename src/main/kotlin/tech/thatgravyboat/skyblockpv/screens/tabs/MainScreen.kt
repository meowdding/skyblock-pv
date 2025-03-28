package tech.thatgravyboat.skyblockpv.screens.tabs

import com.mojang.authlib.GameProfile
import earth.terrarium.olympus.client.components.buttons.Button
import earth.terrarium.olympus.client.components.renderers.WidgetRenderers
import kotlinx.coroutines.runBlocking
import net.minecraft.client.gui.layouts.LinearLayout
import net.minecraft.client.gui.layouts.SpacerElement
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.Text.wrap
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockpv.SkyBlockPv
import tech.thatgravyboat.skyblockpv.api.PronounsDbAPI
import tech.thatgravyboat.skyblockpv.api.SkillAPI
import tech.thatgravyboat.skyblockpv.api.SkillAPI.getSkillLevel
import tech.thatgravyboat.skyblockpv.api.StatusAPI
import tech.thatgravyboat.skyblockpv.api.data.PlayerStatus
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.data.SkullTextures
import tech.thatgravyboat.skyblockpv.data.getIconFromSlayerName
import tech.thatgravyboat.skyblockpv.data.getSlayerLevel
import tech.thatgravyboat.skyblockpv.screens.BasePvScreen
import tech.thatgravyboat.skyblockpv.screens.elements.ExtraConstants
import tech.thatgravyboat.skyblockpv.utils.FakePlayer
import tech.thatgravyboat.skyblockpv.utils.LayoutBuild
import tech.thatgravyboat.skyblockpv.utils.LayoutBuilder.Companion.setPos
import tech.thatgravyboat.skyblockpv.utils.LayoutUtils.centerHorizontally
import tech.thatgravyboat.skyblockpv.utils.Utils.append
import tech.thatgravyboat.skyblockpv.utils.Utils.pushPop
import tech.thatgravyboat.skyblockpv.utils.Utils.round
import tech.thatgravyboat.skyblockpv.utils.Utils.shorten
import tech.thatgravyboat.skyblockpv.utils.Utils.toTitleCase
import tech.thatgravyboat.skyblockpv.utils.components.PvWidgets
import tech.thatgravyboat.skyblockpv.utils.displays.*
import java.text.SimpleDateFormat


class MainScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BasePvScreen("MAIN", gameProfile, profile) {

    private var cachedX = 0.0F
    private var cachedY = 0.0F

    override fun create(bg: DisplayWidget) {
        val middleColumnWidth = (uiWidth * 0.2).toInt()
        val sideColumnWidth = (uiWidth - middleColumnWidth) / 2

        LayoutBuild.horizontal {
            widget(createLeftColumn(profile!!, sideColumnWidth))
            widget(createMiddleColumn(profile!!, middleColumnWidth))
            widget(createRightColumn(profile!!, sideColumnWidth))
        }.setPos(bg.x, bg.y).visitWidgets(this::addRenderableWidget)
    }

    private fun createLeftColumn(profile: SkyBlockProfile, width: Int) = LayoutBuild.vertical(alignment = 0.5f) {
        spacer(height = 5)

        val irrelevantSkills = listOf(
            "SKILL_RUNECRAFTING",
            "SKILL_SOCIAL",
        )

        val skillAvg = profile.skill
            .filterNot { it.key in irrelevantSkills }
            .map { getSkillLevel(SkillAPI.getSkill(it.key), it.value, profile) }
            .average()

        widget(PvWidgets.getTitleWidget("Info", width))

        val infoColumn = LayoutBuild.vertical(2) {
            fun grayText(text: String) = Displays.text(text, color = { 0x555555u }, shadow = false)

            string("Purse: ${profile.currency?.purse?.toFormattedString()}")
            string("Motes: ${profile.currency?.motes?.toFormattedString()}")
            string(
                buildString {
                    append("Bank: ")
                    val soloBank = profile.currency?.soloBank.takeIf { it != 0L }?.toFormattedString()
                    val mainBank = profile.currency?.mainBank.takeIf { it != 0L }?.toFormattedString()

                    if (soloBank != null && mainBank != null) append("$soloBank/$mainBank")
                    else append(soloBank ?: mainBank ?: "0")
                },
            )
            string("Cookie Buff: ${"§aActive".takeIf { profile.currency?.cookieBuffActive == true } ?: "§cInactive"}")
            display(
                grayText("SkyBlock Level: ${profile.skyBlockLevel.first}")
                    .withTooltip("Progress: ${profile.skyBlockLevel.second}/100"),
            )
            display(
                grayText("First Join: ${SimpleDateFormat("yyyy.MM.dd").format(profile.firstJoin)}")
                    .withTooltip(SimpleDateFormat("yyyy.MM.dd HH:mm").format(profile.firstJoin)),
            )
            display(
                grayText("Skill Avg: ${skillAvg.round()}"),
            )

            display(
                listOf(
                    grayText("Pronouns: "),
                    PronounsDbAPI.getDisplay(gameProfile.id),
                ).toRow().withTooltip("Provided by https://pronoundb.org/"),
            )
            string("Fairy Souls: ${profile.fairySouls}")
        }

        widget(PvWidgets.getMainContentWidget(infoColumn, width))
    }

    private fun createMiddleColumn(profile: SkyBlockProfile, width: Int): LinearLayout {
        val height = (width * 1.1).toInt()
        val armor = profile.inventory?.armorItems?.inventory ?: List(4) { ItemStack.EMPTY }
        val skyblockLvl = profile.skyBlockLevel.first
        val skyblockLvlColor = tech.thatgravyboat.skyblockapi.api.profile.profile.ProfileAPI.getLevelColor(skyblockLvl)
        val name = Text.join("§8[", Text.of("$skyblockLvl").withColor(skyblockLvlColor), "§8] §f", gameProfile.name)
        val fakePlayer = FakePlayer(gameProfile, name, armor)
        val playerWidget = Displays.background(SkyBlockPv.id("buttons/dark/disabled"), width, height).asWidget().withRenderer { gr, ctx, _ ->
            val eyesX = (ctx.mouseX - ctx.x).toFloat().takeIf { ctx.mouseX >= 0 }?.also { cachedX = it } ?: cachedX
            val eyesY = (ctx.mouseY - ctx.y).toFloat().takeIf { ctx.mouseY >= 0 }?.also { cachedY = it } ?: cachedY
            gr.pushPop {
                translate(0f, 0f, 100f)
                Displays.entity(
                    fakePlayer,
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

    private fun createRightColumn(profile: SkyBlockProfile, width: Int) = LayoutBuild.vertical(alignment = 0.5f) {
        spacer(height = 5)

        fun <D, T> addSection(
            title: String,
            titleIcon: ResourceLocation? = null,
            data: Sequence<Pair<D, T>>,
            getToolTip: (D, T) -> Component? = { _, _ -> null },
            getIcon: (D) -> Any,
            getLevel: (D, T) -> Any,
        ) {
            val mainContent = LinearLayout.vertical().spacing(5)

            val convertedElements = data.map { (name, data) ->
                val level = getLevel(name, data).toString()
                val iconValue = getIcon(name)
                val icon = when (iconValue) {
                    is ResourceLocation -> Displays.sprite(iconValue, 12, 12)
                    is ItemStack -> Displays.item(iconValue, 12, 12)
                    else -> error("Invalid icon type")
                }
                val display = listOf(
                    icon,
                    Displays.padding(0, 0, 2, 0, Displays.text(level, color = { 0x555555u }, shadow = false)),
                ).toRow(1)
                val skillDisplay = Displays.background(SkyBlockPv.id("box/rounded_box_thin"), Displays.padding(2, display))
                (getToolTip(name, data)?.let { skillDisplay.withTooltip(it) } ?: skillDisplay).asWidget()
            }.toList()

            val elementsPerRow = width / (convertedElements.firstOrNull()?.width?.plus(10) ?: return)
            if (elementsPerRow < 1) return

            convertedElements.chunked(elementsPerRow).forEach { chunk ->
                val element = LinearLayout.horizontal().spacing(5)
                chunk.forEach { element.addChild(it) }
                mainContent.addChild(element.centerHorizontally(width))
            }

            mainContent.arrangeElements()

            widget(PvWidgets.getTitleWidget(title, width, titleIcon))
            widget(PvWidgets.getMainContentWidget(mainContent, width))
        }

        addSection(
            title = "Skills",
            titleIcon = SkyBlockPv.id("icon/item/sword"),
            data = profile.skill.asSequence().map { SkillAPI.getSkill(it.key) to it.value },
            getToolTip = { skill, num ->
                SkillAPI.getProgressToNextLevel(skill, num, profile).let { progress ->
                    TooltipBuilder().apply {
                        add(skill.data.name) { this.color = TextColor.YELLOW }
                        add("Exp: ${num.shorten()}") { this.color = TextColor.GRAY }
                        add("Progress: ") {
                            this.color = TextColor.GRAY
                            if (progress == 1f) {
                                append("Maxed!") { this.color = TextColor.RED }
                            } else if (skill.hasFloatingLevelCap() && getSkillLevel(skill, num, profile) == skill.maxLevel(profile)) {
                                append("Reached max skill cap!") { this.color = TextColor.DARK_PURPLE }
                            } else {
                                append("${(progress * 100).round()}% to next") { this.color = TextColor.GREEN }
                            }
                        }
                        if (skill.data.maxLevel != getSkillLevel(skill, num, profile)) {
                            add("Progress to max: ") {
                                this.color = TextColor.GRAY
                                val expRequired = SkillAPI.getExpRequired(skill, skill.data.maxLevel)
                                if (expRequired == null) {
                                    append("Unknown") { this.color = TextColor.RED }
                                    return@add
                                }

                                append(num.toFormattedString()) { this.color = TextColor.YELLOW }
                                append("/") { this.color = TextColor.GOLD }
                                append(expRequired.shorten()) { this.color = TextColor.YELLOW }
                                append(
                                    Text.of(((num.toFloat() / expRequired) * 100).round()) {
                                        append("%")
                                        this.color = TextColor.GREEN
                                    }.wrap(" (", ")"),
                                )
                            }
                        }
                    }.build()
                }
            },
            getIcon = SkillAPI.Skill::icon,
        ) { name, num ->
            getSkillLevel(name, num, profile)
        }

        spacer(height = 10)

        addSection(
            title = "Slayer",
            data = profile.slayer.asSequence().map { it.toPair() },
            getIcon = ::getIconFromSlayerName,
            getToolTip = { name, data ->
                TooltipBuilder().apply {
                    add(name.toTitleCase()) { this.color = TextColor.YELLOW }
                    add("Exp: ${data.exp.shorten()}") { this.color = TextColor.GRAY }
                    add("Kills: ") {
                        this.color = TextColor.GRAY
                        Text.join(
                            data.bossKillsTier.map {
                                Text.of(it.value.toFormattedString()) { this.color = TextColor.GRAY }
                            },
                            separator = Text.of("/") { this.color = TextColor.DARK_GRAY }
                                .takeUnless { it.stripped.isBlank() } ?: Text.of("0") { this.color = TextColor.GRAY },
                        ).let { append(it) }
                    }
                }.build()
            },
        )
        { name, data ->
            getSlayerLevel(name, data.exp)
        }

        spacer(height = 10)

        val essence = profile.currency?.essence?.asSequence()?.map { it.toPair() } ?: emptySequence()
        if (essence.sumOf { it.second } == 0L) return@vertical
        addSection(
            title = "Essence",
            data = essence,
            getIcon = {
                when (it) {
                    "WITHER" -> SkullTextures.WITHER_ESSENCE
                    "SPIDER" -> SkullTextures.SPIDER_ESSENCE
                    "UNDEAD" -> SkullTextures.UNDEAD_ESSENCE
                    "DRAGON" -> SkullTextures.DRAGON_ESSENCE
                    "GOLD" -> SkullTextures.GOLD_ESSENCE
                    "DIAMOND" -> SkullTextures.DIAMOND_ESSENCE
                    "ICE" -> SkullTextures.ICE_ESSENCE
                    "CRIMSON" -> SkullTextures.CRIMSON_ESSENCE
                    else -> null
                }?.createSkull() ?: ItemStack.EMPTY
            },
        ) { _, amount -> amount.shorten() }
    }

}
