package me.owdding.skyblockpv.api.predicates

import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import java.util.function.Predicate

typealias ItemPredicate = Predicate<ItemStack>

object ItemPredicates {

    fun of(predicate: (ItemStack) -> Boolean): ItemPredicate {
        return ItemPredicate { predicate(it) }
    }

    fun SkyblockID(id: String): ItemPredicate {
        return of { item -> item.getData(DataTypes.ID) == id }
    }

    fun AnySkyblockID(ids: List<String>): ItemPredicate {
        return ids.map { SkyblockID(it) }.reduce(ItemPredicate::or)
    }

}

object ItemPredicateHelper {

    fun getItemsMatching(profile: SkyBlockProfile, predicate: ItemPredicate): List<ItemStack>? {
        val inventory = profile.inventory ?: return null
        return inventory.getAllItems().filter(predicate::test)
    }
}
