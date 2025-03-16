package tech.thatgravyboat.skyblockpv.screens.tabs.mining

import com.mojang.authlib.GameProfile
import net.minecraft.ChatFormatting
import net.minecraft.client.gui.layouts.GridLayout
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.client.gui.layouts.SpacerElement
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockpv.SkyBlockPv
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.data.*
import tech.thatgravyboat.skyblockpv.utils.LayoutBuild
import tech.thatgravyboat.skyblockpv.utils.displays.Display
import tech.thatgravyboat.skyblockpv.utils.displays.Displays
import tech.thatgravyboat.skyblockpv.utils.displays.asWidget
import tech.thatgravyboat.skyblockpv.utils.displays.withTooltip
import java.text.DecimalFormat
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

class HotmScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseMiningScreen(gameProfile, profile) {
    private val decimalFormat = DecimalFormat("#.##")

    override fun getLayout(): Layout {
        val mining = profile?.mining ?: return LayoutBuild.horizontal { }
        val gridLayout = GridLayout()

        gridLayout.addChild(SpacerElement(5, 5), 1, 1)

        MiningNodes.miningNodes.forEach { node ->
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
                Displays.background(
                    SkyBlockPv.id("inventory/inventory-1x1"),
                    Displays.padding(
                        2,
                        getNode(node, level, disabled, mining),
                    ),
                ).asWidget(),
                9 - node.location.y,
                node.location.x + 2,
            )
        }

