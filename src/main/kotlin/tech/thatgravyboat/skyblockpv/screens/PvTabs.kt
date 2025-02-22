package tech.thatgravyboat.skyblockpv.screens

import com.mojang.authlib.GameProfile
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockpv.api.data.SkyblockProfile
import tech.thatgravyboat.skyblockpv.screens.tabs.CollectionScreen
import tech.thatgravyboat.skyblockpv.screens.tabs.MainScreen
import tech.thatgravyboat.skyblockpv.screens.tabs.MobScreen
import java.util.*
import kotlin.reflect.KClass

enum class PvTabs(val screen: KClass<out BasePvScreen>, val icon: ItemStack) {
    MAIN(MainScreen::class, ItemStack.EMPTY),
    COLLECTION(CollectionScreen::class, Items.ITEM_FRAME.defaultInstance),
    MOB(MobScreen::class, Items.ZOMBIE_HEAD.defaultInstance),
    ;

    fun create(gameProfile: GameProfile): BasePvScreen {
        return screen.constructors.first().call(gameProfile, null)
    }

    fun create(gameProfile: GameProfile, profile: SkyblockProfile?): BasePvScreen {
        return screen.constructors.first().call(gameProfile, profile)
    }
}
