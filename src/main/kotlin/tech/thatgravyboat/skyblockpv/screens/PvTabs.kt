package tech.thatgravyboat.skyblockpv.screens

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

    fun create(uuid: UUID): BasePvScreen {
        return screen.constructors.first().call(uuid, null)
    }

    fun create(uuid: UUID, profile: SkyblockProfile?): BasePvScreen {
        return screen.constructors.first().call(uuid, profile)
    }
}
