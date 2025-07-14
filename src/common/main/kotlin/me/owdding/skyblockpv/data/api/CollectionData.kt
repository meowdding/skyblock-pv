package me.owdding.skyblockpv.data.api

import tech.thatgravyboat.skyblockapi.api.remote.RepoItemsAPI


data class CollectionItem(
    val category: String,
    val itemId: String,
    val amount: Long,
) {
    val itemStack by RepoItemsAPI.getItemLazy(itemId)
}

data class CollectionCategory(
    val items: Map<String, CollectionEntry>,
)

data class CollectionEntry(
    val name: String,
    val maxTiers: Int,
    val tiers: Map<String, Long>,
)
