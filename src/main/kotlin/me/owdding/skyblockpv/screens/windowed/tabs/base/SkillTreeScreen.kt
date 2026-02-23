package me.owdding.skyblockpv.screens.windowed.tabs.base

import me.owdding.lib.displays.Display
import me.owdding.lib.displays.DisplayWidget
import me.owdding.lib.displays.Displays
import me.owdding.lib.displays.asWidget
import me.owdding.lib.displays.withTooltip
import me.owdding.lib.extensions.round
import me.owdding.lib.repo.AbilityTreeNode
import me.owdding.lib.repo.Context
import me.owdding.lib.repo.CoreTreeNode
import me.owdding.lib.repo.CostType
import me.owdding.lib.repo.FreeCostType
import me.owdding.lib.repo.LevelableTreeNode
import me.owdding.lib.repo.SpacerNode
import me.owdding.lib.repo.TierNode
import me.owdding.lib.repo.TreeNode
import me.owdding.skyblockpv.data.api.skills.SkillTree
import me.owdding.skyblockpv.utils.LayoutUtils.asScrollable
import me.owdding.skyblockpv.utils.LayoutUtils.withScrollToBottom
import me.owdding.skyblockpv.utils.components.PvLayouts
import me.owdding.skyblockpv.utils.debugToggle
import me.owdding.skyblockpv.utils.displays.ExtraDisplays
import me.owdding.skyblockpv.utils.theme.PvColors
import net.minecraft.client.gui.layouts.GridLayout
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.client.gui.layouts.SpacerElement
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.world.item.Item
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

val enableDebugOrdering by debugToggle("skill_tree/order_debug", "Removes the default ordering from skill tree nodes.")

data class TierNodeItems(
    val locked: Item,
    val unlocked: Item,
    val unlocking: Item,
) {
    fun select(treeNode: TierNode, level: Int): Item = when {
        treeNode.isMaxed(level) -> this.unlocked
        treeNode.isMaxed(level + 1) -> this.unlocking
        else -> this.locked
    }
}

data class CoreNodeItems(
    val maxLevel: Item,
    val unlocked: Item,
    val locked: Item,
    val leveling: Item,
) {
    fun select(node: CoreTreeNode, level: Int) = when {
        node.isMaxed(level) -> this.maxLevel
        level < 0 -> this.locked
        level == 1 -> this.unlocked
        else -> leveling
    }
}

data class AbilityNodeItems(
    val selected: Item,
    val unlocked: Item,
    val locked: Item,
) {
    fun select(node: AbilityTreeNode, tree: SkillTree, level: Int) = when {
        tree.selectedAbility == node.id -> this.selected
        level > 0 -> this.unlocked
        else -> this.locked
    }
}

data class NodeItems(
    val disabled: Item,
    val unlocked: Item,
    val maxLevel: Item,
    val locked: Item,
) {
    fun select(node: TreeNode, level: Int, disabled: Boolean) = when {
        disabled -> this.disabled
        level < 0 -> this.locked
        node.isMaxed(level) -> this.maxLevel
        else -> unlocked
    }
}

