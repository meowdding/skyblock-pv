package tech.thatgravyboat.skyblockpv.screens.tabs.rift

import com.mojang.authlib.GameProfile
import earth.terrarium.olympus.client.utils.Orientation
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.minecraft.world.item.TooltipFlag
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.text.CommonText
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.data.RiftCodecs
import tech.thatgravyboat.skyblockpv.data.RiftData
import tech.thatgravyboat.skyblockpv.utils.LayoutBuild
import tech.thatgravyboat.skyblockpv.utils.LayoutUtils.asScrollable
import tech.thatgravyboat.skyblockpv.utils.Utils.append
import tech.thatgravyboat.skyblockpv.utils.Utils.formatReadableTime
import tech.thatgravyboat.skyblockpv.utils.Utils.toReadableString
import tech.thatgravyboat.skyblockpv.utils.Utils.toTitleCase
import tech.thatgravyboat.skyblockpv.utils.components.PvWidgets
import tech.thatgravyboat.skyblockpv.utils.displays.*
import java.time.Instant

class MainRiftScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseRiftScreen(gameProfile, profile) {
    override fun getLayout(bg: DisplayWidget) = LayoutBuild.horizontal(5, 0.5f) {
        val rift = profile?.rift ?: run {
            string("Failed to load rift profile data") {
                color = TextColor.RED
            }
            return@horizontal
        }
        val data = RiftCodecs.data ?: run {
            string("Failed to load rift data") {
                color = TextColor.RED
            }
            return@horizontal
        }

        val trophy = getTrophy(rift, data)
        val info = getInformation(profile!!, data)

        if (trophy.width + info.width + 5 > bg.width) {
            widget(
                LayoutBuild.vertical(3, 0.5f) {
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

    private fun getInformation(profile: SkyBlockProfile, data: RiftCodecs.RiftRepoData) = PvWidgets.label(
        "Information",
        LayoutBuild.vertical {
            val rift = profile.rift!!

            string("Motes: ") {
                color = TextColor.DARK_GRAY
                append((profile.currency?.motes ?: 0).toFormattedString()) {
                    color = TextColor.LIGHT_PURPLE
                }
            }
            string("Lifetime Motes: ") {
                color = TextColor.DARK_GRAY
                append(rift.lifetimeMotes.toFormattedString()) {
                    color = TextColor.LIGHT_PURPLE
                }
            }
            string("Visits: ") {
                color = TextColor.DARK_GRAY
                append(rift.visits.toFormattedString()) {
                    color = TextColor.LIGHT_PURPLE
                }
            }
            string("Time sitting with Ävaeìkx: ") {
                color = TextColor.DARK_GRAY
                append(rift.secondsSitting.formatReadableTime()) {
                    color = TextColor.DARK_PURPLE
                }
            }
            string("Enigma Souls: ") {
                color = TextColor.DARK_GRAY
                append("${rift.foundSouls.size}") {
                    color = TextColor.DARK_PURPLE
                }
            }
            display(
                Displays.text(
                    Text.of("Found Cats: ") {
                        color = TextColor.DARK_GRAY
                        append("${rift.deadCat.foundCats.size}/${data.montezuma.size}") {
                            color = TextColor.DARK_PURPLE
                        }
                    },
                    shadow = false,
                ).withTooltip {
                    val missingCats = data.montezuma.filter { cat ->
                        !rift.deadCat.foundCats.contains(cat)
                    }
                    if (missingCats.isEmpty()) return@withTooltip
                    add("Missing Cats (${missingCats.size}): ") {
                        color = TextColor.GRAY
                    }
                    missingCats.forEach { cat ->
                        add(cat.toTitleCase()) {
                            color = TextColor.DARK_PURPLE
                        }
                    }
                },
            )
            display(
                Displays.text(
                    Text.of("Unlocked Eyes: ") {
                        color = TextColor.DARK_GRAY
                        append("${rift.unlockedEyes.size}/${data.eyes.size}") {
                            color = TextColor.DARK_PURPLE
                        }
                    },
                    shadow = false,
                ).withTooltip {
                    val missingEyes = data.eyes.filter { eye ->
                        !rift.unlockedEyes.contains(eye)
                    }
                    if (missingEyes.isEmpty()) return@withTooltip
                    add("Locked Eyes (${missingEyes.size}): ") {
                        color = TextColor.GRAY
                    }
                    missingEyes.forEach { eye ->
                        add(eye.toTitleCase()) {
                            color = TextColor.DARK_PURPLE
                        }
                    }
                },
            )
        },
    )

    private fun getTrophy(rift: RiftData, data: RiftCodecs.RiftRepoData) = PvWidgets.label(
        "Timecharms",
        LayoutBuild.horizontal {
            data.trophies.map { trophy ->
                val ingameTrophy = rift.trophies.find { it.type == trophy.id }
                val unlocked = ingameTrophy != null
                val item = trophy.item.takeIf { unlocked } ?: Items.GRAY_DYE.defaultInstance
                val lore = TooltipBuilder(trophy.item.getTooltipLines(Item.TooltipContext.EMPTY, null, TooltipFlag.NORMAL)).apply {
                    if (!unlocked) return@apply
                    add(CommonText.EMPTY)
                    add("Found after ") {
                        color = TextColor.GRAY
                        append("${ingameTrophy.visits} ") {
                            color = TextColor.GREEN
                        }
                        append("visits")
                    }
                    add("Timestamp: ") {
                        color = TextColor.GRAY
                        append(Instant.ofEpochMilli(ingameTrophy.timestamp).toReadableString()) {
                            color = TextColor.GREEN
                        }
                    }
                }.build()
                Displays.padding(2, Displays.item(item).withTooltip(lore))
            }.toRow().let { display(Displays.inventoryBackground(8, Orientation.HORIZONTAL, Displays.padding(2, it))) }
        },
    )
}
