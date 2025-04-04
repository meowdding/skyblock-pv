package tech.thatgravyboat.skyblockpv.screens.tabs.museum

import com.mojang.authlib.GameProfile
import earth.terrarium.olympus.client.components.buttons.Button
import earth.terrarium.olympus.client.components.renderers.WidgetRenderers
import earth.terrarium.olympus.client.layouts.Layouts
import earth.terrarium.olympus.client.layouts.LinearViewLayout
import earth.terrarium.olympus.client.ui.UIConstants
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.ItemLore
import net.minecraft.world.item.component.TooltipDisplay
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import tech.thatgravyboat.skyblockpv.SkyBlockPv
import tech.thatgravyboat.skyblockpv.api.ItemAPI
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.data.museum.MuseumItem
import tech.thatgravyboat.skyblockpv.data.museum.MuseumRepoEntry
import tech.thatgravyboat.skyblockpv.data.museum.RepoMuseumCategory
import tech.thatgravyboat.skyblockpv.data.museum.RepoMuseumData
import tech.thatgravyboat.skyblockpv.utils.ExtraWidgetRenderers
import tech.thatgravyboat.skyblockpv.utils.LayoutBuild
import tech.thatgravyboat.skyblockpv.utils.LayoutUtils.centerHorizontally
import tech.thatgravyboat.skyblockpv.utils.Utils.rightPad
import tech.thatgravyboat.skyblockpv.utils.components.CarouselWidget
import tech.thatgravyboat.skyblockpv.utils.displays.*
import tech.thatgravyboat.skyblockpv.utils.tooltipDataComponents
import kotlin.math.ceil

abstract class AbstractMuseumItemScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) :
    BaseMuseumScreen(gameProfile, profile) {
    protected var carousel: CarouselWidget? = null

    fun getInventories() = RepoMuseumData.museumCategoryMap.entries.flatMap { (_, entries) ->
        entries.asSequence().sortedWith(
            Comparator.comparingInt<MuseumRepoEntry> { ItemAPI.getItem(it.id).getData(DataTypes.RARITY)?.ordinal ?: 0 }
                .thenComparing({ ItemAPI.getItemName(it.id).stripped }, String::compareTo),
        ).map { item ->
            loaded(
                whileLoading = listOf(Items.ORANGE_DYE.defaultInstance),
                onError = listOf(Items.BEDROCK.defaultInstance),
            ) {
                it.items.find { it.id == item.id }?.stacks
                    ?: listOf(
                        run {
                            val defaultInstance =
                                (if (it.isParentDonated(item)) Items.LIME_DYE else Items.GRAY_DYE).defaultInstance
                            defaultInstance.set(DataComponents.CUSTOM_NAME, ItemAPI.getItemName(item.id))
                            defaultInstance.set(DataComponents.LORE, ItemLore(listOf(ItemAPI.getItemName(item.id))))
                            defaultInstance
                        },
                    )
            }
        }.flatMap { it.map { Displays.item(it, showTooltip = true) } }.chunked(54)
            .map {
                it.toMutableList().rightPad(54, Displays.empty(16, 16))
                    .map { Displays.padding(2, it) }
                    .chunked(9)
                    .map { it.toRow() }
                    .toColumn()
            }.toList()
    }.map { Displays.inventoryBackground(9, 6, Displays.padding(2, it)) }

    private fun getIcons(): List<RepoMuseumCategory> =
        RepoMuseumData.museumCategoryMap.entries.flatMap { (category, entries) ->
            val nextUp = ceil(entries.size / 54.0).toInt()
            mutableListOf<RepoMuseumCategory>().rightPad(nextUp, category)
        }

    open fun getExtraLine(): Display? = null

    override fun getLayout() = LayoutBuild.vertical {
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
                customStackText = (compute ?: 0).takeIf { it != 0 }?.let { Text.of(it.plus(1).toString()) }
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

        getExtraLine()?.let {
            spacer(height = 5)
            widget(it.asWidget().centerHorizontally(uiWidth))
        }
    }

    fun meow() = LayoutBuild.frame {
        getMuseumData().asSequence().sortedBy { it.id }.map { repoData ->
            val item = loaded(
                whileLoading = Items.ORANGE_DYE.defaultInstance,
                onError = Items.BEDROCK.defaultInstance,
            ) {
                it.items.find { it.id == repoData.id }?.let { museumItem ->
                    if (museumItem.stacks.size > 1) {
                        SkyBlockPv.error("Stack size is ${museumItem.stacks.size} expected 0 or 1, (${repoData.id})")
                    }
                    museumItem.stacks.firstOrNull()
                } ?: run {
                    val defaultInstance = Items.GRAY_DYE.defaultInstance
                    defaultInstance.set(DataComponents.CUSTOM_NAME, ItemAPI.getItemName(repoData.id))
                    defaultInstance.set(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay(false, tooltipDataComponents))
                    TooltipBuilder().apply {

                    }.lines().map { Text.multiline(it) }
                        .let { defaultInstance.set(DataComponents.LORE, ItemLore(it, it)) }

                    defaultInstance
                }
            }
            Displays.item(item, showTooltip = true)
        }.map { Displays.padding(2, it) }.chunked(54).map { it.toMutableList() }.map {
            it.rightPad(54, Displays.placeholder(20, 20))
            Displays.inventoryBackground(9, 6, Displays.padding(2, it.chunked(9).map { it.toRow() }.toColumn()))
        }.toList().let { CarouselWidget(it, 0, 246) }.let { widget(it) }
    }

    abstract fun getMuseumData(): List<MuseumItem>
}
