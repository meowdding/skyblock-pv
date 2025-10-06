package me.owdding.skyblockpv.screens.windowed.tabs.mining

import com.mojang.authlib.GameProfile
import me.owdding.lib.builder.LayoutFactory
import me.owdding.lib.builder.MIDDLE
import me.owdding.lib.displays.*
import me.owdding.lib.extensions.round
import me.owdding.skyblockpv.api.data.SkyBlockProfile
import me.owdding.skyblockpv.data.api.skills.MiningCore
import me.owdding.skyblockpv.data.api.skills.PowderType
import me.owdding.skyblockpv.data.repo.*
import me.owdding.skyblockpv.utils.LayoutUtils.asScrollable
import me.owdding.skyblockpv.utils.LayoutUtils.withScrollToBottom
import me.owdding.skyblockpv.utils.components.PvLayouts
import me.owdding.skyblockpv.utils.displays.ExtraDisplays
import me.owdding.skyblockpv.utils.theme.PvColors
import net.minecraft.client.gui.layouts.GridLayout
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.client.gui.layouts.SpacerElement
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.text.CommonText
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.bold
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.strikethrough
import java.text.DecimalFormat
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

class HotmScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseMiningScreen(gameProfile, profile) {

    override fun getLayout(bg: DisplayWidget): Layout {
        val mining = profile.mining ?: return PvLayouts.empty()
        val gridLayout = GridLayout()

        // nodes that are unlocked but not in the repo:
        val unknownNodes = mining.nodes.keys - MiningNodes.miningNodes.map { it.id }

        if (unknownNodes.isNotEmpty()) {
            println("Unknown hotm nodes: $unknownNodes")
        }

        MiningNodes.miningNodes.forEach { node ->
            if (node is SpacerNode) {
                gridLayout.addChild(
                    SpacerElement(node.size.x, node.size.y),
                    9 - node.location.y,
                    node.location.x + 2,
                )
                return@forEach
            }

            val level = (mining.nodes[node.id] ?: -1).let {
                if (node is AbilityMiningNode && it != -1) {
                    return@let mining.getAbilityLevel()
                }
                if (node is TierNode) {
                    return@let mining.getHotmLevel()
                }

                it
            }
            val disabled = mining.toggledNodes.contains(node.id).let {
                if (node is AbilityMiningNode) {
                    return@let mining.miningAbility != node.id
                }
                return@let it
            }
            gridLayout.addChild(
                ExtraDisplays.inventorySlot(Displays.padding(2, getNode(node, level, disabled, mining))).asWidget(),
                9 - node.location.y,
                node.location.x + 2,
            )
        }

        // TODO: remove whenever hypixel fixes their shit
        if (mining.nodes.isEmpty()) {
            return LayoutFactory.vertical(alignment = MIDDLE) {
                string("§cHypixel currently does not send any unlocked Hotm Perks aswell as the Hotm Experience,")
                string("§cso both of the features cannot be displayed.")
                widget(
                    gridLayout.asScrollable(uiWidth, uiHeight - 30) {
                        withScrollToBottom()
                    },
                )
            }

        }

        return gridLayout.asScrollable(uiWidth, uiHeight) {
            withScrollToBottom()
        }
    }

    private fun getNode(miningNode: MiningNode, level: Int, disabled: Boolean, miningCore: MiningCore): Display {
        val item = when (miningNode) {
            is TierNode -> when {
                miningNode.isMaxed(level) -> Items.GREEN_STAINED_GLASS_PANE
                miningNode.isMaxed(level + 1) -> Items.YELLOW_STAINED_GLASS_PANE
                else -> Items.RED_STAINED_GLASS_PANE
            }

            is CoreMiningNode -> when {
                level == 10 -> Items.DIAMOND_BLOCK
                level < 0 -> Items.BEDROCK
                level == 1 -> Items.IRON_BLOCK
                else -> Items.REDSTONE_BLOCK
            }

            is AbilityMiningNode -> when {
                miningCore.miningAbility == miningNode.id -> Items.EMERALD_BLOCK
                level > 0 -> Items.REDSTONE_BLOCK
                else -> Items.COAL_BLOCK
            }

            else -> when {
                disabled -> Items.REDSTONE
                !miningNode.isMaxed(level) && level != -1 -> Items.EMERALD
                miningNode.isMaxed(level) -> Items.DIAMOND
                else -> Items.COAL
            }
        }

        return Displays.item(item.defaultInstance, customStackText = if (level > 1 && miningNode !is TierNode) "$level" else "").withTooltip(
            getTooltip(miningNode, level, disabled, miningCore),
        )
    }

