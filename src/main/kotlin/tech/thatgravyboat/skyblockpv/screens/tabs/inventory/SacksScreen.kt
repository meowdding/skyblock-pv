package tech.thatgravyboat.skyblockpv.screens.tabs.inventory

import com.mojang.authlib.GameProfile
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockpv.api.ItemAPI
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.data.repo.SackCodecs
import tech.thatgravyboat.skyblockpv.utils.Utils.rightPad
import tech.thatgravyboat.skyblockpv.utils.Utils.shorten
import tech.thatgravyboat.skyblockpv.utils.displays.Display
import tech.thatgravyboat.skyblockpv.utils.displays.Displays
import tech.thatgravyboat.skyblockpv.utils.displays.asTable

class SacksScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BasePagedInventoryScreen(gameProfile, profile) {
    private val sackItems get() = profile?.inventory?.sacks ?: emptyMap()
    private val sackDisplays: Map<ItemStack, Display>
        get() = SackCodecs.data.mapNotNull { (_, v) ->
            val sackItems = v.items.associateWith { sackItems[it] ?: 0 }
            if (sackItems.entries.sumOf { it.value } == 0L) return@mapNotNull null

            val display = sackItems.map {
                Displays.item(ItemAPI.getItem(it.key), customStackText = it.value.shorten(0), showTooltip = true)
            }.toMutableList().rightPad(9, Displays.item(ItemStack.EMPTY)).map { Displays.padding(2, it) }.chunked(9).let {
                Displays.inventoryBackground(
                    9, it.size,
                    Displays.padding(2, it.asTable()),
                )
            }

            v.item to display
        }.toMap()

    override fun getInventories(): List<Display> = sackDisplays.values.toList()

    override fun getIcons(): List<ItemStack> = sackDisplays.keys.toList()
}
