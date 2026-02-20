package me.owdding.skyblockpv.screens.windowed.tabs.farming

import com.mojang.authlib.GameProfile
import me.owdding.lib.displays.Display
import me.owdding.lib.displays.DisplayWidget
import me.owdding.lib.displays.Displays
import me.owdding.lib.displays.withTooltip
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.data.repo.MutationData
import me.owdding.skyblockpv.data.repo.StaticGardenData
import me.owdding.skyblockpv.screens.windowed.tabs.base.GroupedScreen
import me.owdding.skyblockpv.utils.theme.PvColors
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.BlockItemStateProperties
import tech.thatgravyboat.skyblockapi.api.data.SkyBlockRarity
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockItemId
import tech.thatgravyboat.skyblockapi.utils.builders.ItemBuilder
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

data class MutationEntry(
    val data: MutationData?,
    val analyzed: Boolean,
    val discovered: Boolean,
    val id: String,
)

class MutationScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseFarmingScreen(gameProfile, profile),
    GroupedScreen<MutationScreen.Filter, SkyBlockRarity, MutationEntry> {
    override fun getLayout(bg: DisplayWidget): Layout = createLayout(bg)

    override var query: String? = null
    override var filter: Filter = Filter.ALL
    override val noMatchFoundText: String = "No mutation matches the input!"

    override fun Filter.doesDisplay(data: MutationEntry): Boolean = when (this) {
        Filter.ALL -> true
        Filter.DISCOVERED -> data.discovered
        Filter.ANALYZED -> data.analyzed
        Filter.DISCOVERED_NOT_ANALYZED -> data.discovered && !data.analyzed
        Filter.UNDISCOVERED -> !data.discovered
    }

    override fun filterEntries(): Collection<Filter> = Filter.entries

    override fun Filter.display(): String = display

    override fun matchesSearch(data: MutationEntry): Boolean = true

    override val MutationEntry.group: SkyBlockRarity? get() = this.data?.rarity

    override fun SkyBlockRarity.compareTo(other: SkyBlockRarity): Int = this.ordinal - other.ordinal

    override fun getData(): List<MutationEntry> {
        val analyzed = profile.gardenData.analyzedCrops.map { it.lowercase() }
        val discovered = profile.gardenData.discoveredCrops.map { it.lowercase() }

        val all = buildSet {
            addAll(analyzed)
            addAll(discovered)
            addAll(StaticGardenData.mutations.map { it.id })
        }.map { it.lowercase() }.distinct()

        return all.map { id ->
            val data = StaticGardenData.mutations.find { it.id.lowercase() == id }

            MutationEntry(data, analyzed.contains(id) || data?.analyzable == false, discovered.contains(id), id)
        }
    }

    override fun getColor(
        group: SkyBlockRarity?,
        data: MutationEntry,
    ): Int {
        val (data, analyzed, _) = data
        if (this.data?.isSuccess != true) {
            return group?.color ?: PvColors.DARK_GRAY
        }

        return when {
            analyzed && data == null -> PvColors.DARK_GRAY
            else -> when (group) {
                SkyBlockRarity.COMMON if !analyzed -> PvColors.GRAY
                SkyBlockRarity.UNCOMMON if !analyzed -> PvColors.DARK_GREEN
                SkyBlockRarity.RARE if !analyzed -> PvColors.DARK_BLUE
                SkyBlockRarity.EPIC if !analyzed -> PvColors.LIGHT_PURPLE
                SkyBlockRarity.LEGENDARY if !analyzed -> PvColors.YELLOW
                SkyBlockRarity.MYTHIC if !analyzed -> PvColors.DARK_PURPLE
                SkyBlockRarity.SPECIAL if !analyzed -> PvColors.DARK_RED
                else -> group?.color ?: PvColors.DARK_GRAY
            }
        }
    }

    override fun toDisplay(
        group: SkyBlockRarity?,
        data: MutationEntry,
    ): Display {
        val (data, analyzed, discovered, id) = data

        val baseItem = data?.id?.let { SkyBlockItemId.item(it).toItem() }
        return Displays.item(
            baseItem.takeIf { discovered && filter != Filter.UNDISCOVERED } ?: Items.GRAY_DYE.defaultInstance.takeUnless { data == null }
            ?: ItemBuilder(Items.TEST_BLOCK) {
                set(DataComponents.BLOCK_STATE, BlockItemStateProperties(mapOf("mode" to "fail")))
            },
        ).withTooltip {
            add(baseItem?.hoverName ?: Text.of(id))
            space()
            fun append(type: String, value: Boolean, notApplicable: Boolean = false) = add("$type: ") {
                color = TextColor.GRAY
                append(when {
                        notApplicable -> "N/A"
                        value -> "Yes"
                        else -> "No"
                    }) {
                    color = when {
                        notApplicable -> TextColor.ORANGE
                        value -> TextColor.GREEN
                        else -> TextColor.RED
                    }
                }
            }

            append("Discovered", discovered)
            append("Analyzed", analyzed, data?.analyzable == false)
        }
    }

    enum class Filter(val display: String) {
        ALL("All"),
        UNDISCOVERED("Undiscovered"),
        DISCOVERED("Discovered"),
        DISCOVERED_NOT_ANALYZED("Discovered (not analyzed)"),
        ANALYZED("Analyzed"),
    }
}