data class SkillTreeItems(
    val tierItems: TierNodeItems,
    val coreItems: CoreNodeItems,
    val abilityItems: AbilityNodeItems,
    val nodeItems: NodeItems,
) {
    companion object {
        private val tierItems = TierNodeItems(
            locked = Items.RED_STAINED_GLASS_PANE,
            unlocked = Items.GREEN_STAINED_GLASS_PANE,
            unlocking = Items.YELLOW_STAINED_GLASS_PANE,
        )

        val MINING = SkillTreeItems(
            tierItems = tierItems,
            coreItems = CoreNodeItems(
                maxLevel = Items.DIAMOND_BLOCK,
                unlocked = Items.COPPER_BLOCK,
                leveling = Items.REDSTONE_BLOCK,
                locked = Items.BEDROCK,
            ),
            abilityItems = AbilityNodeItems(
                selected = Items.EMERALD_BLOCK,
                unlocked = Items.REDSTONE_BLOCK,
                locked = Items.COAL_BLOCK,
            ),
            nodeItems = NodeItems(
                disabled = Items.REDSTONE,
                maxLevel = Items.DIAMOND,
                unlocked = Items.EMERALD,
                locked = Items.COAL,
            ),
        )

        val FORAGING = SkillTreeItems(
            tierItems = tierItems,
            coreItems = CoreNodeItems(
                maxLevel = Items.OAK_WOOD,
                unlocked = Items.STRIPPED_BIRCH_WOOD,
                leveling = Items.STRIPPED_OAK_WOOD,
                locked = Items.STRIPPED_PALE_OAK_WOOD,
            ),
            abilityItems = AbilityNodeItems(
                // hypixel uses oak sapling for both selected and unlocked and just enchants the selected one, think that's not very obvious
                selected = Items.OAK_SAPLING,
                unlocked = Items.CHERRY_SAPLING,
                locked = Items.PALE_OAK_SAPLING,
            ),
            nodeItems = NodeItems(
                disabled = Items.STRIPPED_MANGROVE_LOG,
                maxLevel = Items.OAK_LOG,
                unlocked = Items.STRIPPED_OAK_LOG,
                locked = Items.PALE_OAK_BUTTON,
            ),
        )
    }

    fun select(treeNode: TreeNode, tree: SkillTree, level: Int, disabled: Boolean): Item = when (treeNode) {
        is TierNode -> tierItems.select(treeNode, level)
        is CoreTreeNode -> coreItems.select(treeNode, level)
        is AbilityTreeNode -> abilityItems.select(treeNode, tree, level)
        else -> nodeItems.select(treeNode, level, disabled)
    }
}

interface SkillTreeScreen {
    val treeType: String
    val coreNode: String
    val items: SkillTreeItems
    fun skillTree(): SkillTree?
    fun nodes(): List<TreeNode>

    fun createLayout(bg: DisplayWidget): Layout {
        val skillTree = skillTree() ?: return PvLayouts.empty()
        val gridLayout = GridLayout()

        // nodes that are unlocked but not in the repo:
        val unknownNodes = skillTree.nodes.keys - nodes().map { it.id }.toSet()

        if (unknownNodes.isNotEmpty()) {
            println("Unknown $treeType nodes: $unknownNodes")
        }

        nodes().forEachIndexed { index, node ->
            if (node is SpacerNode) {
                if (enableDebugOrdering) return@forEachIndexed
                gridLayout.addChild(
                    SpacerElement(node.size.x, node.size.y),
                    9 - node.location.y,
                    node.location.x + 2,
                )
                return@forEachIndexed
            }

            val level = (skillTree.nodes[node.id] ?: -1).let {
                if (node is AbilityTreeNode && it != -1) {
                    return@let skillTree.getAbilityLevel(coreNode)
                }
                if (node is TierNode) {
                    return@let skillTree.getTreeLevel()
                }

                it
            }
            val disabled = skillTree.disabled.contains(node.id).let {
                if (node is AbilityTreeNode) {
                    return@let skillTree.selectedAbility != node.id
                }
                return@let it
            }
            if (enableDebugOrdering) {
                gridLayout.addChild(
                    ExtraDisplays.inventorySlot(Displays.padding(2, getNode(node, level, disabled, skillTree))).asWidget(),
                    index / 9,
                    index % 9,
                )
            } else {
                gridLayout.addChild(
                    ExtraDisplays.inventorySlot(Displays.padding(2, getNode(node, level, disabled, skillTree))).asWidget(),
                    9 - node.location.y,
                    node.location.x + 2,
                )
            }

        }

        return gridLayout.asScrollable(bg.width, bg.height) {
            withScrollToBottom()
        }
    }

