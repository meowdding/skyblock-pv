package me.owdding.skyblockpv.screens.windowed.tabs

import com.mojang.authlib.GameProfile
import com.mojang.blaze3d.platform.InputConstants
import earth.terrarium.olympus.client.components.base.renderer.WidgetRenderer
import earth.terrarium.olympus.client.components.buttons.Button
import earth.terrarium.olympus.client.components.renderers.WidgetRenderers
import kotlinx.coroutines.runBlocking
import me.owdding.lib.builder.LayoutFactory
import me.owdding.lib.builder.MIDDLE
import me.owdding.lib.displays.*
import me.owdding.lib.extensions.floor
import me.owdding.lib.extensions.round
import me.owdding.lib.extensions.shorten
import me.owdding.lib.layouts.setPos
import me.owdding.lib.utils.keys
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.api.SkillAPI
import me.owdding.skyblockpv.api.SkillAPI.getSkillLevel
import me.owdding.skyblockpv.api.StatusAPI
import me.owdding.skyblockpv.api.data.PlayerStatus
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.config.Config
import me.owdding.skyblockpv.data.api.skills.combat.SlayerTypeData
import me.owdding.skyblockpv.data.api.skills.combat.getIconFromSlayerName
import me.owdding.skyblockpv.data.repo.SlayerCodecs
import me.owdding.skyblockpv.screens.PvTab
import me.owdding.skyblockpv.screens.windowed.BaseWindowedPvScreen
import me.owdding.skyblockpv.screens.windowed.elements.ExtraConstants
import me.owdding.skyblockpv.screens.windowed.tabs.general.NetworthDisplay
import me.owdding.skyblockpv.utils.FakePlayer
import me.owdding.skyblockpv.utils.LayoutUtils.asScrollable
import me.owdding.skyblockpv.utils.LayoutUtils.centerHorizontally
import me.owdding.skyblockpv.utils.Utils.append
import me.owdding.skyblockpv.utils.Utils.asTranslated
import me.owdding.skyblockpv.utils.Utils.plus
import me.owdding.skyblockpv.utils.Utils.unaryPlus
import me.owdding.skyblockpv.utils.components.PvLayouts
import me.owdding.skyblockpv.utils.components.PvToast
import me.owdding.skyblockpv.utils.components.PvWidgets
import me.owdding.skyblockpv.utils.displays.ExtraDisplays.grayText
import me.owdding.skyblockpv.utils.theme.PvColors
import me.owdding.skyblockpv.utils.theme.ThemeSupport
import me.owdding.skyblockpv.widgets.PronounWidget
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.client.gui.layouts.LayoutElement
import net.minecraft.client.gui.layouts.LinearLayout
import net.minecraft.client.gui.layouts.SpacerElement
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.util.TriState
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import tech.thatgravyboat.skyblockapi.api.remote.RepoItemsAPI
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.platform.pushPop
import tech.thatgravyboat.skyblockapi.utils.builders.TooltipBuilder
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.extentions.toTitleCase
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.Text.wrap
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.strikethrough
import java.text.SimpleDateFormat


class MainScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseWindowedPvScreen("MAIN", gameProfile, profile) {

    private val rightClick = keys {
        withButton(InputConstants.MOUSE_BUTTON_RIGHT)
    }

    private var cachedX = 0.0F
    private var cachedY = 0.0F

