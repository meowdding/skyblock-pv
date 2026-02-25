package me.owdding.skyblockpv.screens.windowed.tabs.museum

import com.mojang.authlib.GameProfile
import me.owdding.lib.displays.Display
import me.owdding.lib.displays.Displays
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.data.museum.MuseumData
import me.owdding.skyblockpv.utils.displays.DropdownContext
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.utils.extentions.cleanName
import tech.thatgravyboat.skyblockapi.utils.extentions.getRawLore
import tech.thatgravyboat.skyblockapi.utils.extentions.getSkyBlockId

class MiscMuseumScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseMuseumScreen<MiscMuseumScreen.Filter, Unit>(gameProfile, profile) {
    override fun entries(): List<Unit> = listOf(Unit)

    override fun Unit.toDisplay(
        data: MuseumData,
        dropdownContext: DropdownContext,
    ): List<Display> {
        val query = query

        return data.special.map(Lazy<ItemStack>::value).filter {
            query == null || buildSet {
                add(it.cleanName)
                it.getSkyBlockId()?.let(::add)
                it.getRawLore().forEach(::add)
            }.any {
                it.contains(query, true)
            }
        }.map {
            Displays.item(it, showTooltip = true)
        }
    }
    override var filter: Filter = Filter.ALL
    override fun filterEntries(): Collection<Filter> = Filter.entries
    override fun Filter.display(): String = display

    enum class Filter(val display: String) {
        ALL("All")
    }
}
