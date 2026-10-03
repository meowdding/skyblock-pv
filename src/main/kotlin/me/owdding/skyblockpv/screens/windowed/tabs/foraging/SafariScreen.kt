package me.owdding.skyblockpv.screens.windowed.tabs.foraging

import com.mojang.authlib.GameProfile
import me.owdding.lib.displays.DisplayWidget
import me.owdding.lib.displays.Displays
import me.owdding.lib.displays.withTooltip
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.data.api.skills.SafariData
import me.owdding.skyblockpv.data.repo.EssenceData.addSafariPerk
import me.owdding.skyblockpv.data.repo.SafariCodecs
import me.owdding.skyblockpv.utils.LayoutUtils.asScrollable
import me.owdding.skyblockpv.utils.components.PvLayouts
import me.owdding.skyblockpv.utils.components.PvWidgets
import me.owdding.skyblockpv.utils.displays.ExtraDisplays
import me.owdding.skyblockpv.utils.theme.PvColors
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.client.gui.layouts.LayoutElement
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.text.CommonText
import tech.thatgravyboat.skyblockapi.utils.text.SkyBlockColor
import tech.thatgravyboat.skyblockapi.utils.text.Text

class SafariScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseForagingScreen(gameProfile, profile) {
    override val type: ForagingCategory = ForagingCategory.SAFARI

    override fun getLayout(bg: DisplayWidget): Layout {
        val safari = profile.safari ?: run {
            return PvLayouts.vertical(alignment = 0.5f) {
                display(ExtraDisplays.text(Text.of("No Safari Data found for this profile!", PvColors.RED)))
            }
        }

        return if (uiWidth < 400) {
            val columnWidth = uiWidth - 10

            PvLayouts.vertical(10) {
                widget(getTicketsWidget(safari, columnWidth))
                widget(getMilestonesWidget(safari, columnWidth))
                widget(getCrittersWidget(safari, columnWidth))
                widget(getEssenceWidget(columnWidth))
            }
        } else {
            val columnWidth = (uiWidth - 30) / 2

            PvLayouts.horizontal(10) {
                spacer(width = 5)
                vertical(5) {
                    widget(getTicketsWidget(safari, columnWidth))
                    widget(getMilestonesWidget(safari, columnWidth))
                }
                vertical(5) {
                    widget(getCrittersWidget(safari, columnWidth))
                    widget(getEssenceWidget(columnWidth))
                }
                spacer(width = 5)
            }
        }.asScrollable(uiWidth, uiHeight)
    }

    private fun getEssenceWidget(width: Int): LayoutElement = PvWidgets.label(
        "Essence Perks",
        PvLayouts.vertical(2) {
            val perks = listOf(
                "critter_catcher", "critter_master", "floortunate", "fresh_footprints",
                "head_start", "hunting_hotspot", "thawing", "deep_diver",
                "quickdraw", "amateur_hour", "sparkling_specialist",
            )

            val elementsPerRow = (width - 15) / 22
            perks.chunked(elementsPerRow).forEach { rowPerks ->
                horizontal(2) {
                    rowPerks.forEach { perk ->
                        addSafariPerk(profile, perk)
                    }
                }
            }
        },
        width = width,
    )

    private fun getTicketsWidget(safari: SafariData, width: Int): LayoutElement = PvWidgets.label(
        "Safari Tickets",
        PvLayouts.vertical(3) {
            SafariCodecs.SafariTicket.entries.forEach { ticket ->
                val amount = safari.tickets[ticket] ?: 0
                horizontal {
                    display(ExtraDisplays.text(Text.of("${ticket.formattedName}: ", ticket.color)))
                    display(ExtraDisplays.text(Text.of(amount.toFormattedString(), PvColors.YELLOW)))
                }
            }
        },
        width = width,
    )

