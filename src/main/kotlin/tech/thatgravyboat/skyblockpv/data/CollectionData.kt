package tech.thatgravyboat.skyblockpv.data

import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items


fun getIconFromCollectionType(type: String): ItemStack {
    return when (type) {
        "MINING" -> Items.STONE_PICKAXE.defaultInstance
        "FARMING" -> Items.GOLDEN_HOE.defaultInstance
        "COMBAT" -> Items.STONE_SWORD.defaultInstance
        "FORAGING" -> Items.JUNGLE_SAPLING.defaultInstance
        "FISHING" -> Items.FISHING_ROD.defaultInstance
        "RIFT" -> Items.MYCELIUM.defaultInstance
        else -> Items.BARRIER.defaultInstance
    }
}

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
