package tech.thatgravyboat.skyblockpv.data

import net.minecraft.world.item.ItemStack


data class CollectionItem(
    val category: String,
    val itemId: String,
    val itemStack: ItemStack?,
    val amount: Long,
)


data class CollectionCategory(
    val items: Map<String, CollectionEntry>,
)

data class CollectionEntry(
    val name: String,
    val maxTiers: Int,
    val tiers: Map<String, Long>,
)
