package me.owdding.skyblockpv.screens.tabs

import com.mojang.authlib.GameProfile
import com.mojang.blaze3d.platform.InputConstants
import earth.terrarium.olympus.client.components.buttons.Button
import earth.terrarium.olympus.client.components.renderers.WidgetRenderers
import kotlinx.coroutines.runBlocking
import me.owdding.lib.builder.LayoutBuilder.Companion.setPos
import me.owdding.lib.builder.LayoutFactory
import me.owdding.lib.displays.*
import me.owdding.lib.extensions.round
import me.owdding.lib.extensions.shorten
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.api.SkillAPI
import me.owdding.skyblockpv.api.SkillAPI.getSkillLevel
import me.owdding.skyblockpv.api.StatusAPI
import me.owdding.skyblockpv.api.data.PlayerStatus
import me.owdding.skyblockpv.api.data.SkyBlockProfile
import me.owdding.skyblockpv.config.Config
import me.owdding.skyblockpv.data.api.skills.combat.SlayerTypeData
import me.owdding.skyblockpv.data.api.skills.combat.getIconFromSlayerName
import me.owdding.skyblockpv.data.repo.SkullTextures
import me.owdding.skyblockpv.data.repo.SlayerCodecs
import me.owdding.skyblockpv.screens.BasePvScreen
import me.owdding.skyblockpv.screens.PvTab
import me.owdding.skyblockpv.screens.elements.ExtraConstants
import me.owdding.skyblockpv.screens.tabs.general.NetworthDisplay
import me.owdding.skyblockpv.screens.tabs.general.PronounDisplay
import me.owdding.skyblockpv.utils.FakePlayer
import me.owdding.skyblockpv.utils.LayoutUtils.asScrollable
import me.owdding.skyblockpv.utils.LayoutUtils.centerHorizontally
import me.owdding.skyblockpv.utils.Utils.append
import me.owdding.skyblockpv.utils.components.FailedToLoadToast
import me.owdding.skyblockpv.utils.components.PvWidgets
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.client.gui.layouts.LayoutElement
import net.minecraft.client.gui.layouts.LinearLayout
import net.minecraft.client.gui.layouts.SpacerElement
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.TriState
import net.minecraft.world.item.ItemStack
import org.lwjgl.glfw.GLFW
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.builders.TooltipBuilder
import tech.thatgravyboat.skyblockapi.utils.extentions.pushPop
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.extentions.toTitleCase
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.Text.wrap
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.strikethrough
import java.text.SimpleDateFormat


class MainScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BasePvScreen("MAIN", gameProfile, profile) {

    private var cachedX = 0.0F
    private var cachedY = 0.0F

