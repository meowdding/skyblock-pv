package tech.thatgravyboat.skyblockpv.screens.tabs.inventory

import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.helpers.McScreen
import tech.thatgravyboat.skyblockpv.screens.BasePvScreen
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

enum class InventoryCategory(val screen: KClass<out BasePvScreen>, val icon: ItemStack) {
    INVENTORY(InventoryScreen::class, Items.CHEST.defaultInstance),
    ;

    fun isSelected() = screen.isSubclassOf(McScreen.self!!::class)
}
