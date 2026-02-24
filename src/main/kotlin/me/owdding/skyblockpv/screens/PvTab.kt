package me.owdding.skyblockpv.screens

import com.mojang.authlib.GameProfile
import me.owdding.lib.extensions.ItemUtils.createSkull
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.data.repo.SkullTextures
import me.owdding.skyblockpv.screens.windowed.BaseWindowedPvScreen
import me.owdding.skyblockpv.screens.windowed.tabs.ChocolateFactoryScreen
import me.owdding.skyblockpv.screens.windowed.tabs.FishingScreen
import me.owdding.skyblockpv.screens.windowed.tabs.MainScreen
import me.owdding.skyblockpv.screens.windowed.tabs.PetScreen
import me.owdding.skyblockpv.screens.windowed.tabs.base.Category
import me.owdding.skyblockpv.screens.windowed.tabs.collection.BaseCollectionScreen
import me.owdding.skyblockpv.screens.windowed.tabs.collection.CollectionCategories
import me.owdding.skyblockpv.screens.windowed.tabs.combat.BaseCombatScreen
import me.owdding.skyblockpv.screens.windowed.tabs.combat.BestiaryScreen
import me.owdding.skyblockpv.screens.windowed.tabs.combat.DungeonScreen
import me.owdding.skyblockpv.screens.windowed.tabs.farming.BaseFarmingScreen
import me.owdding.skyblockpv.screens.windowed.tabs.farming.FarmingScreen
import me.owdding.skyblockpv.screens.windowed.tabs.foraging.BaseForagingScreen
import me.owdding.skyblockpv.screens.windowed.tabs.foraging.MainForagingScreen
import me.owdding.skyblockpv.screens.windowed.tabs.inventory.BaseInventoryScreen
import me.owdding.skyblockpv.screens.windowed.tabs.inventory.InventoryScreen
import me.owdding.skyblockpv.screens.windowed.tabs.mining.BaseMiningScreen
import me.owdding.skyblockpv.screens.windowed.tabs.mining.MainMiningScreen
import me.owdding.skyblockpv.screens.windowed.tabs.mining.MiningCategory
import me.owdding.skyblockpv.screens.windowed.tabs.museum.BaseMuseumScreen
import me.owdding.skyblockpv.screens.windowed.tabs.museum.MuseumItemScreen
import me.owdding.skyblockpv.screens.windowed.tabs.rift.BaseRiftScreen
import me.owdding.skyblockpv.screens.windowed.tabs.rift.MainRiftScreen
import me.owdding.skyblockpv.screens.windowed.tabs.rift.RiftCategory
import me.owdding.skyblockpv.utils.CatharsisSupport.withCatharsisId
import me.owdding.skyblockpv.utils.PvPageState
import net.minecraft.util.TriState
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.helpers.McScreen
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

enum class PvTab(
    private val screen: KClass<out BaseWindowedPvScreen>,
    private val constructor: (GameProfile, SkyBlockProfile?) -> BaseWindowedPvScreen,
    private val icon: (GameProfile?) -> ItemStack,
    private val hideOnStranded: Boolean = false,
) : PvPageState {
    MAIN(MainScreen::class, ::MainScreen, { it?.let(::createSkull) ?: Items.PLAYER_HEAD.withCatharsisId("tab/main") }),
    COMBAT(
        BaseCombatScreen::class,
        { gameProfile, profile ->
            if (profile?.onStranded == true) {
                BestiaryScreen(gameProfile, profile)
            } else {
                DungeonScreen(gameProfile, profile)
            }
        },
        Items.DIAMOND_SWORD.withCatharsisId("tab/combat/icon"),
    ),
    INVENTORY(BaseInventoryScreen::class, ::InventoryScreen, Items.CHEST.withCatharsisId("tab/inventory/icon")),
    COLLECTION(BaseCollectionScreen::class, CollectionCategories::createScreen, Items.ITEM_FRAME.withCatharsisId("tab/collections/icon")),
    MINING(BaseMiningScreen::class, ::MainMiningScreen, Items.DIAMOND_PICKAXE.withCatharsisId("tab/mining/icon")),
    FISHING(FishingScreen::class, Items.FISHING_ROD.withCatharsisId("tab/fishing/icon")),
    FORAGING(BaseForagingScreen::class, ::MainForagingScreen, Items.OAK_WOOD.withCatharsisId("tab/foraging/icon")),
    PETS(PetScreen::class, Items.BONE.withCatharsisId("tab/pets/icon")),
    FARMING(BaseFarmingScreen::class, ::FarmingScreen, Items.WHEAT.withCatharsisId("tab/farming/icon")),
    MUSEUM(BaseMuseumScreen::class, ::MuseumItemScreen, Items.GOLD_BLOCK.withCatharsisId("tab/museum/icon"), true),
    CHOCOLATE_FACTORY(ChocolateFactoryScreen::class, SkullTextures.CHOCOLATE_FACTORY.skull.withCatharsisId("tab/chocolate_factory/icon")),
    RIFT(BaseRiftScreen::class, ::MainRiftScreen, SkullTextures.RIFT.skull.withCatharsisId("tab/rift/icon"), true),
    ;

    constructor(
        screen: KClass<out BaseWindowedPvScreen>,
        icon: ItemStack,
        hideOnStranded: Boolean = false,
    ) : this(
        screen,
        screen.java.getConstructor(GameProfile::class.java, SkyBlockProfile::class.java)::newInstance,
        { icon },
        hideOnStranded
    )

    constructor(
        screen: KClass<out BaseWindowedPvScreen>,
        constructor: (GameProfile, SkyBlockProfile?) -> BaseWindowedPvScreen,
        icon: ItemStack,
        hideOnStranded: Boolean = false,
    ) : this(
        screen,
        constructor,
        { icon },
        hideOnStranded
    )

    fun isSelected() = McScreen.self?.takeIf { it::class.isSubclassOf(screen) } != null

    fun getTabState(profile: SkyBlockProfile): TriState = when (this) {
        INVENTORY -> if (profile.inventory != null) TriState.TRUE else TriState.FALSE
        COLLECTION -> Category.getTabState<CollectionCategories>(profile)
        MINING -> Category.getTabState<MiningCategory>(profile)
        RIFT -> Category.getTabState<RiftCategory>(profile)
        else -> TriState.TRUE
    }

    override fun create(gameProfile: GameProfile, profile: SkyBlockProfile?): BaseWindowedPvScreen {
        return constructor.invoke(gameProfile, profile)
    }

    fun getIcon(gameProfile: GameProfile?): ItemStack {
        return icon(gameProfile)
    }

    override fun canDisplay(profile: SkyBlockProfile?): Boolean = !this.hideOnStranded || profile?.onStranded != true
}
