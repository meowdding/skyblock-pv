package tech.thatgravyboat.skyblockpv.screens.tabs.mining

import com.mojang.authlib.GameProfile
import net.minecraft.ChatFormatting
import net.minecraft.client.gui.layouts.GridLayout
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.network.chat.MutableComponent
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.data.*
import tech.thatgravyboat.skyblockpv.utils.LayoutBuild
import tech.thatgravyboat.skyblockpv.utils.displays.Display
import tech.thatgravyboat.skyblockpv.utils.displays.Displays
import tech.thatgravyboat.skyblockpv.utils.displays.asWidget
import tech.thatgravyboat.skyblockpv.utils.displays.withTooltip
import kotlin.math.absoluteValue

class HotmScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseMiningScreen(gameProfile, profile) {
    override fun getLayout(): Layout {
        val mining = profile?.mining ?: return LayoutBuild.horizontal { }
        val gridLayout = GridLayout()

        MiningNodes.miningNodes.forEach { node ->
            gridLayout.addChild(
                getNode(node, mining.nodes[node.id] ?: -1, mining.toggledNodes.contains(node.id), mining).asWidget(),
                9 - node.location.y,
                node.location.x,
            )
        }

        return gridLayout
    }

    fun getNode(miningNode: MiningNode, level: Int, toggled: Boolean, miningCore: MiningCore): Display {
        val item = if (toggled) {
            Items.REDSTONE
        } else if (miningNode is LevelableMiningNode && !miningNode.isMaxed(level) && level != -1) {
            Items.EMERALD
        } else if (miningNode.isMaxed(level)) {
            Items.DIAMOND
        } else {
            Items.COAL
        }

        fun grayText(text: String = "", init: MutableComponent.() -> Unit = {}) = Text.of(text) {
            withStyle(ChatFormatting.GRAY)
            init(this)
        }

        return Displays.item(item.defaultInstance, customStackText = if (level != -1) "$level" else "").withTooltip(
            Text.of(miningNode.name) {
                if (toggled || level == -1) {
                    withStyle(ChatFormatting.RED)
                } else {
                    withStyle(ChatFormatting.GREEN)
                }
            },

            buildList {
                if (miningNode is LevelingMiningNode) {
                    if (level == miningNode.maxLevel) {
                        add(grayText("Level $level"))
                    } else {
                        add(grayText("Level ${level.absoluteValue}/") {
                            append(grayText("${miningNode.maxLevel}") {
                                withStyle(ChatFormatting.DARK_GRAY)
                            })
                        })
                    }
                    add(grayText(""))
                }

                addAll(miningNode.tooltip(Context(miningCore.getHotmLevel(), level)))

                if (level > 0) {
                    add(grayText(""))
                    if (toggled) {
                        add(grayText("DISABLED") {
                            withStyle(ChatFormatting.RED, ChatFormatting.BOLD)
                        })
                    } else {
                        add(grayText("ENABLED") {
                            withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)
                        })
                    }
                }
            }
        )
    }
}