    private fun getTierNodeTooltip(node: TierNode, level: Int, miningCore: MiningCore) = buildList<Component> {
        val hasUnlocked = node.isMaxed(level)
        val isCurrentlyUnlocking = node.isMaxed(level + 1)

        add(node.name) {
            if (hasUnlocked) {
                this.color = PvColors.GREEN
            } else if (isCurrentlyUnlocking) {
                this.color = PvColors.YELLOW
            } else {
                this.color = PvColors.RED
            }
        }

        if (!hasUnlocked && isCurrentlyUnlocking) {
            val xpToNextLevel = miningCore.getXpToNextLevel()
            val xpRequiredForNextLevel = miningCore.getXpRequiredForNextLevel()

            addEmpty()
            val progress = (xpToNextLevel.toFloat() / xpRequiredForNextLevel)
            addGray("Progress: ") {
                append((progress * 100).round()) {
                    this.append("%")
                    this.color = PvColors.YELLOW
                }
            }

            val amountGreen = (25 * progress).roundToInt()
            val amountWhite = 25 - amountGreen

            add {
                append(" ".repeat(amountGreen)) {
                    this.color = PvColors.DARK_GREEN
                    this.strikethrough = true
                }
                append(" ".repeat(amountWhite)) {
                    this.color = PvColors.WHITE
                    this.strikethrough = true
                }
                append(" ")
                append(xpToNextLevel.toFormattedString()) { this.color = PvColors.YELLOW }

                append("/") { this.color = PvColors.GOLD }
                append(DecimalFormat.getCompactNumberInstance().format(xpRequiredForNextLevel)) { this.color = PvColors.YELLOW }
            }
        }

        addEmpty()
        addGray("Rewards")
        addAll(node.tooltip(Context()))

        addEmpty()
        if (hasUnlocked) {
            add("UNLOCKED") {
                this.color = PvColors.GREEN
                this.bold = true
            }
        } else if (isCurrentlyUnlocking) {
            add("LOCKED") {
                this.color = PvColors.RED
                this.bold = true
            }
        } else {
            add("Requires Tier ${node.location.y}") { this.color = PvColors.RED }
        }
    }

    private fun getTooltip(miningNode: MiningNode, level: Int, disabled: Boolean, miningCore: MiningCore) = buildList {
        if (miningNode is TierNode) {
            addAll(getTierNodeTooltip(miningNode, level, miningCore))
            return@buildList
        }

        add(miningNode.name) {
            if (disabled || level == -1) {
                this.color = PvColors.RED
            } else {
                this.color = PvColors.GREEN
            }
        }

        if (miningNode is LevelableMiningNode) {
            if (level == miningNode.maxLevel) {
                addGray("Level $level")
            } else {
                addGray("Level ${level.absoluteValue}/") {
                    append("${miningNode.maxLevel}") {
                        this.color = PvColors.DARK_GRAY
                    }
                }
            }
            addEmpty()
        }

        addAll(miningNode.tooltip(Context(miningCore.getHotmLevel(), level)))
        when (miningNode) {
            is AbilityMiningNode -> {
                if (!disabled) {
                    addEmpty()
                    addGray("SELECTED") {
                        this.color = PvColors.GREEN
                        this.bold = true
                    }
                }
            }

            !is CoreMiningNode -> {
                if (level <= 0) {
                    return@buildList
                }

                addEmpty()
                if (miningNode is LevelableMiningNode) {
                    addAll(getPowderSpent(miningNode, level))
                }

                if (disabled) {
                    add("DISABLED") {
                        this.color = PvColors.RED
                        this.bold = true
                    }
                } else {
                    add("ENABLED") {
                        this.color = PvColors.GREEN
                        this.bold = true
                    }
                }
            }
        }
    }

    private fun getPowderSpent(miningNode: LevelableMiningNode, level: Int): List<Component> = buildList {
        fun getAmountUntil(level: Int): Map<PowderType, Int> {
            return (1 until level).map { miningNode.costForLevel(it) }.groupBy { it.first }.mapValues { it.value.sumOf { it.second } }
        }

        val totalAmountRequired = getAmountUntil(miningNode.maxLevel + ((1).takeIf { miningNode is CoreMiningNode } ?: 0))
        val amountSpend = if (level == miningNode.maxLevel) {
            totalAmountRequired
        } else {
            getAmountUntil(level + ((1).takeIf { miningNode is CoreMiningNode } ?: 0))
        }


        addGray("Powder Spent")
        totalAmountRequired.forEach { (type, amountRequired) ->
            addGray {
                val amountSpent = amountSpend[type] ?: 0

                withStyle(type.formatting)
                append(type.name.lowercase().replaceFirstChar(Char::uppercase))
                appendGray(": ")
                appendGray(amountSpent.toFormattedString())
                appendGray("/")
                append(amountRequired.toFormattedString()) {
                    this.color = PvColors.DARK_GRAY
                }
                if (amountSpent < amountRequired) {
                    val percentage = (amountSpent.toDouble() / amountRequired) * 100
                    append(" ")
                    appendGray("(${(percentage).round()}%)") {
                        this.color = PvColors.YELLOW
                    }
                }
            }
        }
        addEmpty()
    }

    private fun (MutableComponent.() -> Unit).withColor(color: Int): (MutableComponent.() -> Unit) {
        val init = this
        return {
            this.color = color
            init(this)
        }
    }

    private fun MutableComponent.append(text: String = "", init: MutableComponent.() -> Unit = {}) = this.append(Text.of(text, init))
    private fun MutableComponent.appendGray(text: String = "", init: MutableComponent.() -> Unit = {}) =
        this.append(Text.of(text, init.withColor(PvColors.GRAY)))

    private fun MutableList<Component>.add(text: String = "", init: MutableComponent.() -> Unit = {}) = add(Text.of(text, init))
    private fun MutableList<Component>.addGray(text: String = "", init: MutableComponent.() -> Unit = {}) = add(text, init.withColor(PvColors.GRAY))

    private fun MutableList<Component>.addEmpty() {
        this.add(CommonText.EMPTY)
    }
}
