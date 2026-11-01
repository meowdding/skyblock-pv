package me.owdding.skyblockpv.screens.windowed.tabs.farming

import com.mojang.authlib.GameProfile
import me.owdding.lib.builder.DisplayFactory
import me.owdding.lib.displays.Alignment
import me.owdding.lib.displays.DisplayWidget
import me.owdding.lib.displays.Displays
import me.owdding.lib.displays.withTooltip
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.data.api.skills.farming.ActiveCommission
import me.owdding.skyblockpv.data.api.skills.farming.CommissionStatus
import me.owdding.skyblockpv.data.repo.GardenUpgrades
import me.owdding.skyblockpv.utils.components.PvLayouts
import me.owdding.skyblockpv.utils.components.PvWidgets
import me.owdding.skyblockpv.utils.theme.PvColors
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.utils.extentions.cleanName
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.bold
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

class ActiveCommissionsScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseFarmingScreen(gameProfile, profile) {

    override fun getLayout(bg: DisplayWidget): Layout {
        val garden = data?.getOrNull()
        val commissions = garden?.activeCommission?.values?.sortedBy { it.position } ?: emptyList()

        val maxSize = 5 + (garden?.gardenUpgrades?.get(GardenUpgrades.MAX_OCCUPANCY) ?: 0)

        return PvWidgets.label(
            "Active Visitor Queue (${commissions.size}/$maxSize)",
            PvLayouts.vertical(5) {
                if (commissions.isEmpty()) {
                    display(
                        Displays.text(
                            Text.of("No active visitor commissions found!") {
                                this.color = PvColors.RED
                            },
                        ),
                    )
                    return@vertical
                }

                commissions.chunked(2).forEach { rowCommissions ->
                    horizontal(5) {
                        rowCommissions.forEach { commission ->
                            widget(getCommissionWidget(commission))
                        }
                    }
                }
            },
        )
    }

    private fun getCommissionWidget(commission: ActiveCommission): Layout {
        val visitor = commission.visitor
        val itemStack = visitor?.itemStack ?: Items.BARRIER.defaultInstance

        val display = DisplayFactory.vertical {
            horizontal(alignment = Alignment.CENTER) {
                item(itemStack)
                string(visitor?.name ?: "Unknown Visitor") {
                    this.color = visitor?.rarity?.color ?: PvColors.WHITE
                }
            }
        }.withTooltip {
            add(visitor?.name ?: "Unknown Visitor") {
                this.bold = true
                this.color = visitor?.rarity?.color ?: PvColors.WHITE
            }

            space()
            add("Queue Position: ") {
                this.color = PvColors.GRAY
                append("#${commission.position}") {
                    this.color = PvColors.YELLOW
                }
            }

            add("Status: ") {
                this.color = PvColors.GRAY
                append(
                    when (commission.status) {
                        CommissionStatus.STARTED -> "Started"
                        CommissionStatus.NOT_STARTED -> "Not Started"
                    },
                ) {
                    this.color = when (commission.status) {
                        CommissionStatus.STARTED -> PvColors.GREEN
                        CommissionStatus.NOT_STARTED -> PvColors.RED
                    }
                }
            }

            if (commission.requirement.isNotEmpty()) {
                space()
                add("Requirements:") {
                    this.color = PvColors.GOLD
                    this.bold = true
                }
                commission.requirement.forEach { req ->
                    val reqItemName = req.item.toItem().cleanName
                    add("- ") {
                        this.color = PvColors.GRAY
                        append("$reqItemName x${req.amount}") {
                            this.color = PvColors.WHITE
                        }
                    }
                }
            }

            if (commission.extraRewards.isNotEmpty() || commission.bonusRewards.isNotEmpty()) {
                space()
                add("Rewards:") {
                    this.color = PvColors.GOLD
                    this.bold = true
                }
                commission.bonusRewards.forEach { reward ->
                    val rewardName = reward.itemId.toItem().cleanName
                    add("- Bonus: $rewardName") {
                        this.color = PvColors.GREEN
                    }
                }
            }
        }

        return PvWidgets.label(
            "Slot #${commission.position + 1}",
            DisplayWidget(display),
        )
    }
}
