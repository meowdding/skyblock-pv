package tech.thatgravyboat.skyblockpv.screens

import com.mojang.authlib.GameProfile
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.helpers.McScreen
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.screens.tabs.*
import tech.thatgravyboat.skyblockpv.screens.tabs.inventory.InventoryScreen
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

enum class PvTabs(val screen: KClass<out BasePvScreen>, val icon: ItemStack) {
    MAIN(MainScreen::class, ItemStack.EMPTY),
    DUNGEON(DungeonScreen::class, Items.DIAMOND_SWORD.defaultInstance),
    INVENTORY(InventoryScreen::class, Items.CHEST.defaultInstance),
    COLLECTION(CollectionScreen::class, Items.ITEM_FRAME.defaultInstance),
    MOB(MobScreen::class, Items.ZOMBIE_HEAD.defaultInstance),
    MINING(MiningScreen::class, Items.DIAMOND_PICKAXE.defaultInstance),
    ;

    fun isSelected() = screen.isSubclassOf(McScreen.self!!::class)

    fun create(gameProfile: GameProfile): BasePvScreen {
        return screen.constructors.first().call(gameProfile, null)
    }

    fun create(gameProfile: GameProfile, profile: SkyBlockProfile?): BasePvScreen {
        return screen.constructors.first().call(gameProfile, profile)
    }
}
