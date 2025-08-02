package me.owdding.skyblockpv.screens

import com.mojang.authlib.GameProfile
import me.owdding.lib.extensions.ItemUtils.createSkull
import me.owdding.skyblockpv.api.data.SkyBlockProfile
import me.owdding.skyblockpv.data.repo.SkullTextures
import me.owdding.skyblockpv.screens.tabs.ChocolateFactoryScreen
import me.owdding.skyblockpv.screens.tabs.FishingScreen
import me.owdding.skyblockpv.screens.tabs.MainScreen
import me.owdding.skyblockpv.screens.tabs.PetScreen
import me.owdding.skyblockpv.screens.tabs.base.Category
import me.owdding.skyblockpv.screens.tabs.collection.BaseCollectionScreen
import me.owdding.skyblockpv.screens.tabs.collection.CollectionCategories
import me.owdding.skyblockpv.screens.tabs.combat.BaseCombatScreen
import me.owdding.skyblockpv.screens.tabs.combat.BestiaryScreen
import me.owdding.skyblockpv.screens.tabs.combat.DungeonScreen
import me.owdding.skyblockpv.screens.tabs.farming.BaseFarmingScreen
import me.owdding.skyblockpv.screens.tabs.farming.FarmingScreen
import me.owdding.skyblockpv.screens.tabs.inventory.BaseInventoryScreen
import me.owdding.skyblockpv.screens.tabs.inventory.InventoryScreen
import me.owdding.skyblockpv.screens.tabs.mining.BaseMiningScreen
import me.owdding.skyblockpv.screens.tabs.mining.MainMiningScreen
import me.owdding.skyblockpv.screens.tabs.mining.MiningCategory
import me.owdding.skyblockpv.screens.tabs.museum.BaseMuseumScreen
import me.owdding.skyblockpv.screens.tabs.museum.MuseumItemScreen
import me.owdding.skyblockpv.screens.tabs.rift.BaseRiftScreen
import me.owdding.skyblockpv.screens.tabs.rift.MainRiftScreen
import me.owdding.skyblockpv.screens.tabs.rift.RiftCategory
import net.minecraft.util.TriState
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.helpers.McScreen
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

enum class PvTab(
    private val screen: KClass<out BasePvScreen>,
    private val constructor: (GameProfile, SkyBlockProfile?) -> BasePvScreen,
    private val icon: (GameProfile?) -> ItemStack,
    val hideOnStranded: Boolean = false,
) {
    MAIN(MainScreen::class, ::MainScreen, { it?.let(::createSkull) ?: Items.PLAYER_HEAD.defaultInstance }),
    COMBAT(BaseCombatScreen::class, { gameProfile, profile ->
        if (profile?.onStranded == true) {
            BestiaryScreen(gameProfile, profile)
        } else {
            DungeonScreen(gameProfile, profile)
        }
    }, Items.DIAMOND_SWORD.defaultInstance),
    INVENTORY(BaseInventoryScreen::class, ::InventoryScreen, Items.CHEST.defaultInstance),
    COLLECTION(BaseCollectionScreen::class, CollectionCategories::createScreen, Items.ITEM_FRAME.defaultInstance),
    MINING(BaseMiningScreen::class, ::MainMiningScreen, Items.DIAMOND_PICKAXE.defaultInstance),
    FISHING(FishingScreen::class, Items.FISHING_ROD.defaultInstance),
    PETS(PetScreen::class, Items.BONE.defaultInstance),
    FARMING(BaseFarmingScreen::class, ::FarmingScreen, Items.WHEAT.defaultInstance),
    MUSEUM(BaseMuseumScreen::class, ::MuseumItemScreen, Items.GOLD_BLOCK.defaultInstance, true),
    CHOCOLATE_FACTORY(ChocolateFactoryScreen::class, SkullTextures.CHOCOLATE_FACTORY.skull),
    RIFT(BaseRiftScreen::class, ::MainRiftScreen, SkullTextures.RIFT.skull, true),
    ;

    constructor(
        screen: KClass<out BasePvScreen>,
        icon: ItemStack,
        hideOnStranded: Boolean = false
    ) : this(
        screen,
        screen.java.getConstructor(GameProfile::class.java, SkyBlockProfile::class.java)::newInstance,
        { icon },
        hideOnStranded
    )

    constructor(
        screen: KClass<out BasePvScreen>,
        constructor: (GameProfile, SkyBlockProfile?) -> BasePvScreen,
        icon: ItemStack,
        hideOnStranded: Boolean = false
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

    fun create(gameProfile: GameProfile, profile: SkyBlockProfile? = null): BasePvScreen {
        return constructor.invoke(gameProfile, profile)
    }

    fun getIcon(gameProfile: GameProfile?): ItemStack {
        return icon(gameProfile)
    }
}
