package me.owdding.skyblockpv.screens.windowed.tabs.farming

import com.mojang.authlib.GameProfile
import me.owdding.lib.displays.Alignment
import me.owdding.lib.displays.Display
import me.owdding.lib.displays.DisplayWidget
import me.owdding.lib.displays.Displays
import me.owdding.lib.displays.asWidget
import me.owdding.lib.displays.toColumn
import me.owdding.lib.displays.toRow
import me.owdding.lib.displays.withTooltip
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.data.api.skills.farming.Commission
import me.owdding.skyblockpv.data.repo.StaticGardenData
import me.owdding.skyblockpv.data.repo.StaticVisitorData
import me.owdding.skyblockpv.screens.windowed.tabs.base.GroupedScreen
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
import tech.thatgravyboat.skyblockapi.utils.builders.ItemBuilder
import tech.thatgravyboat.skyblockapi.utils.extentions.stripColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

class VisitorScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseFarmingScreen(gameProfile, profile),
    GroupedScreen<VisitorScreen.Filter, SkyBlockRarity, Pair<StaticVisitorData?, Commission?>> {

    override var query: String? = null
    override var filter: Filter = Filter.ALL
    override val noMatchFoundText: String = "No Visitor matches the input!"
    override val Pair<StaticVisitorData?, Commission?>.group: SkyBlockRarity? get() = first?.rarity

    override fun filterEntries(): Collection<Filter> = Filter.entries
    override fun Filter.display(): String = this.display
    override fun SkyBlockRarity.compareTo(other: SkyBlockRarity): Int = this.ordinal - other.ordinal


    override fun getData(): List<Pair<StaticVisitorData?, Commission?>> {
        val visitors = data?.map { it.commissionData.commissions.associateBy { it.visitor } }?.getOrNull() ?: emptyMap()

        return buildList {
            addAll(StaticGardenData.visitors.map { it to visitors[it] })
            data?.onSuccess {
                it.commissionData.commissions.filter { it.visitor == null }.forEach { add(null to it) }
            }
        }
    }

    override fun getLayout(bg: DisplayWidget): Layout = createLayout(bg)

    override fun Filter.doesDisplay(data: Pair<StaticVisitorData?, Commission?>): Boolean {
        val (_, api) = data

        return when (this) {
            Filter.ALL -> true
            Filter.NEVER_VISITED -> api == null
            Filter.VISITED -> api != null && api.total >= 1
            Filter.VISITED_NOT_COMPLETED -> api != null && api.total >= 1 && api.accepted <= 0
            Filter.OFFER_COMPLETED -> api != null && api.accepted >= 1
        }
    }

    override fun matchesSearch(data: Pair<StaticVisitorData?, Commission?>): Boolean {
        val (repo, _) = data
        val query = query
        if (query == null || repo == null) return true
        return buildList {
            add(repo.name)
            add(repo.id)
            add(repo.rarity.name)
        }.map { it.stripColor() }.any { it.contains(query, ignoreCase = true) }

    }
    override fun getColor(group: SkyBlockRarity?, data: Pair<StaticVisitorData?, Commission?>): Int {
        val (_, commission) = data
        if (this.data?.isSuccess != true) {
            return group?.color ?: PvColors.GRAY
        }

        return when (commission) {
            null if group == null -> PvColors.GRAY
            null -> when (group) {
                SkyBlockRarity.UNCOMMON -> PvColors.DARK_GREEN
                SkyBlockRarity.RARE -> PvColors.DARK_BLUE
                SkyBlockRarity.LEGENDARY -> PvColors.YELLOW
                SkyBlockRarity.MYTHIC -> PvColors.DARK_PURPLE
                SkyBlockRarity.SPECIAL -> PvColors.DARK_RED
                else -> PvColors.BLACK
            }

            else -> group?.color ?: PvColors.GRAY
        }
    }

    override fun toDisplay(group: SkyBlockRarity?, data: Pair<StaticVisitorData?, Commission?>): Display {
        val (visitor, commission) = data
        val item = loadingValue(
            visitor?.itemStack.takeUnless { commission == null && !filter.alwaysDisplay } ?: Items.GRAY_DYE.defaultInstance.takeUnless { visitor == null }
            ?: ItemBuilder(Items.TEST_BLOCK) {
                set(DataComponents.BLOCK_STATE, BlockItemStateProperties(mapOf("mode" to "fail")))
            },
            Items.ORANGE_DYE.defaultInstance,
            Items.BEDROCK.defaultInstance,
        )

        return Displays.item(item).withTooltip {
            val profile = this@VisitorScreen.data ?: run {
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

    enum class Filter(val display: String, val alwaysDisplay: Boolean = false) {
        ALL("All"),
        NEVER_VISITED("Never Visited", true),
        VISITED("Visited"),
        VISITED_NOT_COMPLETED("Visited (None Completed)"),
        OFFER_COMPLETED("Offer Completed"),
        ;
    }
}