        return gridLayout
    }

    private fun getNode(miningNode: MiningNode, level: Int, disabled: Boolean, miningCore: MiningCore): Display {
        val item = when (miningNode) {
            is TierNode if miningNode.isMaxed(level) -> Items.GREEN_STAINED_GLASS_PANE
            is TierNode if miningNode.isMaxed(level + 1) -> Items.YELLOW_STAINED_GLASS_PANE
            is TierNode -> Items.RED_STAINED_GLASS_PANE
            is CoreMiningNode if level == 10 -> Items.DIAMOND_BLOCK
            is CoreMiningNode if level < 0 -> Items.COAL_BLOCK
            is CoreMiningNode if level == 1 -> Items.IRON_BLOCK
            is CoreMiningNode -> Items.REDSTONE_BLOCK
            is AbilityMiningNode if miningCore.miningAbility == miningNode.id -> Items.EMERALD_BLOCK
            is AbilityMiningNode if level > 0 -> Items.REDSTONE_BLOCK
            is AbilityMiningNode -> Items.COAL_BLOCK
            else -> {
                if (disabled) {
                    Items.REDSTONE
                } else if (!miningNode.isMaxed(level) && level != -1) {
                    Items.EMERALD
                } else if (miningNode.isMaxed(level)) {
                    Items.DIAMOND
                } else {
                    Items.COAL
                }
            }
        }

        return Displays.item(item.defaultInstance, customStackText = if (level > 1 && miningNode !is TierNode) "$level" else "").withTooltip(
            getTooltip(miningNode, level, disabled, miningCore),
        )
    }

    private fun getTierNodeTooltip(node: TierNode, level: Int, miningCore: MiningCore) = buildList<Component> {
        val hasUnlocked = node.isMaxed(level)
        val isCurrentlyUnlocking = node.isMaxed(level + 1)

        add(
            grayText(node.name) {
                if (hasUnlocked) {
                    withStyle(ChatFormatting.GREEN)
                } else if (isCurrentlyUnlocking) {
                    withStyle(ChatFormatting.YELLOW)
                } else {
                    withStyle(ChatFormatting.RED)
                }
            },
        )

        if (!hasUnlocked && isCurrentlyUnlocking) {
            val xpToNextLevel = miningCore.getXpToNextLevel()
            val xpRequiredForNextLevel = miningCore.getXpRequiredForNextLevel()

            add(grayText())
            val progress = (xpToNextLevel.toFloat() / xpRequiredForNextLevel)
            add(
                grayText("Progress: ") {
                    append(
                        Text.of(decimalFormat.format(progress * 100)) {
                            append("%")
                            withStyle(ChatFormatting.YELLOW)
                        },
                    )
                },
            )

            val amountGreen = (25 * progress).roundToInt()
            val amountWhite = 25 - amountGreen

            add(
                grayText {
                    append(
                        Text.of(" ".repeat(amountGreen)) {
                            withStyle(ChatFormatting.GREEN, ChatFormatting.STRIKETHROUGH)
                        },
                    )
                    append(
                        Text.of(" ".repeat(amountWhite)) {
                            withStyle(ChatFormatting.WHITE, ChatFormatting.STRIKETHROUGH)
                        },
                    )
                    append(" ")
                    append(
                        Text.of(xpToNextLevel.toFormattedString()) {
                            withStyle(ChatFormatting.YELLOW)
                        },
                    )
                    append(
                        Text.of("/") {
                            withStyle(ChatFormatting.GOLD)
                        },
                    )
                    append(
                        Text.of(DecimalFormat.getCompactNumberInstance().format(xpRequiredForNextLevel)) {
                            withStyle(ChatFormatting.YELLOW)
                        },
                    )
                },
            )
        }

        add(grayText())
        add(grayText("Rewards"))
        addAll(node.tooltip(Context()))

        add(grayText())
        if (hasUnlocked) {
            add(Text.of("UNLOCKED").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
        } else if (isCurrentlyUnlocking) {
            add(Text.of("LOCKED").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
        } else {
            add(grayText("Requires Tier ${node.location.y}").withStyle(ChatFormatting.RED))
        }
    }

    private fun getTooltip(miningNode: MiningNode, level: Int, disabled: Boolean, miningCore: MiningCore) = buildList {
        if (miningNode is TierNode) {
            addAll(getTierNodeTooltip(miningNode, level, miningCore))
            return@buildList
        }

        add(
            Text.of(miningNode.name) {
                if (disabled || level == -1) {
                    withStyle(ChatFormatting.RED)
                } else {
                    withStyle(ChatFormatting.GREEN)
                }
            },
        )

        if (miningNode is LevelableMiningNode) {
            if (level == miningNode.maxLevel) {
                add(grayText("Level $level"))
            } else {
                add(
                    grayText("Level ${level.absoluteValue}/") {
                        append(
                            grayText("${miningNode.maxLevel}") {
                                withStyle(ChatFormatting.DARK_GRAY)
                            },
                        )
                    },
                )
            }
            add(grayText(""))
        }

        addAll(miningNode.tooltip(Context(miningCore.getHotmLevel(), level)))
        when (miningNode) {
            is AbilityMiningNode -> {
                if (!disabled) {
                    add(grayText(""))
                    add(
                        grayText("SELECTED") {
                            withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)
                        },
                    )
                }
            }

            else if level > 0 -> {
                add(grayText(""))
                if (miningNode is LevelableMiningNode) {
                    addAll(getPowderSpent(miningNode, level))
                }

                if (disabled) {
                    add(
                        grayText("DISABLED") {
                            withStyle(ChatFormatting.RED, ChatFormatting.BOLD)
                        },
                    )
                } else {
                    add(
                        grayText("ENABLED") {
                            withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)
                        },
                    )
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


        add(grayText("Powder Spent"))
        totalAmountRequired.forEach { (type, amountRequired) ->
            add(
                grayText {
                    val amountSpent = amountSpend[type] ?: 0

                    withStyle(type.formatting)
                    append(type.name.lowercase().replaceFirstChar(Char::uppercase))
                    append(grayText(": "))
                    append(
                        grayText(amountSpent.toFormattedString()) {
                            withStyle(ChatFormatting.GRAY)
                        },
                    )
                    append(grayText("/"))
                    append(
                        grayText(amountRequired.toFormattedString()) {
                            withStyle(ChatFormatting.DARK_GRAY)
                        },
                    )
                    if (amountSpent < amountRequired) {

                        val percentage = (amountSpent.toDouble() / amountRequired) * 100
                        append(" ")
                        append(
                            grayText("(${decimalFormat.format(percentage)}%)") {
                                withStyle(ChatFormatting.YELLOW)
                            },
                        )
                    }
                },
            )
        }
        add(grayText())
    }

    private fun grayText(text: String = "", init: MutableComponent.() -> Unit = {}) = Text.of(text) {
        withStyle(ChatFormatting.GRAY)
        init(this)
    }
}
