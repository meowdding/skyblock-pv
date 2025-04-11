package tech.thatgravyboat.skyblockpv.screens.tabs.museum

import com.mojang.authlib.GameProfile
import earth.terrarium.olympus.client.components.buttons.Button
import earth.terrarium.olympus.client.components.renderers.WidgetRenderers
import earth.terrarium.olympus.client.layouts.Layouts
import earth.terrarium.olympus.client.layouts.LinearViewLayout
import earth.terrarium.olympus.client.ui.UIConstants
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockpv.api.ItemAPI
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.data.museum.*
import tech.thatgravyboat.skyblockpv.utils.ExtraWidgetRenderers
import tech.thatgravyboat.skyblockpv.utils.LayoutBuild
import tech.thatgravyboat.skyblockpv.utils.LayoutUtils.centerHorizontally
import tech.thatgravyboat.skyblockpv.utils.Utils.rightPad
import tech.thatgravyboat.skyblockpv.utils.components.CarouselWidget
import tech.thatgravyboat.skyblockpv.utils.displays.*
import tech.thatgravyboat.skyblockpv.utils.withTooltip
import kotlin.math.ceil

class MuseumItemScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) :
    BaseMuseumScreen(gameProfile, profile) {
    private var carousel: CarouselWidget? = null

    fun getInventories(): List<Display> {
        return RepoMuseumData.museumCategoryMap.entries
            .flatMap { (_, entries) -> createPagesForCategory(entries) }
            .map { Displays.inventoryBackground(9, 6, Displays.padding(2, it)) }
    }

    fun createPagesForCategory(items: List<MuseumItem>): List<Display> {
        val sortedItems = items.asSequence().sortedWith(
            Comparator
                .comparingInt<MuseumRepoEntry> { ItemAPI.getItem(it.id).getData(DataTypes.RARITY)?.ordinal ?: 0 }
                .thenComparing({ ItemAPI.getItemName(it.id).stripped }, String::compareTo),
        )

        val map = sortedItems.map { item ->
            loaded(
                whileLoading = listOf(Items.ORANGE_DYE.defaultInstance),
                onError = listOf(Items.BEDROCK.defaultInstance),
            ) { createItem(item, it) }
        }

        return map.flatMap { it.map { Displays.item(it, showTooltip = true) } }
            .chunked(54)
            .map { mapItemListToInventories(it) }
            .toList()
    }

    fun mapItemListToInventories(item: List<Display>): Display {
        return item.toMutableList().rightPad(54, Displays.empty(16, 16))
            .map { Displays.padding(2, it) }
            .chunked(9)
            .map { it.toRow() }
            .toColumn()
    }

    fun createItem(museumItem: MuseumItem, museumData: MuseumData): List<ItemStack> {
        return museumData.items.find { it.id == museumItem.id }?.stacks?.map { it.value } ?: listOf(
            run {
                val parent = museumData.isParentDonated(museumItem)
                val defaultInstance = (if (parent != null) Items.LIME_DYE else Items.GRAY_DYE).defaultInstance
                defaultInstance.withTooltip {
                    add(ItemAPI.getItemName(museumItem.id))
                    if (parent != null) {
                        add("Parent donated: ") {
                            this.color = TextColor.GRAY
                            append(ItemAPI.getItemName(parent))
                        }
                    } else {
                        add("This item has not been donated!") {
                            this.color = TextColor.GRAY
                        }
                    }
                }
            },
        )
    }

    private fun getIcons(): List<RepoMuseumCategory> =
        RepoMuseumData.museumCategoryMap.entries.flatMap { (category, entries) ->
            val nextUp = ceil(entries.size / 54.0).toInt()
            mutableListOf<RepoMuseumCategory>().rightPad(nextUp, category)
        }

    override fun getLayout(bg: DisplayWidget) = LayoutBuild.vertical {
        val inventories = getInventories()
        val icons = getIcons()

        carousel = CarouselWidget(
            inventories,
            carousel?.index ?: 0,
            246,
        )

        val map = mutableMapOf<RepoMuseumCategory, Int>()
        val buttons = List(inventories.size) { index ->
            val icon = icons[index]
            val compute = map.compute(icon) { _, i -> i?.plus(1) ?: 0 }
            val itemDisplay = Displays.item(
                icon.item.value,
                customStackText = (compute ?: 0).takeIf { it != 0 }?.let { Text.of(it.plus(1).toString()) },
            ).withTooltip {
                add(icon.name)
            }

            Button()
                .withSize(20, 20)
                .withRenderer(
                    WidgetRenderers.layered(
                        ExtraWidgetRenderers.conditional(
                            WidgetRenderers.sprite(UIConstants.PRIMARY_BUTTON),
                            WidgetRenderers.sprite(UIConstants.DARK_BUTTON),
                        ) { index == carousel?.index },
                        WidgetRenderers.center(16, 20, ExtraWidgetRenderers.display(itemDisplay)),
                    ),
                )
                .withCallback {
                    carousel?.index = index
                }
        }

        val buttonContainer = buttons.chunked(9)
            .map { it.fold(Layouts.row().withGap(1), LinearViewLayout::withChild) }
            .fold(Layouts.column().withGap(1), LinearViewLayout::withChild)

        widget(buttonContainer.centerHorizontally(uiWidth))
        spacer(height = 10)
        widget(carousel!!.centerHorizontally(uiWidth))
    }
}
