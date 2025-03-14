package tech.thatgravyboat.skyblockpv.api

import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile

typealias ItemPredicate = (ItemStack) -> Boolean

fun ItemPredicate.and(other: (ItemStack) -> Boolean): ItemPredicate {
    return { this(it) && other(it) }
}

fun ItemPredicate.or(other: (ItemStack) -> Boolean): ItemPredicate {
    return { this(it) || other(it) }
}

object ItemApi {

    fun anySkyblockId(ids: List<String>): ItemPredicate {
        return ids.map { skyblockIdPredicate(it) }.reduce(ItemPredicate::or)
    }

    fun skyblockIdPredicate(predicate: String): ItemPredicate {
        return skyblockIdPredicate { it.equals(predicate.lowercase(), true) }
    }

    fun skyblockIdPredicate(predicate: (String) -> Boolean): ItemPredicate {
        return { it.getData(DataTypes.ID)?.let(predicate::invoke) ?: false }
    }

    /**
     * @return `null` if no inventory data is available, if data is available it will return a list with all matches, or empty if nothing was found.
     */
    fun getItemsMatching(profile: SkyBlockProfile, predicate: ItemPredicate): List<ItemStack>? {
        val inventory = profile.inventory ?: return null
        return inventory.getAllItems().filter(predicate)
    }

}
