package tech.thatgravyboat.skyblockpv.screens

import com.mojang.authlib.GameProfile
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.helpers.McScreen
import tech.thatgravyboat.skyblockapi.utils.extentions.ItemUtils.createSkull
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.data.repo.SkullTextures
import tech.thatgravyboat.skyblockpv.screens.tabs.*
import tech.thatgravyboat.skyblockpv.screens.tabs.combat.BaseCombatScreen
import tech.thatgravyboat.skyblockpv.screens.tabs.combat.DungeonScreen
import tech.thatgravyboat.skyblockpv.screens.tabs.farming.BaseFarmingScreen
import tech.thatgravyboat.skyblockpv.screens.tabs.farming.FarmingScreen
import tech.thatgravyboat.skyblockpv.screens.tabs.inventory.BaseInventoryScreen
import tech.thatgravyboat.skyblockpv.screens.tabs.inventory.InventoryScreen
import tech.thatgravyboat.skyblockpv.screens.tabs.mining.BaseMiningScreen
import tech.thatgravyboat.skyblockpv.screens.tabs.mining.MainMiningScreen
import tech.thatgravyboat.skyblockpv.screens.tabs.museum.BaseMuseumScreen
import tech.thatgravyboat.skyblockpv.screens.tabs.museum.MuseumItemScreen
import tech.thatgravyboat.skyblockpv.screens.tabs.rift.BaseRiftScreen
import tech.thatgravyboat.skyblockpv.screens.tabs.rift.MainRiftScreen
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

enum class PvTab(
    private val screen: KClass<out BasePvScreen>,
    private val constructor: (GameProfile, SkyBlockProfile?) -> BasePvScreen,
    private val icon: (GameProfile?) -> ItemStack,
) {
    MAIN(MainScreen::class, ::MainScreen, { it?.let(::createSkull) ?: Items.PLAYER_HEAD.defaultInstance }),
    COMBAT(BaseCombatScreen::class, ::DungeonScreen, Items.DIAMOND_SWORD.defaultInstance),
    INVENTORY(BaseInventoryScreen::class, ::InventoryScreen, Items.CHEST.defaultInstance),
    COLLECTION(CollectionScreen::class, Items.ITEM_FRAME.defaultInstance),
    MINING(BaseMiningScreen::class, ::MainMiningScreen, Items.DIAMOND_PICKAXE.defaultInstance),
    FISHING(FishingScreen::class, Items.FISHING_ROD.defaultInstance),
    PETS(PetScreen::class, Items.BONE.defaultInstance),
    FARMING(BaseFarmingScreen::class, ::FarmingScreen, Items.WHEAT.defaultInstance),
    MUSEUM(BaseMuseumScreen::class, ::MuseumItemScreen, Items.GOLD_BLOCK.defaultInstance),
    CHOCOLATE_FACTORY(ChocolateFactoryScreen::class, SkullTextures.CHOCOLATE_FACTORY.skull),
    RIFT(BaseRiftScreen::class, ::MainRiftScreen, SkullTextures.RIFT.skull),
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

    fun isSelected() = McScreen.self?.takeIf { it::class.isSubclassOf(screen) } != null

    fun create(gameProfile: GameProfile, profile: SkyBlockProfile? = null): BasePvScreen {
        return constructor.invoke(gameProfile, profile)
    }

    fun getIcon(gameProfile: GameProfile?): ItemStack {
        return icon.invoke(gameProfile)
    }
}