    override fun create(bg: DisplayWidget) {
        val middleColumnWidth = (uiWidth * 0.2).toInt()
        val sideColumnWidth = (uiWidth - middleColumnWidth) / 2

        val leftStuff = PvLayouts.vertical(10) {
            spacer()
            widget(getSkillSection(profile, sideColumnWidth - 20))
            widget(getSlayerSection(sideColumnWidth - 20))
            widget(getEssenceSection(sideColumnWidth - 20))
        }


        fun Layout.applyLayout() {
            this.setPos(bg.x, bg.y).visitWidgets(this@MainScreen::addRenderableWidget)
        }

        if (leftStuff.height < uiHeight) {
            return PvLayouts.horizontal {
                vertical {
                    spacer(height = 10)
                    widget(getGeneralInfo(profile, sideColumnWidth))
                }
                vertical {
                    val player = getPlayerDisplay(profile, middleColumnWidth)
                    player.arrangeElements()
                    spacer(height = (uiHeight - player.height) / 2)
                    widget(player)
                }
                widget(leftStuff)

            }.applyLayout()
        }

        PvLayouts.horizontal {
            val newWidth = (uiWidth * 0.35).toInt()
            vertical {
                val playerDisplay = getPlayerDisplay(profile, newWidth)
                playerDisplay.arrangeElements()
                spacer(newWidth, (uiHeight - playerDisplay.height) / 2)
                horizontal {
                    spacer(10)
                    widget(playerDisplay)
                }
            }
            val width = uiWidth - newWidth - 40
            widget(
                PvLayouts.vertical(10) {
                    widget(getGeneralInfo(profile, width)) {
                        alignHorizontallyCenter()
                    }
                    widget(getSkillSection(profile, width))
                    widget(getSlayerSection(width))
                    widget(getEssenceSection(width))
                }.asScrollable(width + 27, uiHeight),
            )
        }.applyLayout()
    }

    private fun getGeneralInfo(profile: SkyBlockProfile, width: Int) = PvLayouts.vertical(alignment = 0.5f) {
        val irrelevantSkills = listOf(
            "SKILL_RUNECRAFTING",
            "SKILL_SOCIAL",
        )

        val skillAvg = profile.skill
            .filterNot { it.key in irrelevantSkills }
            .map { getSkillLevel(SkillAPI.getSkill(it.key), it.value, profile) }
            .average()

        widget(PvWidgets.getTitleWidget("Info", width, SkyBlockPv.id("icon/item/clipboard")))

        val infoColumn = PvLayouts.vertical(2) {
            string("screens.main.info.purse".asTranslated(profile.currency?.purse?.toFormattedString()))
            display(
                grayText(
                    "screens.main.info.bank".asTranslated(
                        run {
                            val soloBank = profile.bank?.soloBank.takeIf { it != 0L }?.shorten(2)
                            val mainBank = profile.bank?.profileBank.takeIf { it != 0L }?.shorten(2)

                            if (soloBank != null && mainBank != null) "$soloBank/$mainBank"
                            else soloBank ?: mainBank ?: "0"
                        },
                    ),
                ).withTooltip {
                    val bank = profile.bank ?: return@withTooltip
                    add("screens.main.info.bank.solo".asTranslated(bank.soloBank.toFormattedString()))
                    add("screens.main.info.bank.coop".asTranslated(bank.profileBank.toFormattedString()))

                    if (bank.history.isEmpty()) return@withTooltip

                    this.space()
                    this.add(+"screens.main.info.bank.history")
                    this.add("                                           ") {
                        strikethrough = true
                        color = PvColors.DARK_GRAY
                    }
                    bank.history.forEach {
                        this.add(
                            "screens.main.info.bank.history.entry".asTranslated(
                                it.amount.toFormattedString(),
                                +"screens.main.info.bank.actions.${it.action.lowercase()}",
                                it.initiator,
                                SimpleDateFormat("yyyy.MM.dd HH:mm").format(it.timestamp),
                            ),
                        )
                        this.add("                                           ") {
                            strikethrough = true
                            color = PvColors.DARK_GRAY
                        }
                    }
                },
            )
            string(
                "screens.main.info.cookie".asTranslated(
                    +"screens.main.info.cookie.${if (profile.currency?.cookieBuffActive == true) "active" else "inactive"}",
                ),
            )
            profile.skyBlockLevel.let { (level, progress) ->
                val skyblockLevel = if (progress == 0) "$level" else "$level.${progress.toString().padStart(2, '0')}"
                display(
                    grayText("screens.main.info.sb_lvl".asTranslated(skyblockLevel))
                        .withTooltip("screens.main.info.sb_lvl.progress".asTranslated(progress)),
                )
            }
            display(
                grayText("screens.main.info.first_join".asTranslated(SimpleDateFormat("yyyy.MM.dd").format(profile.firstJoin)))
                    .withTooltip(SimpleDateFormat("yyyy.MM.dd HH:mm").format(profile.firstJoin)),
            )
            display(grayText("screens.main.info.skill_avg".asTranslated(skillAvg.round())))
            display(grayText("screens.main.info.fairy_souls".asTranslated(profile.fairySouls)))

            val totalKills = profile.mobData.maxOfOrNull { it.kills } ?: 0
            val totalDeaths = profile.mobData.maxOfOrNull { it.deaths } ?: 0
            val kd = if (totalDeaths == 0L) "∞" else (totalKills.toDouble() / totalDeaths).round()
            display(grayText("screens.main.info.kd".asTranslated(totalKills.shorten(), totalDeaths.shorten(), kd)))

            display(NetworthDisplay.getNetworthDisplay(profile))
        }

        widget(PvWidgets.getMainContentWidget(infoColumn, width))
    }