    private fun getNode(treeNode: TreeNode, level: Int, disabled: Boolean, tree: SkillTree): Display {
        val item = items.select(treeNode, tree, level, disabled)

        val tooltip = getTooltip(treeNode, level, disabled, tree)
        return Displays.item(item.defaultInstance, customStackText = if (level > 1 && treeNode !is TierNode) "$level" else "").withTooltip(
            Text.multiline(
                listOfNotNull(
                    tooltip,
                    if (enableDebugOrdering) Text.of {
                        append("x: ${treeNode.location.x}, y: ${treeNode.location.y}")
                    } else null,
                ),
            ),
        )
    }

    private fun getTierNodeTooltip(tierNode: TierNode, level: Int, tree: SkillTree) = buildList<Component> {
        val hasUnlocked = tierNode.isMaxed(level)
        val isCurrentlyUnlocking = tierNode.isMaxed(level + 1)

        add(tierNode.name) {
            if (hasUnlocked) {
                this.color = PvColors.GREEN
            } else if (isCurrentlyUnlocking) {
                this.color = PvColors.YELLOW
            } else {
                this.color = PvColors.RED
            }
        }

        if (!hasUnlocked && isCurrentlyUnlocking) {
            val xpToNextLevel = tree.getXpToNextLevel()
            val xpRequiredForNextLevel = tree.getXpRequiredForNextLevel()

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
        addAll(tierNode.tooltip(Context()))

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
            add("Requires Tier ${tierNode.location.y}") { this.color = PvColors.RED }
        }
    }

    private fun getTooltip(treeNode: TreeNode, level: Int, disabled: Boolean, tree: SkillTree) = buildList {
        if (treeNode is TierNode) {
            addAll(getTierNodeTooltip(treeNode, level, tree))
            return@buildList
        }

        add(treeNode.name) {
            if (disabled || level == -1) {
                this.color = PvColors.RED
            } else {
                this.color = PvColors.GREEN
            }
        }

        if (treeNode is LevelableTreeNode) {
            if (level == treeNode.maxLevel) {
                addGray("Level $level")
            } else {
                addGray("Level ${level.absoluteValue}/") {
                    append("${treeNode.maxLevel}") {
                        this.color = PvColors.DARK_GRAY
                    }
                }
            }
            addEmpty()
        }

        addAll(treeNode.tooltip(Context(tree.getTreeLevel(), level)))
        when (treeNode) {
            is AbilityTreeNode -> {
                if (!disabled) {
                    addEmpty()
                    addGray("SELECTED") {
                        this.color = PvColors.GREEN
                        this.bold = true
                    }
                }
            }

            !is CoreTreeNode -> {
                if (level <= 0) {
                    return@buildList
                }

                addEmpty()
                if (treeNode is LevelableTreeNode) {
                    addAll(getPowderSpent(treeNode, level))
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

    private fun getPowderSpent(levelableTreeNode: LevelableTreeNode, level: Int): List<Component> = buildList {
        fun getAmountUntil(level: Int): Map<CostType, Int> {
            return (1 until level).map { levelableTreeNode.costForLevel(it) }.groupBy { it.first }.filterNot { (key) -> key is FreeCostType }
                .mapValues { it.value.sumOf { it.second } }
        }

        val totalAmountRequired = getAmountUntil(levelableTreeNode.maxLevel + ((1).takeIf { levelableTreeNode is CoreTreeNode } ?: 0))
        val amountSpend = if (level == levelableTreeNode.maxLevel) {
            totalAmountRequired
        } else {
            getAmountUntil(level + ((1).takeIf { levelableTreeNode is CoreTreeNode } ?: 0))
        }


        addGray("Powder Spent")
        totalAmountRequired.forEach { (type, amountRequired) ->
            addGray {
                val amountSpent = amountSpend[type] ?: 0

                withStyle(type.formatting!!)
                append(type.displayName!!.string.lowercase().replaceFirstChar(Char::uppercase))
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
    private fun MutableList<Component>.addGray(text: String = "", init: MutableComponent.() -> Unit = {}) = add(
        text,
        init.withColor(
            PvColors.GRAY,
        ),
    )

    private fun MutableList<Component>.addEmpty() {
        this.add(CommonText.EMPTY)
    }
}
