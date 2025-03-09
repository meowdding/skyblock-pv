package tech.thatgravyboat.skyblockpv.screens

import com.mojang.authlib.GameProfile
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.helpers.McScreen
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.screens.tabs.*
import tech.thatgravyboat.skyblockpv.screens.tabs.inventory.InventoryScreen
import tech.thatgravyboat.skyblockpv.utils.createSkull
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

enum class PvTabs(
    private val screen: KClass<out BasePvScreen>,
    private val constructor: (GameProfile, SkyBlockProfile?) -> BasePvScreen,
    private val icon: (GameProfile?) -> ItemStack
) {
    MAIN(MainScreen::class, ::MainScreen, { it?.let(::createSkull) ?: Items.PLAYER_HEAD.defaultInstance }),
    DUNGEON(DungeonScreen::class, Items.DIAMOND_SWORD.defaultInstance),
    INVENTORY(InventoryScreen::class, ::InventoryScreen, Items.CHEST.defaultInstance),
    COLLECTION(CollectionScreen::class, Items.ITEM_FRAME.defaultInstance),
    MOB(MobScreen::class, Items.ZOMBIE_HEAD.defaultInstance),
    MINING(MiningScreen::class, Items.DIAMOND_PICKAXE.defaultInstance),
    ;

    constructor(screen: KClass<out BasePvScreen>, icon: ItemStack) : this(
        screen,
        screen.java.getConstructor(GameProfile::class.java, SkyBlockProfile::class.java)::newInstance,
        icon,
    )

    constructor(screen: KClass<out BasePvScreen>, constructor: (GameProfile, SkyBlockProfile?) -> BasePvScreen, icon: ItemStack) : this(
        screen,
        constructor,
        { icon },
    )

    fun isSelected() = screen.isSubclassOf(McScreen.self!!::class)

    fun create(gameProfile: GameProfile, profile: SkyBlockProfile? = null): BasePvScreen {
        return constructor.invoke(gameProfile, profile)
    }

    fun getIcon(gameProfile: GameProfile?): ItemStack {
        return icon.invoke(gameProfile)
    }
}
