package tech.thatgravyboat.skyblockpv.data.api

import tech.thatgravyboat.skyblockpv.api.ItemAPI


data class CollectionItem(
    val category: String,
    val itemId: String,
    val amount: Long,
) {
    val itemStack by lazy { ItemAPI.getItem(itemId) }
}


data class CollectionCategory(
    val items: Map<String, CollectionEntry>,
)

data class CollectionEntry(
    val name: String,
    val maxTiers: Int,
    val tiers: Map<String, Long>,
)
