package tech.thatgravyboat.skyblockpv.screens

import tech.thatgravyboat.skyblockpv.screens.tabs.CollectionScreen
import tech.thatgravyboat.skyblockpv.screens.tabs.MainScreen
import tech.thatgravyboat.skyblockpv.screens.tabs.MobScreen
import java.util.UUID
import kotlin.reflect.KClass

enum class PvTabs(val screen: KClass<out BasePvScreen>) {
    MAIN(MainScreen::class),
    COLLECTION(CollectionScreen::class),
    MOB(MobScreen::class);

    fun create(uuid: UUID): BasePvScreen {
        return screen.constructors.first().call(uuid)
    }
}
