package tech.thatgravyboat.skyblockpv.screens.tabs.inventory

import com.mojang.authlib.GameProfile
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.helpers.McScreen
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.data.SkullTextures
import tech.thatgravyboat.skyblockpv.screens.BasePvScreen
import tech.thatgravyboat.skyblockpv.screens.tabs.base.Category
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

val backpackIcon by lazy { SkullTextures.BACKPACK.createSkull() }
val accessoryIcon by lazy { SkullTextures.ACCESSORY_BAG.createSkull() }
val personalVaultIcon by lazy { SkullTextures.PERSONAL_VAULT.createSkull() }

enum class InventoryCategory(val screen: KClass<out BasePvScreen>, override val icon: ItemStack) : Category {
    INVENTORY(InventoryScreen::class, Items.CHEST.defaultInstance),
    ENDER_CHEST(EnderChestScreen::class, Items.ENDER_CHEST.defaultInstance),
    BACKPACK(BackpackScreen::class, backpackIcon),
    WARDROBE(WardrobeScreen::class, Items.LEATHER_CHESTPLATE.defaultInstance),
    ACCESSORY(AccessoryScreen::class, accessoryIcon),
    PERSONAL_VAULT(ItemVaultScreen::class, personalVaultIcon),
    POTION_BAG(PotionBagScreen::class, Items.POTION.defaultInstance),
    FISHING_BAG(FishingBagScreen::class, Items.FISHING_ROD.defaultInstance),
    QUIVER_BAG(QuiverBagScreen::class, Items.ARROW.defaultInstance),
    ;

    override val isSelected: Boolean get() = McScreen.self?.takeIf { it::class.isSubclassOf(screen) } != null
    override fun create(gameProfile: GameProfile, profile: SkyBlockProfile?) = screen.constructors.first().call(gameProfile, profile)
}