    private fun getMilestonesWidget(safari: SafariData, width: Int): LayoutElement = PvWidgets.label(
        "Biome Captures & Milestones",
        PvLayouts.vertical(5) {
            SafariCodecs.CritterSafariBiome.entries.forEach { biome ->
                val captures = safari.biomeCaptures[biome] ?: 0
                val milestoneTier = safari.milestone[biome] ?: 0

                val nextMilestoneReq = SafariCodecs.data.milestones.getOrNull(milestoneTier + 1)
                val progressText = if (nextMilestoneReq != null) {
                    Text.of(" ($captures/$nextMilestoneReq)", PvColors.GRAY)
                } else {
                    Text.of(" (Maxed)", PvColors.GOLD)
                }

                vertical(1) {
                    display(ExtraDisplays.text(biome.component))
                    horizontal {
                        spacer(width = 5)
                        display(ExtraDisplays.text(Text.of("Captures: ", PvColors.GRAY)))
                        display(ExtraDisplays.text(Text.of(captures.toFormattedString(), PvColors.YELLOW)))
                        display(ExtraDisplays.text(progressText))
                    }
                    horizontal {
                        spacer(width = 5)
                        display(ExtraDisplays.text(Text.of("Milestone Tier: ", PvColors.GRAY)))
                        display(ExtraDisplays.text(Text.of(milestoneTier.toString(), PvColors.AQUA)))
                    }
                }
            }
        },
        width = width,
    )

    private fun getCrittersWidget(safari: SafariData, width: Int): LayoutElement = PvWidgets.label(
        "Discovered Critters",
        PvLayouts.vertical(5) {
            val totalDiscovered = safari.discoveredCritters.size
            val totalAvailable = SafariCodecs.data.critters.size

            display(ExtraDisplays.text(Text.of("Total Discovered: $totalDiscovered / $totalAvailable", PvColors.YELLOW)))
            display(ExtraDisplays.text(Text.of("Total Sparkling: ${safari.totalSparkling.toFormattedString()}", PvColors.LIGHT_PURPLE)))
            spacer(height = 2)

            SafariCodecs.CritterSafariBiome.entries.forEach { biome ->
                val crittersInBiome = SafariCodecs.data.critters.filter { it.biome == biome }
                if (crittersInBiome.isEmpty()) return@forEach

                vertical(2) {
                    display(ExtraDisplays.text(biome.component))

                    val critterDisplays = crittersInBiome.map { critter ->
                        val isDiscovered = safari.discoveredCritters.contains(critter.id)
                        val isSparkling = safari.discoveredSparklingCritters.contains(critter.id)

                        val inventoryDisplay = if (isDiscovered) {
                            val itemDisplay = Displays.padding(2, Displays.item(critter.attribute.toItem()))

                            val display = if (!isSparkling) itemDisplay
                            else Displays.layered(
                                Displays.padding(2, Displays.item(critter.attribute.toItem())),
                                Displays.padding(12, 0, 0, 0, Displays.text(Text.of("★", SkyBlockColor.GOLD))),
                            )

                            ExtraDisplays.inventorySlot(display, biome.color)
                        } else {
                            ExtraDisplays.inventorySlot(Displays.padding(2, Displays.item(Items.DYE.gray)), PvColors.DARK_GRAY)
                        }

                        inventoryDisplay.withTooltip {
                            add(Text.of(critter.name, if (isDiscovered) biome.color else PvColors.RED))
                            add(CommonText.EMPTY)
                            if (isSparkling) {
                                add(Text.of("Discovered (Sparkling)", PvColors.LIGHT_PURPLE))
                            } else {
                                add(Text.of(if (isDiscovered) "Discovered" else "Undiscovered", PvColors.GRAY))
                            }
                        }
                    }

                    val elementsPerRow = (width - 15) / 22
                    critterDisplays.chunked(elementsPerRow).forEach { rowDisplays ->
                        horizontal(2) {
                            rowDisplays.forEach { display(it) }
                        }
                    }
                }
            }
        },
        width = width,
    )
}
