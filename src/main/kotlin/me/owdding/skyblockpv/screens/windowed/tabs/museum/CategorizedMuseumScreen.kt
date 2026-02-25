package me.owdding.skyblockpv.screens.windowed.tabs.museum

import com.mojang.authlib.GameProfile
import me.owdding.lib.displays.Display
import me.owdding.lib.displays.Displays
import me.owdding.lib.displays.asTable
import me.owdding.lib.displays.withPadding
import me.owdding.lib.extensions.transpose
import me.owdding.lib.extensions.withTooltip
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.data.museum.MuseumArmor
import me.owdding.skyblockpv.data.museum.MuseumData
import me.owdding.skyblockpv.data.museum.MuseumItem
import me.owdding.skyblockpv.data.museum.MuseumRepoEntry
import me.owdding.skyblockpv.data.museum.RepoMuseumData.MuseumCategory
import me.owdding.skyblockpv.utils.displays.DropdownContext
import me.owdding.skyblockpv.utils.displays.ExtraDisplays
import me.owdding.skyblockpv.utils.displays.withDropdown
import me.owdding.skyblockpv.utils.theme.PvColors
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import tech.thatgravyboat.skyblockapi.api.remote.RepoItemsAPI
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

class CategorizedMuseumScreen(val category: MuseumCategory, gameProfile: GameProfile, profile: SkyBlockProfile? = null) :
    BaseMuseumScreen<CategorizedMuseumScreen.Filter, MuseumRepoEntry>(gameProfile, profile) {

    override var filter: Filter = Filter.ALL
    override fun filterEntries(): Collection<Filter> = Filter.entries
    override fun Filter.display(): String = display

    override fun entries(): List<MuseumRepoEntry> {
        val query = query
        return listOf(category.items, category.armors).flatten().filter { query == null || it.matches(query) }
    }

    fun createItem(museumItem: MuseumItem, museumData: MuseumData): List<ItemStack>? {
        val result = museumData.items.find { it.id == museumItem.id }?.stacks?.map { it.value }
        if (filter == Filter.DONATED) return result
        if (result != null && (filter == Filter.MISSING || filter == Filter.DONATED_THROUGH_PARENT)) return null
        return result ?: run {
            val parent = museumData.isParentDonated(museumItem)
            val defaultInstance = (if (parent != null) Items.LIME_DYE else Items.GRAY_DYE).defaultInstance
            if (filter == Filter.MISSING && parent != null) return null
            if (filter == Filter.DONATED_THROUGH_PARENT && parent == null) {
                return null
            }

            listOf(
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
                },
            )
        }
    }

    fun sortAmor(itemStack: ItemStack): Int {
        val skyBlockCategory = itemStack.getData(DataTypes.CATEGORY) ?: return -1
        return when (skyBlockCategory.name) {
            "helmet", "hat", "mask" -> 8
            "chestplate" -> 7
            "leggings" -> 6
            "boots" -> 5
            "necklace" -> 4
            "cloak" -> 3
            "belt" -> 2
            "bracelet", "gloves" -> 1
            else -> -1
        }
    }

    private fun createArmor(museumArmor: MuseumArmor, data: MuseumData, dropdownContext: DropdownContext): List<Display>? {
        val parent = data.isParentDonated(museumArmor)

        val missingItem = Displays.item(
            (if (parent != null) Items.LIME_DYE else Items.GRAY_DYE).defaultInstance.withTooltip {
                if (parent != null) {
                    add("Parent donated: ") {
                        this.color = PvColors.GRAY
                        append(RepoItemsAPI.getItemName(parent))
                    }
                } else {
                    add("Missing Armor") {
                        this.color = PvColors.RED
                    }
                }

                museumArmor.armorIds.map { RepoItemsAPI.getItem(it) }.sortedByDescending { sortAmor(it) }.forEach {
                    add(it.hoverName)
                }
            },
            showTooltip = true,
        )

        val display = data.items.find { it.id == museumArmor.id }?.let {
            val table = it.stacks.map { Displays.item(it.value, showTooltip = true).withPadding(2) }.chunked(4)
            val dropdown = table.transpose().asTable().let { display ->
                ExtraDisplays.inventoryBackground(table.size, it.stacks.size.coerceAtMost(4), display.withPadding(2))
                    .withPadding(top = -4, left = -4)
            }

            Displays.item(it.stacks.first().value, showTooltip = false).withDropdown(dropdown, dropdownContext)
        }
        if (filter == Filter.DONATED) return listOf(display ?: return null)
        if (display != null && (filter == Filter.MISSING || filter == Filter.DONATED_THROUGH_PARENT)) return null
        if (filter == Filter.MISSING && parent != null) return null
        if (filter == Filter.DONATED_THROUGH_PARENT && parent == null) {
            return null
        }

        return listOf(display ?: missingItem)
    }

    override fun MuseumRepoEntry.toDisplay(
        data: MuseumData,
        dropdownContext: DropdownContext,
    ): List<Display>? {
        return when (this) {
            is MuseumArmor -> createArmor(this, data, dropdownContext)
            is MuseumItem -> createItem(this, data)?.map { Displays.item(it, showTooltip = true) }
            else -> null
        }
    }

    enum class Filter(val display: String) {
        ALL("All"),
        DONATED("Donated"),
        DONATED_THROUGH_PARENT("Donated (Through Parent)"),
        MISSING("Missing"),
    }
}
