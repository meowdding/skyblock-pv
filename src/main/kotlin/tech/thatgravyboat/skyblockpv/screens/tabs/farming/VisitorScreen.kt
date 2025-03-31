package tech.thatgravyboat.skyblockpv.screens.tabs.farming

import com.mojang.authlib.GameProfile
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.api.data.SkyBlockRarity
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.data.skills.farming.Commission
import tech.thatgravyboat.skyblockpv.data.skills.farming.StaticGardenData
import tech.thatgravyboat.skyblockpv.data.skills.farming.StaticVisitorData
import tech.thatgravyboat.skyblockpv.utils.LayoutBuild
import tech.thatgravyboat.skyblockpv.utils.LayoutUtils.asScrollable
import tech.thatgravyboat.skyblockpv.utils.Utils.append
import tech.thatgravyboat.skyblockpv.utils.displays.*

class VisitorScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseFarmingScreen(gameProfile, profile) {
    override fun getLayout(): Layout {
        val visitors = data?.map { it.commissionData.commissions.associateBy { it.visitor } }?.getOrNull() ?: emptyMap()

        return StaticGardenData.visitors
            .groupBy { it.rarity }
            .map { it.value }
            .map {
                it.chunked(uiWidth / 24).map {
                    it.map {
                        Displays.inventorySlot(
                            Displays.padding(2, toDisplay(it, visitors[it])),
                            this.getRarity(it.rarity, visitors[it]),
                        )
                    }.toRow(0, Alignment.CENTER)
                }.toColumn(0, Alignment.CENTER)
            }.toColumn(4, Alignment.CENTER)
            .let { LayoutBuild.frame { display(it) }.asScrollable(uiWidth, uiHeight) }
    }

    private fun getRarity(rarity: SkyBlockRarity, commission: Commission?): Int {
        if (this.data?.isSuccess != true) {
            return rarity.color
        }

        return when {
            commission == null -> when (rarity) {
                SkyBlockRarity.UNCOMMON -> TextColor.DARK_GREEN
                SkyBlockRarity.RARE -> TextColor.DARK_BLUE
                SkyBlockRarity.LEGENDARY -> TextColor.YELLOW
                SkyBlockRarity.MYTHIC -> TextColor.DARK_PURPLE
                SkyBlockRarity.SPECIAL -> TextColor.DARK_RED
                else -> TextColor.BLACK
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
                add("Loading...") { this.color = TextColor.LIGHT_PURPLE }
                return@withTooltip
            }

            if (profile.isFailure) {
                add("Error!") { this.color = TextColor.RED }
                return@withTooltip
            }

            add(visitor.name) { this.color = visitor.rarity.color }

            if (commission != null) {
                space()
                add("Visits: ") {
                    this.color = TextColor.GRAY
                    append("${commission.total}") { this.color = TextColor.GREEN }
                }
                add("Accepted: ") {
                    this.color = TextColor.GRAY
                    append("${commission.accepted}") { this.color = TextColor.GREEN }
                }
                add("Rejected: ") {
                    this.color = TextColor.GRAY
                    append("${commission.total - commission.accepted}") { this.color = TextColor.GREEN }
                }
            }
        }
    }
}
