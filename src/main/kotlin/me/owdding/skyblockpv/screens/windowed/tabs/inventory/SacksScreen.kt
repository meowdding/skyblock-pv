package me.owdding.skyblockpv.screens.windowed.tabs.inventory

import com.mojang.authlib.GameProfile
import me.owdding.lib.displays.Display
import me.owdding.lib.displays.Displays
import me.owdding.lib.displays.asTable
import me.owdding.lib.extensions.rightPad
import me.owdding.lib.extensions.shorten
import me.owdding.lib.extensions.withTooltip
import me.owdding.lib.repo.SacksRepoData
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.utils.Utils.append
import me.owdding.skyblockpv.utils.displays.ExtraDisplays
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.remote.RepoItemsAPI
import tech.thatgravyboat.skyblockapi.utils.extentions.getLore
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

class SacksScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BasePagedInventoryScreen(gameProfile, profile) {
    private val sackItems get() = profile.inventory?.sacks ?: emptyMap()
    private val sackDisplays: Map<ItemStack, Display>
        get() = SacksRepoData.data.mapNotNull { sack ->
            val sackItems = sack.items.associateWith { sackItems[it] ?: 0 }
            if (sackItems.entries.sumOf { it.value } == 0L) return@mapNotNull null

            val display = sackItems.map {
                val item = RepoItemsAPI.getItem(it.key).copy().apply {
                    withTooltip {
                        add(hoverName)
                        val lore = getLore()
                        val amount = lore.takeWhile { line -> !line.stripped.isBlank() }.apply { forEach(::add) }.size
                        space()
                        add("Amount: ") {
                            color = TextColor.GRAY
                            append(it.value.toFormattedString()) {
                                color = TextColor.GREEN
                            }
                        }
                        lore.drop(amount).forEach(::add)
                    }
                }
                Displays.item(item, customStackText = it.value.shorten(1), showTooltip = true)
            }.toMutableList().rightPad(9, Displays.item(ItemStack.EMPTY)).map { Displays.padding(2, it) }.chunked(9).let {
                ExtraDisplays.inventoryBackground(
                    9, it.size,
                    Displays.padding(2, it.asTable()),
                )
            }

            RepoItemsAPI.getItem(sack.sack) to display
        }.toMap()

    override fun getInventories(): List<Display> = sackDisplays.values.toList()

    override fun getIcons(): List<ItemStack> = sackDisplays.keys.toList()

    override val itemStackSize = false
}
