package tech.thatgravyboat.skyblockpv.api

import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.remote.RepoItemsAPI

object ItemAPI {
    @Deprecated("Use RepoItemsAPI.getItem() instead", ReplaceWith("RepoItemsAPI.getItem(id)"))
    fun getItem(id: String): ItemStack = RepoItemsAPI.getItem(id)
}