    private fun getPlayerDisplay(profile: SkyBlockProfile, width: Int): LinearLayout {
        val height = (width * 1.1).toInt()
        val armor = profile.inventory?.armorItems ?: List(4) { ItemStack.EMPTY }
        val skyblockLvl = profile.skyBlockLevel.first
        val skyblockLvlColor = tech.thatgravyboat.skyblockapi.api.profile.profile.ProfileAPI.getLevelColor(skyblockLvl)
        val name = Text.join("§8[", Text.of("$skyblockLvl").withColor(skyblockLvlColor), "§8] §f", gameProfile.name)
        val fakePlayer = FakePlayer(gameProfile, name, armor)
        val nakedFakePlayer = FakePlayer(gameProfile, name)
        val playerWidget = Displays.background(ThemeSupport.texture(SkyBlockPv.id("buttons/disabled")), width, height).asWidget().withRenderer { gr, ctx, _ ->
            val isHovered = ctx.mouseX in ctx.x..(ctx.x + width) && ctx.mouseY in ctx.y..(ctx.y + height)
            val eyesX = (ctx.mouseX - ctx.x).toFloat().takeIf { ctx.mouseX >= 0 }?.also { cachedX = it } ?: cachedX
            val eyesY = (ctx.mouseY - ctx.y).toFloat().takeIf { ctx.mouseY >= 0 }?.also { cachedY = it } ?: cachedY
            gr.pushPop {
                Displays.entity(
                    if (rightClick.isDown() && isHovered) {
                        nakedFakePlayer
                    } else {
                        fakePlayer
                    },
                    width,
                    height,
                    (width / 3f).floor(),
                    eyesX, eyesY,
                ).render(gr, ctx.x, ctx.y + height / 10)
            }
        }

        val layout = LinearLayout.vertical()
        layout.addChild(playerWidget)
        layout.addChild(SpacerElement.height(5))
        layout.addChild(getStatusButton().withSize(width, 20))
        layout.addChild(SpacerElement.height(3))
        if (Config.showPronouns) {
            layout.addChild(PronounWidget.getPronounDisplay(gameProfile.id, width).asWidget())
        }

        return layout
    }

