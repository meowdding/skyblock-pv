package me.owdding.skyblockpv.screens.windowed.tabs.museum

import com.mojang.authlib.GameProfile
import me.owdding.lib.displays.*
import me.owdding.lib.extensions.rightPad
import me.owdding.lib.extensions.withTooltip
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.data.museum.*
import me.owdding.skyblockpv.utils.LayoutUtils.centerHorizontally
import me.owdding.skyblockpv.utils.components.CarouselWidget
import me.owdding.skyblockpv.utils.components.PvLayouts
import me.owdding.skyblockpv.utils.displays.ExtraDisplays
import me.owdding.skyblockpv.utils.theme.PvColors
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import tech.thatgravyboat.skyblockapi.api.remote.RepoItemsAPI
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import kotlin.math.ceil

private const val ROWS = 5
private const val COLUMNS = 10
private const val SIZE = ROWS * COLUMNS

class MuseumItemScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) :
    BaseMuseumScreen(gameProfile, profile) {
    private var carousel: CarouselWidget? = null

    fun getInventories(): List<Display> {
        return RepoMuseumData.museumCategoryMap.entries
            .flatMap { (_, entries) -> createPagesForCategory(entries) }
            .map { ExtraDisplays.inventoryBackground(COLUMNS, ROWS, Displays.padding(2, it)) }
    }

    fun createPagesForCategory(items: List<MuseumItem>): List<Display> {
        val sortedItems = items.asSequence().sortedWith(
            Comparator
                .comparingInt<MuseumRepoEntry> { RepoItemsAPI.getItem(it.id).getData(DataTypes.RARITY)?.ordinal ?: 0 }
                .thenComparing({ RepoItemsAPI.getItemName(it.id).stripped }, String::compareTo),
        )

        val map = sortedItems.map { item ->
            loaded(
                whileLoading = listOf(Items.ORANGE_DYE.defaultInstance.withTooltip { add("Loading...") { this.color = PvColors.GOLD } }),
                onError = listOf(Items.BEDROCK.defaultInstance.withTooltip { add("Error!") { this.color = PvColors.RED } }),
            ) { createItem(item, it) }
        }

        return map.flatMap { it.map { Displays.item(it, showTooltip = true) } }
            .chunked(SIZE)
            .map { mapItemListToInventories(it) }
            .toList()
    }

    fun mapItemListToInventories(item: List<Display>): Display {
        return item.toMutableList().rightPad(SIZE, Displays.empty(16, 16))
            .map { Displays.padding(2, it) }
            .chunked(COLUMNS)
            .map { it.toRow() }
            .toColumn()
    }

    fun createItem(museumItem: MuseumItem, museumData: MuseumData): List<ItemStack> {
        return museumData.items.find { it.id == museumItem.id }?.stacks?.map { it.value } ?: listOf(
            run {
                val parent = museumData.isParentDonated(museumItem)
                val defaultInstance = (if (parent != null) Items.LIME_DYE else Items.GRAY_DYE).defaultInstance
                defaultInstance.withTooltip {
                    add(RepoItemsAPI.getItemName(museumItem.id))
                    if (parent != null) {
                        add("Parent donated: ") {
                            this.color = PvColors.GRAY
                            append(RepoItemsAPI.getItemName(parent))
                        }
                    } else {
                        add("This item has not been donated!") {
                            this.color = PvColors.GRAY
                        }
                    }
                }
            },
        )
    }

    private fun getIcons(): List<RepoMuseumCategory> =
        RepoMuseumData.museumCategoryMap.entries.flatMap { (category, entries) ->
            val nextUp = ceil(entries.size / SIZE.toDouble()).toInt()
            mutableListOf<RepoMuseumCategory>().rightPad(nextUp, category)
        }

    override fun getLayout(bg: DisplayWidget) = PvLayouts.vertical {
        val inventories = getInventories()
        val icons = getIcons()

        carousel = CarouselWidget(
            inventories,
            carousel?.index ?: 0,
            246,
        )

        val map = mutableMapOf<RepoMuseumCategory, Int>()
        val buttonContainer = carousel!!.getIcons(perRow = 12) {
            List(inventories.size) { index ->
                val icon = icons[index]
                val compute = map.compute(icon) { _, i -> i?.plus(1) ?: 0 }
                Displays.item(
                    icon.item.value,
                    customStackText = (compute ?: 0).takeIf { it != 0 }?.let { Text.of(it.plus(1).toString()) },
                ).withTooltip {
                    add(icon.name)
                }
            }
        }

        widget(buttonContainer.centerHorizontally(uiWidth))
        spacer(height = 10)
        widget(carousel!!.centerHorizontally(uiWidth))
    }
}
