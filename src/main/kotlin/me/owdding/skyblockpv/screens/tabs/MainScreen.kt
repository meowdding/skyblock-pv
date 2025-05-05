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
import me.owdding.skyblockpv.api.PronounsDbAPI
import me.owdding.skyblockpv.api.SkillAPI
import me.owdding.skyblockpv.api.SkillAPI.getSkillLevel
import me.owdding.skyblockpv.api.StatusAPI
import me.owdding.skyblockpv.api.data.PlayerStatus
import me.owdding.skyblockpv.api.data.SkyBlockProfile
import me.owdding.skyblockpv.config.Config
import me.owdding.skyblockpv.config.CurrenciesAPI
import me.owdding.skyblockpv.data.api.skills.combat.SlayerTypeData
import me.owdding.skyblockpv.data.api.skills.combat.getIconFromSlayerName
import me.owdding.skyblockpv.data.repo.SkullTextures
import me.owdding.skyblockpv.data.repo.SlayerCodecs
import me.owdding.skyblockpv.feature.NetworthCalculator
import me.owdding.skyblockpv.screens.BasePvScreen
import me.owdding.skyblockpv.screens.elements.ExtraConstants
import me.owdding.skyblockpv.utils.FakePlayer
import me.owdding.skyblockpv.utils.LayoutUtils.centerHorizontally
import me.owdding.skyblockpv.utils.Utils.append
import me.owdding.skyblockpv.utils.components.PvWidgets
import me.owdding.skyblockpv.utils.displays.ExtraDisplays
import net.minecraft.client.gui.layouts.LinearLayout
import net.minecraft.client.gui.layouts.SpacerElement
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import org.lwjgl.glfw.GLFW
import tech.thatgravyboat.skyblockapi.api.area.hub.BazaarAPI
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
import java.text.SimpleDateFormat
import kotlin.math.roundToInt


class MainScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BasePvScreen("MAIN", gameProfile, profile) {

    private var cachedX = 0.0F
    private var cachedY = 0.0F

    override fun create(bg: DisplayWidget) {
        val middleColumnWidth = (uiWidth * 0.2).toInt()
        val sideColumnWidth = (uiWidth - middleColumnWidth) / 2

        LayoutFactory.horizontal {
            widget(createLeftColumn(profile!!, sideColumnWidth))
            widget(createMiddleColumn(profile!!, middleColumnWidth))
            widget(createRightColumn(profile!!, sideColumnWidth))
        }.setPos(bg.x, bg.y).visitWidgets(this::addRenderableWidget)
    }

    private fun createLeftColumn(profile: SkyBlockProfile, width: Int) = LayoutFactory.vertical(alignment = 0.5f) {
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

        val infoColumn = LayoutFactory.vertical(2) {
            fun grayText(text: String) = Displays.text(text, color = { 0x555555u }, shadow = false)

            string("Purse: ${profile.currency?.purse?.toFormattedString()}")
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
            display(grayText("Skill Avg: ${skillAvg.round()}"))
            string("Fairy Souls: ${profile.fairySouls}")

            display(
                listOf(
                    grayText("Pronouns: "),
                    PronounsDbAPI.getDisplay(gameProfile.id),
                ).toRow().withTooltip("Provided by https://pronoundb.org/"),
            )

            horizontalDisplay {
                grayText("Net worth: ")
                ExtraDisplays.completableDisplay(
                    NetworthCalculator.calculateNetworthAsync(profile),
                    {
                        val cookiePrice = BazaarAPI.getProduct("BOOSTER_COOKIE")?.buyPrice ?: 0.0
                        val networthCookies = if (cookiePrice > 0) (it / cookiePrice).roundToInt() else 0
                        val networthUSD = ((networthCookies * 325.0) / 675.0) * 4.99

                        val (currency, networthConverted) = CurrenciesAPI.convert(Config.currency, networthUSD)

                        grayText(it.toFormattedString()).withTooltip {
                            if (cookiePrice <= 0) return@withTooltip

                            this.add {
                                this.append("Net worth in Cookies: ") { this.color = TextColor.YELLOW }
                                this.append(networthCookies.toFormattedString()) { this.color = TextColor.GOLD }
                            }

                            this.add {
                                this.append("Net worth in ${currency.name}: ") { this.color = TextColor.YELLOW }
                                val formattedNetworth = networthConverted.roundToInt().toFormattedString()
                                this.append("$$formattedNetworth ${currency.name}") { this.color = TextColor.GREEN }
                            }

                            this.space()
                            this.add("Note: You can change the currency in the settings using /sbpv.") { this.color = TextColor.GRAY }
                        }
                    },
                    { error ->
                        grayText("Failed to load").withTooltip {
                            this.add(Text.of("An error occurred: ") { this.color = TextColor.RED })
                            error.stackTraceToString().lines().forEach { line ->
                                this.add(Text.of(line) { this.color = TextColor.RED })
                            }
                        }
                    },
                    {
                        grayText("Loading...")
                    },
                ).let(this::display)
            }
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

    private fun createRightColumn(profile: SkyBlockProfile, width: Int) = LayoutFactory.vertical(alignment = 0.5f) {
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
            data = SlayerCodecs.data.map { (k, v) ->
                val data = profile.slayer[v.id] ?: SlayerTypeData.EMPTY
                Pair(k, Pair(v, data))
            }.asSequence(),
            getIcon = { name ->
                getIconFromSlayerName(name) // TODO: if addSection gets ever removed, include the icon in slayerdata
            },
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
                }?.skull ?: ItemStack.EMPTY
            },
        ) { _, amount -> amount.shorten() }
    }

}
