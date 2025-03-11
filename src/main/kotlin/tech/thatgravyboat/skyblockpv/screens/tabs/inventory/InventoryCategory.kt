package tech.thatgravyboat.skyblockpv.screens.tabs.inventory

import com.mojang.authlib.GameProfile
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.helpers.McScreen
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.data.SkullTextures
import tech.thatgravyboat.skyblockpv.screens.BasePvScreen
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

val backpackIcon by lazy { SkullTextures.BACKPACK.createSkull() }
val accessoryIcon by lazy { SkullTextures.ACCESSORY_BAG.createSkull() }

enum class InventoryCategory(val screen: KClass<out BasePvScreen>, val icon: ItemStack) {
    INVENTORY(InventoryScreen::class, Items.CHEST.defaultInstance),
    ENDER_CHEST(EnderChestScreen::class, Items.ENDER_CHEST.defaultInstance),
    BACKPACK(BackpackScreen::class, backpackIcon),
    ACCESSORY(AccessoryScreen::class, accessoryIcon),
    ;

    fun isSelected() = McScreen.self?.takeIf { it::class.isSubclassOf(screen) } != null

    fun create(gameProfile: GameProfile, profile: SkyBlockProfile? = null) = screen.constructors.first().call(gameProfile, profile)
}
