package me.owdding.skyblockpv.screens.tabs.farming

import com.mojang.authlib.GameProfile
import me.owdding.lib.builder.LayoutFactory
import me.owdding.lib.displays.*
import me.owdding.skyblockpv.api.data.SkyBlockProfile
import me.owdding.skyblockpv.data.api.skills.farming.Commission
import me.owdding.skyblockpv.data.repo.StaticGardenData
import me.owdding.skyblockpv.data.repo.StaticVisitorData
import me.owdding.skyblockpv.utils.LayoutUtils.asScrollable
import me.owdding.skyblockpv.utils.Utils.append
import me.owdding.skyblockpv.utils.displays.ExtraDisplays
import me.owdding.skyblockpv.utils.theme.PvColors
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.api.data.SkyBlockRarity
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

class VisitorScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseFarmingScreen(gameProfile, profile) {
    override fun getLayout(bg: DisplayWidget): Layout {
        val visitors = data?.map { it.commissionData.commissions.associateBy { it.visitor } }?.getOrNull() ?: emptyMap()

        return StaticGardenData.visitors
            .groupBy { it.rarity }
            .map { it.value }
            .map {
                it.chunked(uiWidth / 24).map {
                    it.map {
                        ExtraDisplays.inventorySlot(
                            Displays.padding(2, toDisplay(it, visitors[it])),
                            this.getRarity(it.rarity, visitors[it]),
                        )
                    }.toRow(0, Alignment.CENTER)
                }.toColumn(0, Alignment.CENTER)
            }.toColumn(4, Alignment.CENTER)
            .let { LayoutFactory.frame { display(it) }.asScrollable(uiWidth, uiHeight) }
    }

    private fun getRarity(rarity: SkyBlockRarity, commission: Commission?): Int {
        if (this.data?.isSuccess != true) {
            return rarity.color
        }

        return when {
            commission == null -> when (rarity) {
                SkyBlockRarity.UNCOMMON -> PvColors.DARK_GREEN
                SkyBlockRarity.RARE -> PvColors.DARK_BLUE
                SkyBlockRarity.LEGENDARY -> PvColors.YELLOW
                SkyBlockRarity.MYTHIC -> PvColors.DARK_PURPLE
                SkyBlockRarity.SPECIAL -> PvColors.DARK_RED
                else -> PvColors.BLACK
            }

            else -> rarity.color
        }
    }

    private fun toDisplay(visitor: StaticVisitorData, commission: Commission?): Display {
        val item = loadingValue(
            visitor.itemStack.takeUnless { commission == null } ?: Items.GRAY_DYE.defaultInstance,
            Items.ORANGE_DYE.defaultInstance,
            Items.BEDROCK.defaultInstance,
        )

        return Displays.item(item).withTooltip {
            val profile = data ?: run {
                add("Loading...") { this.color = PvColors.LIGHT_PURPLE }
                return@withTooltip
            }

            if (profile.isFailure) {
                add("Error!") { this.color = PvColors.RED }
                return@withTooltip
            }

            add(visitor.name) { this.color = visitor.rarity.color }

            if (commission != null) {
                space()
                add("Visits: ") {
                    this.color = PvColors.GRAY
                    append("${commission.total}") { this.color = PvColors.GREEN }
                }
                add("Accepted: ") {
                    this.color = PvColors.GRAY
                    append("${commission.accepted}") { this.color = PvColors.GREEN }
                }
                add("Rejected: ") {
                    this.color = PvColors.GRAY
                    append("${commission.total - commission.accepted}") { this.color = PvColors.GREEN }
                }
            }
        }
    }
}