    override fun create(bg: DisplayWidget) {
        val middleColumnWidth = (uiWidth * 0.2).toInt()
        val sideColumnWidth = (uiWidth - middleColumnWidth) / 2

        val leftStuff = LayoutFactory.vertical(10) {
            spacer()
            widget(getSkillSection(profile, sideColumnWidth - 20))
            widget(getSlayerSection(sideColumnWidth - 20))
            widget(getEssenceSection(sideColumnWidth - 20))
        }


        fun Layout.applyLayout() {
            this.setPos(bg.x, bg.y).visitWidgets(this@MainScreen::addRenderableWidget)
        }

        if (leftStuff.height < uiHeight) {
            return LayoutFactory.horizontal {
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

        LayoutFactory.horizontal {
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
                LayoutFactory.vertical(10) {
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

    private fun getGeneralInfo(profile: SkyBlockProfile, width: Int) = LayoutFactory.vertical(alignment = 0.5f) {
        val irrelevantSkills = listOf(
            "SKILL_RUNECRAFTING",
            "SKILL_SOCIAL",
        )

        val skillAvg = profile.skill
            .filterNot { it.key in irrelevantSkills }
            .map { getSkillLevel(SkillAPI.getSkill(it.key), it.value, profile) }
            .average()

        widget(PvWidgets.getTitleWidget("Info", width, SkyBlockPv.id("icon/item/clipboard")))

        val infoColumn = LayoutFactory.vertical(2) {
            fun grayText(text: String) = Displays.text(text, color = { 0x555555u }, shadow = false)

            string("Purse: ${profile.currency?.purse?.toFormattedString()}")
            display(
                grayText(
                    buildString {
                        append("Bank: ")
                        val soloBank = profile.bank?.soloBank.takeIf { it != 0L }?.shorten(2)
                        val mainBank = profile.bank?.profileBank.takeIf { it != 0L }?.shorten(2)

                        if (soloBank != null && mainBank != null) append("$soloBank/$mainBank")
                        else append(soloBank ?: mainBank ?: "0")
                    },
                ).withTooltip {
                    val bank = profile.bank ?: return@withTooltip
                    this.add("§7Solo Bank: §6${bank.soloBank.toFormattedString()}")
                    this.add("§7Profile Bank: §6${bank.profileBank.toFormattedString()}")

                    if (bank.history.isEmpty()) return@withTooltip

                    this.space()
                    this.add("§7Bank History: ")
                    this.add("                                           ") {
                        strikethrough = true
                        color = TextColor.DARK_GRAY
                    }
                    bank.history.forEach {
                        this.add("§6${it.amount.toFormattedString()} §7${it.action.toTitleCase()} by §b${it.initiator}")
                        this.add("§7Date: §e${SimpleDateFormat("yyyy.MM.dd HH:mm").format(it.timestamp)}")
                        this.add("                                           ") {
                            strikethrough = true
                            color = TextColor.DARK_GRAY
                        }
                    }
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
            display(grayText("Skill Avg: ${skillAvg.round()}"))
            string("Fairy Souls: ${profile.fairySouls}")

            display(NetworthDisplay.getNetworthDisplay(profile))
        }

        widget(PvWidgets.getMainContentWidget(infoColumn, width))
    }

    private fun getPlayerDisplay(profile: SkyBlockProfile, width: Int): LinearLayout {
        val height = (width * 1.1).toInt()
        val armor = profile.inventory?.armorItems?.inventory ?: List(4) { ItemStack.EMPTY }
        val skyblockLvl = profile.skyBlockLevel.first
        val skyblockLvlColor = tech.thatgravyboat.skyblockapi.api.profile.profile.ProfileAPI.getLevelColor(skyblockLvl)
        val name = Text.join("§8[", Text.of("$skyblockLvl").withColor(skyblockLvlColor), "§8] §f", gameProfile.name)
        val fakePlayer = FakePlayer(gameProfile, name, armor)
        val nakedFakePlayer = FakePlayer(gameProfile, name)
        val playerWidget = Displays.background(SkyBlockPv.id("buttons/dark/disabled"), width, height).asWidget().withRenderer { gr, ctx, _ ->
            val isHovered = ctx.mouseX in ctx.x..(ctx.x + width) && ctx.mouseY in ctx.y..(ctx.y + height)
            val eyesX = (ctx.mouseX - ctx.x).toFloat().takeIf { ctx.mouseX >= 0 }?.also { cachedX = it } ?: cachedX
            val eyesY = (ctx.mouseY - ctx.y).toFloat().takeIf { ctx.mouseY >= 0 }?.also { cachedY = it } ?: cachedY
            gr.pushPop {
                translate(0f, 0f, 100f)
                Displays.entity(
                    if (GLFW.glfwGetMouseButton(McClient.window.window, InputConstants.MOUSE_BUTTON_RIGHT) == 1 && isHovered) {
                        nakedFakePlayer
                    } else {
                        fakePlayer
                    },
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
                val status = StatusAPI.getData(gameProfile.id).getOrNull()
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
        layout.addChild(playerWidget)
        layout.addChild(SpacerElement.height(5))
        layout.addChild(statusButtonWidget)
        layout.addChild(SpacerElement.height(3))
        if (Config.showPronouns) {
            layout.addChild(PronounDisplay.getPronounDisplay(gameProfile.id, width)
                .asWidget()
                .withTooltip(Text.of("Provided by https://pronoundb.org/"))
            )
        }

        return layout
    }

    fun <D, T> createSection(
        title: String,
        titleIcon: ResourceLocation? = null,
        width: Int,
        data: Sequence<Pair<D, T>>,
        getToolTip: (D, T) -> Component? = { _, _ -> null },
        getIcon: (D) -> Any,
        getLevel: (D, T) -> Any,
    ): LayoutElement {
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

        val elementsPerRow = (convertedElements.firstOrNull()?.width?.plus(10))?.let { width / it } ?: 0
        if (elementsPerRow < 1) return LayoutFactory.empty()

        convertedElements.chunked(elementsPerRow).forEach { chunk ->
            val element = LinearLayout.horizontal().spacing(5)
            chunk.forEach { element.addChild(it) }
            mainContent.addChild(element.centerHorizontally(width))
        }

        mainContent.arrangeElements()

        return PvWidgets.label(title, mainContent, icon = titleIcon)
    }

    fun getSkillSection(profile: SkyBlockProfile, width: Int) = createSection(
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
        width = width,
        getIcon = SkillAPI.Skill::icon,
    ) { name, num ->
        getSkillLevel(name, num, profile)
    }

    fun getEssenceSection(width: Int): LayoutElement {
        val essence = profile.currency?.essence?.asSequence()?.map { it.toPair() } ?: emptySequence()
        if (essence.sumOf { it.second } == 0L) return LayoutFactory.empty()
        return createSection(
            title = "Essence",
            data = essence,
            width = width,
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
                }?.skull ?: ItemStack.EMPTY
            },
        ) { _, amount -> amount.shorten() }
    }

    fun getSlayerSection(width: Int) = createSection(
        title = "Slayer",
        width = width,
        data = SlayerCodecs.data.map { (k, v) ->
            val data = profile.slayer[v.id] ?: SlayerTypeData.EMPTY
            Pair(k, Pair(v, data))
        }.asSequence(),
        getIcon = ::getIconFromSlayerName,
        getToolTip = { name, pair ->
            val (repo, data) = pair
            TooltipBuilder().apply {
                add(name.toTitleCase()) { this.color = TextColor.YELLOW }
                add("Kills: ") {
                    this.color = TextColor.GRAY
                    Text.join(
                        (0 until repo.maxBossTier).map {
                            Text.of(data.bossKillsTier[it]?.toFormattedString() ?: "0") { this.color = TextColor.GRAY }
                        },
                        separator = Text.of("/") { this.color = TextColor.DARK_GRAY },
                    ).let { append(it) }
                }
                add("Exp: ") {
                    this.color = TextColor.GRAY
                    append(data.exp.toFormattedString()) { this.color = TextColor.YELLOW }

                    val percentage = data.exp / repo.leveling.last().toDouble() * 100
                    if (percentage >= 100) {
                        append(" Maxed!") { this.color = TextColor.RED }
                    } else {
                        append("/") { this.color = TextColor.GOLD }
                        append(repo.leveling.last().toFormattedString()) { this.color = TextColor.YELLOW }
                        append(
                            Text.of(((data.exp.toFloat() / repo.leveling.last()) * 100).round()) {
                                append("%")
                                this.color = TextColor.GREEN
                            }.wrap(" (", ")"),
                        )
                    }
                }

                if (repo.getLevel(data.exp) != repo.maxLevel) {
                    add("Next Level: ") {
                        this.color = TextColor.GRAY
                        append(data.exp.toFormattedString()) { this.color = TextColor.YELLOW }
                        append("/") { this.color = TextColor.GOLD }
                        append(repo.leveling[repo.getLevel(data.exp)].toFormattedString()) { this.color = TextColor.YELLOW }
                        append(
                            Text.of(((data.exp.toFloat() / repo.leveling[repo.getLevel(data.exp)]) * 100).round()) {
                                append("%")
                                this.color = TextColor.GREEN
                            }.wrap(" (", ")"),
                        )
                    }
                }

            }.build()
        },
    )
    { name, data ->
        data.first.getLevel(data.second.exp)
    }

    override fun onProfileSwitch(profile: SkyBlockProfile) {
        val disabledTabs = PvTab.entries.filter { it.getTabState(profile) != TriState.TRUE }
        if (disabledTabs.isNotEmpty()) {
            FailedToLoadToast.add(
                profile,
                Displays.background(
                    SkyBlockPv.id("buttons/dark/disabled"),
                    Displays.padding(
                        5,
                        Displays.column(
                            Displays.text("§dSbPv§r: Disabled Tabs on Profile", { TextColor.RED.toUInt() }),
                            Displays.text("Due to missing data or disabled apis,", { TextColor.RED.toUInt() }),
                            Displays.text("the following tabs are disabled or altered:", { TextColor.RED.toUInt() }),
                            Displays.text(disabledTabs.joinToString(", ") { it.name.toTitleCase() }, { TextColor.RED.toUInt() }),
                        ),
                    ),
                ),
                5000,
            )
        }
    }
}