    fun <D, T> createSection(
        title: Component,
        titleIcon: Identifier? = null,
        width: Int,
        data: Sequence<Pair<D, T>>,
        getToolTip: (D, T) -> Component? = { _, _ -> null },
        getIcon: (D) -> Any,
        getLevel: (D, T) -> Any,
    ): LayoutElement {
        val convertedElements = data.map { (name, data) ->
            val level = getLevel(name, data).toString()
            val iconValue = getIcon(name)
            val icon = when (iconValue) {
                is Identifier -> Displays.sprite(ThemeSupport.texture(iconValue), 12, 12)
                is ItemStack -> Displays.item(iconValue, 12, 12)
                else -> error("Invalid icon type")
            }
            val display = listOf(
                icon,
                Displays.padding(0, 0, 2, 0, grayText(level)),
            ).toRow(1)
            val skillDisplay = Displays.background(ThemeSupport.texture(SkyBlockPv.id("box/rounded_box_thin")), Displays.padding(2, display))
            (getToolTip(name, data)?.let { skillDisplay.withTooltip(it) } ?: skillDisplay).asWidget()
        }.toList()

        if (convertedElements.isEmpty()) return PvLayouts.empty()
        val elementsPerRow = width / (convertedElements.maxOf { it.width } + 10)
        if (elementsPerRow < 1) return PvLayouts.empty()

        return PvWidgets.label(
            title,
            LayoutFactory.vertical(5, MIDDLE) {
                convertedElements.chunked(elementsPerRow).forEach { chunk ->
                    val element = LinearLayout.horizontal().spacing(5)
                    chunk.forEach { element.addChild(it) }
                    widget(element.centerHorizontally(width))
                }
            },
            icon = titleIcon,
        )
    }

    fun getStatusButton(): Button {
        val status = StatusAPI.getCached(gameProfile.id)

        fun getStatusDisplay(status: PlayerStatus): WidgetRenderer<Button> {
            val statusText = +"screens.main.status.${status.status.name.lowercase()}"
            val location = SkyBlockIsland.entries.find { it.id == status.location }?.toString() ?: status.location
            val locationText = location?.let { Text.of(it).withColor(PvColors.GREEN) } ?: +"screens.main.status.unknown"
            return WidgetRenderers.text(statusText + locationText)
        }

        return Button().also { button ->
            button.withTexture(ExtraConstants.BUTTON_DARK)
            button.withRenderer(WidgetRenderers.text(+"screens.main.status.load"))
            button.withCallback {
                button.withRenderer(WidgetRenderers.text(+"screens.main.status.loading"))
                runBlocking {
                    val status = StatusAPI.getData(gameProfile.id).getOrNull()
                    if (status == null) {
                        button.withRenderer(WidgetRenderers.text(+"screens.main.status.error"))
                    } else {
                        button.withRenderer(getStatusDisplay(status))
                        button.asDisabled()
                    }
                }
            }

            if (status != null) {
                button.withRenderer(getStatusDisplay(status))
                button.asDisabled()
            }
        }
    }

    fun getSkillSection(profile: SkyBlockProfile, width: Int) = createSection(
        title = +"screens.main.skills",
        titleIcon = SkyBlockPv.id("icon/item/sword"),
        data = profile.skill.asSequence().map { SkillAPI.getSkill(it.key) to it.value },
        getToolTip = { skill, num ->
            SkillAPI.getProgressToNextLevel(skill, num, profile).let { progress ->
                TooltipBuilder().apply {
                    add(skill.data.name) { this.color = PvColors.YELLOW }
                    add("Exp: ${num.shorten()}") { this.color = PvColors.GRAY }
                    if (skill.id == "HUNTING" && num == 0L) { // TODO REMOVE
                        add("Hypixel currently does not share your Hunting XP so we cant actually show a value") {
                            color = TextColor.RED
                        }
                        add("This entry exists for when Hypixel finally adds it") {
                            color = TextColor.RED
                        }
                    }
                    add {
                        append(+"screens.main.skills.progress")
                        this.color = PvColors.GRAY
                        if (progress == 1f) {
                            append(+"misc.maxed")
                        } else if (skill.hasFloatingLevelCap() && getSkillLevel(skill, num, profile) == skill.maxLevel(profile)) {
                            append(+"screens.main.skills.max_cap")
                        } else {
                            append("screens.main.skills.to_next".asTranslated((progress * 100).round()))
                        }
                    }
                    if (skill.data.maxLevel != getSkillLevel(skill, num, profile)) {
                        add {
                            append(+"screens.main.skills.progress_to_max")
                            this.color = PvColors.GRAY
                            val expRequired = SkillAPI.getExpRequired(skill, skill.data.maxLevel)
                            if (expRequired == null) {
                                append(+"screens.main.skills.unknown")
                                return@add
                            }

                            append(num.toFormattedString()) { this.color = PvColors.YELLOW }
                            append("/") { this.color = PvColors.GOLD }
                            append(expRequired.shorten()) { this.color = PvColors.YELLOW }
                            append(
                                Text.of(((num.toFloat() / expRequired) * 100).round()) {
                                    append("%")
                                    this.color = PvColors.GREEN
                                }.wrap(" (", ")"),
                            )
                        }
                    }
                }.build()
            }
        },
        width = width,
        getIcon = SkillAPI.Skill::icon,
    ) { name, num ->
        getSkillLevel(name, num, profile)
    }

