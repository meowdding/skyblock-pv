package me.owdding.skyblockpv.screens.tabs.rift

import com.mojang.authlib.GameProfile
import earth.terrarium.olympus.client.utils.Orientation
import me.owdding.lib.displays.DisplayWidget
import me.owdding.lib.displays.Displays
import me.owdding.lib.displays.toRow
import me.owdding.lib.displays.withTooltip
import me.owdding.lib.extensions.toReadableString
import me.owdding.lib.extensions.toReadableTime
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.api.data.SkyBlockProfile
import me.owdding.skyblockpv.data.api.RiftData
import me.owdding.skyblockpv.data.repo.RiftCodecs
import me.owdding.skyblockpv.utils.LayoutUtils.asScrollable
import me.owdding.skyblockpv.utils.Utils.append
import me.owdding.skyblockpv.utils.components.PvLayouts
import me.owdding.skyblockpv.utils.components.PvWidgets
import me.owdding.skyblockpv.utils.displays.ExtraDisplays
import me.owdding.skyblockpv.utils.theme.PvColors
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.minecraft.world.item.TooltipFlag
import tech.thatgravyboat.skyblockapi.utils.builders.TooltipBuilder
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.extentions.toTitleCase
import tech.thatgravyboat.skyblockapi.utils.text.CommonText
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import java.time.Instant

class MainRiftScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseRiftScreen(gameProfile, profile) {
    override fun getLayout(bg: DisplayWidget) = PvLayouts.horizontal(5, 0.5f) {
        val rift = profile.rift ?: run {
            string("Failed to load rift profile data") {
                color = PvColors.RED
            }
            return@horizontal
        }

        val trophy = getTrophy(rift, RiftCodecs.data)
        val info = getInformation(profile, rift, RiftCodecs.data)

        if (trophy.width + info.width + 5 > bg.width) {
            widget(
                PvLayouts.vertical(3, 0.5f) {
                    widget(info)
                    widget(trophy)
                }.asScrollable(bg.width, bg.height),
            )
        } else {
            horizontal(3, 0.5f) {
                widget(info)
                widget(trophy)
            }
        }
    }

    private fun getInformation(profile: SkyBlockProfile, rift: RiftData, data: RiftCodecs.RiftRepoData) = PvWidgets.label(
        "Information",
        PvLayouts.vertical {
            string("Motes: ") {
                color = PvColors.DARK_GRAY
                append((profile.currency?.motes ?: 0).toFormattedString()) {
                    color = PvColors.LIGHT_PURPLE
                }
            }
            string("Lifetime Motes: ") {
                color = PvColors.DARK_GRAY
                append(rift.lifetimeMotes.toFormattedString()) {
                    color = PvColors.LIGHT_PURPLE
                }
            }
            string("Visits: ") {
                color = PvColors.DARK_GRAY
                append(rift.visits.toFormattedString()) {
                    color = PvColors.LIGHT_PURPLE
                }
            }
            string("Time sitting with Ävaeìkx: ") {
                color = PvColors.DARK_GRAY
                append(rift.secondsSitting.toReadableTime()) {
                    color = PvColors.DARK_PURPLE
                }
            }
            string("Enigma Souls: ") {
                color = PvColors.DARK_GRAY
                append("${rift.foundSouls.size}") {
                    color = PvColors.DARK_PURPLE
                }
            }
            display(
                ExtraDisplays.text(
                    Text.of("Found Cats: ") {
                        color = PvColors.DARK_GRAY
                        append("${rift.deadCat.foundCats.size}/${data.montezuma.size}") {
                            color = PvColors.DARK_PURPLE
                        }
                    },
                    shadow = false,
                ).withTooltip {
                    val missingCats = data.montezuma.filter { cat ->
                        !rift.deadCat.foundCats.contains(cat)
                    }
                    if (missingCats.isEmpty()) return@withTooltip
                    add("Missing Cats (${missingCats.size}): ") {
                        color = PvColors.GRAY
                    }
                    missingCats.forEach { cat ->
                        add(cat.toTitleCase()) {
                            color = PvColors.DARK_PURPLE
                        }
                    }
                },
            )
            display(
                ExtraDisplays.text(
                    Text.of("Unlocked Eyes: ") {
                        color = PvColors.DARK_GRAY
                        append("${rift.unlockedEyes.size}/${data.eyes.size}") {
                            color = PvColors.DARK_PURPLE
                        }
                    },
                    shadow = false,
                ).withTooltip {
                    val missingEyes = data.eyes.filter { eye ->
                        !rift.unlockedEyes.contains(eye)
                    }
                    if (missingEyes.isEmpty()) return@withTooltip
                    add("Locked Eyes (${missingEyes.size}): ") {
                        color = PvColors.GRAY
                    }
                    missingEyes.forEach { eye ->
                        add(eye.toTitleCase()) {
                            color = PvColors.DARK_PURPLE
                        }
                    }
                },
            )
            string("Grubber Stacks: ") {
                color = PvColors.DARK_GRAY
                append(rift.grubberStacks.toFormattedString()) {
                    color = PvColors.LIGHT_PURPLE
                }
                append("/")
                append("5") {
                    color = PvColors.DARK_PURPLE
                }
            }
        },
        icon = SkyBlockPv.id("icon/item/clipboard"),
    )

    private fun getTrophy(rift: RiftData, data: RiftCodecs.RiftRepoData) = PvWidgets.label(
        "Timecharms",
        PvLayouts.horizontal {
            data.trophies.map { trophy ->
                val ingameTrophy = rift.trophies.find { it.type == trophy.id }
                val unlocked = ingameTrophy != null
                val item = trophy.item.takeIf { unlocked } ?: Items.GRAY_DYE.defaultInstance
                val lore = TooltipBuilder(trophy.item.getTooltipLines(Item.TooltipContext.EMPTY, null, TooltipFlag.NORMAL)).apply {
                    if (!unlocked) return@apply
                    add(CommonText.EMPTY)
                    add("Found after ") {
                        color = PvColors.GRAY
                        append("${ingameTrophy.visits} ") {
                            color = PvColors.GREEN
                        }
                        append("visits")
                    }
                    add("Timestamp: ") {
                        color = PvColors.GRAY
                        append(Instant.ofEpochMilli(ingameTrophy.timestamp).toReadableString()) {
                            color = PvColors.GREEN
                        }
                    }
                }.build()
                Displays.padding(2, Displays.item(item).withTooltip(lore))
            }.toRow().let { display(ExtraDisplays.inventoryBackground(8, Orientation.HORIZONTAL, Displays.padding(2, it))) }
        },
    )
}
