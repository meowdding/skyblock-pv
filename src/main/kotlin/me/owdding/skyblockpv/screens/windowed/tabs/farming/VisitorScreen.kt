package me.owdding.skyblockpv.screens.windowed.tabs.farming

import com.mojang.authlib.GameProfile
import me.owdding.lib.displays.*
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.data.api.skills.farming.Commission
import me.owdding.skyblockpv.data.repo.StaticGardenData
import me.owdding.skyblockpv.data.repo.StaticVisitorData
import me.owdding.skyblockpv.utils.LayoutUtils.asScrollable
import me.owdding.skyblockpv.utils.Utils.append
import me.owdding.skyblockpv.utils.components.PvLayouts
import me.owdding.skyblockpv.utils.displays.ExtraDisplays
import me.owdding.skyblockpv.utils.theme.PvColors
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.BlockItemStateProperties
import tech.thatgravyboat.skyblockapi.api.data.SkyBlockRarity
import tech.thatgravyboat.skyblockapi.api.profile.PetsAPI.rarity
import tech.thatgravyboat.skyblockapi.utils.builders.ItemBuilder
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import kotlin.math.PI

class VisitorScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseFarmingScreen(gameProfile, profile) {
    override fun getLayout(bg: DisplayWidget): Layout {
        val visitors = data?.map { it.commissionData.commissions.associateBy { it.visitor } }?.getOrNull() ?: emptyMap()

        val map = buildList {
            addAll(StaticGardenData.visitors.map { it to visitors[it] })
            data?.onSuccess {
                it.commissionData.commissions.filter { it.visitor == null }.forEach { add(null to it) }
            }
        }

        return map
            .groupBy { it.first?.rarity }
            .toSortedMap { o1, o2 ->
                when {
                    o1 == null && o2 == null -> 0
                    o1 == null -> 1
                    o2 == null -> -1
                    else -> o1.ordinal - o2.ordinal
                }
            }.map { it.value }
            .map {
                it.chunked(uiWidth / 24).map {
                    it.map {
                        ExtraDisplays.inventorySlot(
                            Displays.padding(2, toDisplay(it.first, it.second)),
                            this.getRarity(it.first?.rarity, it.second),
                        )
                    }.toRow(0, Alignment.CENTER)
                }.toColumn(0, Alignment.CENTER)
            }.toColumn(4, Alignment.CENTER)
            .let { PvLayouts.frame { display(it) }.asScrollable(uiWidth, uiHeight) }
    }

    private fun getRarity(rarity: SkyBlockRarity?, commission: Commission?): Int {
        if (this.data?.isSuccess != true) {
            return rarity?.color ?: PvColors.GRAY
        }

        return when (commission) {
            null if rarity == null -> PvColors.GRAY
            null -> when (rarity) {
                SkyBlockRarity.UNCOMMON -> PvColors.DARK_GREEN
                SkyBlockRarity.RARE -> PvColors.DARK_BLUE
                SkyBlockRarity.LEGENDARY -> PvColors.YELLOW
                SkyBlockRarity.MYTHIC -> PvColors.DARK_PURPLE
                SkyBlockRarity.SPECIAL -> PvColors.DARK_RED
                else -> PvColors.BLACK
            }

            else -> rarity?.color ?: PvColors.GRAY
        }
    }

    private fun toDisplay(visitor: StaticVisitorData?, commission: Commission?): Display {
        val item = loadingValue(
            visitor?.itemStack.takeUnless { commission == null } ?: Items.GRAY_DYE.defaultInstance.takeUnless { visitor == null }
            ?: ItemBuilder(Items.TEST_BLOCK) {
                set(DataComponents.BLOCK_STATE, BlockItemStateProperties(mapOf("mode" to "fail")))
            },
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

            add(visitor?.name ?: commission!!.visitorId) { this.color = visitor?.rarity?.color ?: PvColors.GRAY }

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
