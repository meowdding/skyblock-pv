package tech.thatgravyboat.skyblockpv.screens.tabs.inventory

import com.mojang.authlib.GameProfile
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.lib.displays.Display
import tech.thatgravyboat.lib.displays.Displays
import tech.thatgravyboat.lib.displays.asTable
import tech.thatgravyboat.skyblockapi.api.remote.RepoItemsAPI
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.data.repo.SackCodecs
import tech.thatgravyboat.skyblockpv.utils.Utils.rightPad
import tech.thatgravyboat.skyblockpv.utils.Utils.shorten
import tech.thatgravyboat.skyblockpv.utils.displays.ExtraDisplays

class SacksScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BasePagedInventoryScreen(gameProfile, profile) {
    private val sackItems get() = profile?.inventory?.sacks ?: emptyMap()
    private val sackDisplays: Map<ItemStack, Display>
        get() = SackCodecs.data.mapNotNull {
            val sackItems = it.items.associateWith { sackItems[it] ?: 0 }
            if (sackItems.entries.sumOf { it.value } == 0L) return@mapNotNull null

            val display = sackItems.map {
                Displays.item(RepoItemsAPI.getItem(it.key), customStackText = it.value.shorten(0), showTooltip = true)
            }.toMutableList().rightPad(9, Displays.item(ItemStack.EMPTY)).map { Displays.padding(2, it) }.chunked(9).let {
                ExtraDisplays.inventoryBackground(
                    9, it.size,
                    Displays.padding(2, it.asTable()),
                )
            }

            it.item to display
        }.toMap()

    override fun getInventories(): List<Display> = sackDisplays.values.toList()

    override fun getIcons(): List<ItemStack> = sackDisplays.keys.toList()

    override val itemStackSize = false
}