    fun getEssenceSection(width: Int): LayoutElement {
        val essence = profile.currency?.essence?.asSequence()?.map { it.toPair() } ?: emptySequence()
        if (essence.sumOf { it.second } == 0L) return PvLayouts.empty()
        return createSection(
            title = +"screens.main.essence",
            data = essence,
            width = width,
            getIcon = { RepoItemsAPI.getItem("ESSENCE_${it.uppercase()}") },
        ) { _, amount -> amount.shorten() }
    }

    fun getSlayerSection(width: Int) = createSection(
        title = +"screens.main.slayer",
        width = width,
        data = SlayerCodecs.data.map { (k, v) ->
            val data = profile.slayer[v.id] ?: SlayerTypeData.EMPTY
            Pair(k, Pair(v, data))
        }.asSequence(),
        getIcon = ::getIconFromSlayerName,
        getToolTip = { name, pair ->
            val (repo, data) = pair
            TooltipBuilder().apply {
                add(name.toTitleCase()) { this.color = PvColors.YELLOW }
                add {
                    append(+"screens.main.slayer.kills")
                    this.color = PvColors.GRAY
                    Text.join(
                        (0 until repo.maxBossTier).map {
                            Text.of(data.bossKillsTier[it]?.toFormattedString() ?: "0") { this.color = PvColors.GRAY }
                        },
                        separator = Text.of("/") { this.color = PvColors.DARK_GRAY },
                    ).let { append(it) }
                }
                add {
                    append(+"screens.main.slayer.exp")
                    this.color = PvColors.GRAY
                    append(data.exp.toFormattedString()) { this.color = PvColors.YELLOW }

                    val percentage = data.exp / repo.leveling.last().toDouble() * 100
                    if (percentage >= 100) {
                        append(CommonComponents.SPACE)
                        append(+"misc.maxed")
                    } else {
                        append("/") { this.color = PvColors.GOLD }
                        append(repo.leveling.last().toFormattedString()) { this.color = PvColors.YELLOW }
                        append(
                            Text.of(((data.exp.toFloat() / repo.leveling.last()) * 100).round()) {
                                append("%")
                                this.color = PvColors.GREEN
                            }.wrap(" (", ")"),
                        )
                    }
                }

                if (repo.getLevel(data.exp) != repo.maxLevel) {
                    add {
                        append(+"screens.main.slayer.next_level")
                        this.color = PvColors.GRAY
                        append(data.exp.toFormattedString()) { this.color = PvColors.YELLOW }
                        append("/") { this.color = PvColors.GOLD }
                        append(repo.leveling[repo.getLevel(data.exp)].toFormattedString()) { this.color = PvColors.YELLOW }
                        append(
                            Text.of(((data.exp.toFloat() / repo.leveling[repo.getLevel(data.exp)]) * 100).round()) {
                                append("%")
                                this.color = PvColors.GREEN
                            }.wrap(" (", ")"),
                        )
                    }
                }

            }.build()
        },
    ) { _, data ->
        data.first.getLevel(data.second.exp)
    }

    override fun onProfileSwitch(profile: SkyBlockProfile) {
        if (!profile.dataFuture.isDone) return
        val disabledTabs = PvTab.entries
            .filter { it.getTabState(profile) != TriState.TRUE }
            .filter { it.canDisplay(profile) }
        if (disabledTabs.isNotEmpty()) {
            McClient.runNextTick { PvToast.addFailedToLoadForUsers(profile, disabledTabs) }
        }
    }
}
