package me.owdding.skyblockpv.screens.windowed.tabs.cf

import com.mojang.authlib.GameProfile
import me.owdding.lib.displays.DisplayWidget
import me.owdding.lib.displays.Displays
import me.owdding.lib.displays.toRow
import me.owdding.lib.displays.withPadding
import me.owdding.lib.displays.withTooltip
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.data.api.CfFaction
import me.owdding.skyblockpv.data.repo.CfCodecs
import me.owdding.skyblockpv.utils.LayoutUtils.asScrollable
import me.owdding.skyblockpv.utils.components.PvLayouts
import me.owdding.skyblockpv.utils.components.PvWidgets
import me.owdding.skyblockpv.utils.displays.ExtraDisplays
import me.owdding.skyblockpv.utils.theme.PvColors
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.extentions.toTitleCase
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color


class FactionCfScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseCfScreen(gameProfile, profile) {
    override fun getLayout(bg: DisplayWidget): Layout = PvLayouts.frame {
        val cf = profile.chocolateFactoryData ?: return@frame

        val factions = CfFaction.entries.map { faction ->
            PvWidgets.label(
                Text.of {
                    append(faction.name.toTitleCase())
                    if (faction == cf.faction) {
                        append(" (")
                        append(cf.factionLevel)
                        append(")")
                    }
                    if (faction == cf.faction) {
                        this.color = PvColors.DARK_GREEN
                    }
                },
                PvLayouts.vertical(alignment = 0.5f) {
                    val factionData = CfCodecs.data.factions.select(faction)

                    factionData.entries.reversed().forEach { (rarity, strings) ->
                        display(
                            ExtraDisplays.inventoryBackground(
                                strings.size, 1,
                                strings.map {
                                    val amount = cf.rabbits.get(it)
                                    Displays.item(if (amount != null) Items.DYE.lime else Items.DYE.gray).withPadding(2).withTooltip {
                                        add(it.toTitleCase(), PvColors.GRAY)
                                        add("Found: ") {
                                            color = PvColors.GRAY
                                            if (amount != null) {
                                                append("Yes", PvColors.GREEN)
                                            } else {
                                                append("No", PvColors.RED)
                                            }
                                        }
                                        if (amount != null) {
                                            add("Total Found: ") {
                                                color = PvColors.GRAY
                                                append(amount.toFormattedString(), PvColors.YELLOW)
                                            }
                                        }
                                    }
                                }.toRow().withPadding(2),
                                color = rarity.skyBlockColor,
                            ),
                        )
                    }
                },
            )
        }
        val factionWidth = factions.maxOf { it.width }
        val factionHeight = factions.maxOf { it.height }

        when {
            factionHeight * 2 + 10 <= bg.height -> {
                horizontal {
                    factions.chunked(2) {
                        vertical(10) {
                            for (layout in it) {
                                widget(layout)
                            }
                        }
                    }
                }
            }

            factionWidth * factions.size + 10 * (factions.size - 1) <= bg.width -> {
                horizontal {
                    for (layout in factions) {
                        widget(layout)
                    }
                }
            }

            factionWidth * factions.size / 2 + 10 * (factions.size / 2 - 1) <= bg.width -> {
                PvLayouts.horizontal {
                    factions.chunked(2) {
                        vertical(10) {
                            for (layout in it) {
                                widget(layout)
                            }
                        }
                    }
                }.asScrollable(bg.width, bg.height).add()
            }

            else -> {
                PvLayouts.vertical(10) {
                    for (layout in factions) {
                        widget(layout)
                    }
                }.asScrollable(bg.width, bg.height).add()
            }
        }
    }
}
