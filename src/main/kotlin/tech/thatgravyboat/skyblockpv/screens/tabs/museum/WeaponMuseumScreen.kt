package tech.thatgravyboat.skyblockpv.screens.tabs.museum

import com.mojang.authlib.GameProfile
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.ItemLore
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import tech.thatgravyboat.skyblockpv.api.ItemAPI
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.data.LegacyItemStack
import tech.thatgravyboat.skyblockpv.data.museum.MuseumRepoEntry
import tech.thatgravyboat.skyblockpv.data.museum.RepoMuseumData
import tech.thatgravyboat.skyblockpv.utils.Utils.rightPad
import tech.thatgravyboat.skyblockpv.utils.displays.Display
import tech.thatgravyboat.skyblockpv.utils.displays.Displays
import tech.thatgravyboat.skyblockpv.utils.displays.toColumn
import tech.thatgravyboat.skyblockpv.utils.displays.toRow
import kotlin.math.ceil

class WeaponMuseumScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : AbstractMuseumItemScreen(gameProfile, profile) {
    override fun getInventories(): List<Display> {
        return RepoMuseumData.museumCategoryMap.entries.flatMap { (_, entries) ->
            entries.asSequence().sortedWith(
                Comparator.comparingInt<MuseumRepoEntry> { ItemAPI.getItem(it.id).getData(DataTypes.RARITY)?.ordinal ?: 0 }
                    .thenComparing({ ItemAPI.getItemName(it.id).stripped }, String::compareTo),
            ).map { item ->
                loaded(
                    whileLoading = listOf(LegacyItemStack.wrap(Items.ORANGE_DYE.defaultInstance)),
                    onError = listOf(LegacyItemStack.wrapItem(Items.BEDROCK)),
                ) {
                    it.items.find { it.id == item.id }?.stacks
                        ?: listOf(
                            LegacyItemStack.wrap(
                                run {
                                    val defaultInstance = (if (it.isParentDonated(item)) Items.LIME_DYE else Items.GRAY_DYE).defaultInstance
                                    defaultInstance.set(DataComponents.CUSTOM_NAME, ItemAPI.getItemName(item.id))
                                    defaultInstance.set(DataComponents.LORE, ItemLore(listOf(ItemAPI.getItemName(item.id))))
                                    defaultInstance
                                },
                            ),
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
    }

    override fun getIcons() = RepoMuseumData.museumCategoryMap.entries.flatMap { (category, entries) ->
        val nextUp = ceil(entries.size / 54.0).toInt()
        mutableListOf<ItemStack>().rightPad(nextUp, category.item.value)
    }.map { it.copy() }


    override fun getMuseumData() = listOf(RepoMuseumData.weapons, RepoMuseumData.rarities).flatten()
}
