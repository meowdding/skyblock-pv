package me.owdding.skyblockpv.screens.tabs.collection

import com.mojang.authlib.GameProfile
import me.owdding.skyblockpv.api.data.SkyBlockProfile
import me.owdding.skyblockpv.screens.BasePvScreen
import me.owdding.skyblockpv.screens.tabs.base.AbstractCategorizedScreen
import me.owdding.skyblockpv.screens.tabs.base.Category
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.helpers.McScreen
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

abstract class BaseCollectionScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) :
    AbstractCategorizedScreen("COLLECTION", gameProfile, profile) {
    override val categories: List<Category> get() = CollectionCategories.entries
}

enum class CollectionCategories(val screen: KClass<out BaseCollectionScreen>, override val icon: ItemStack) : Category {
    FARMING(CommonCollectionScreen::class, Items.GOLDEN_HOE.defaultInstance),
    MINING(CommonCollectionScreen::class, Items.STONE_PICKAXE.defaultInstance),
    COMBAT(CommonCollectionScreen::class, Items.STONE_SWORD.defaultInstance),
    FORAGING(CommonCollectionScreen::class, Items.JUNGLE_SAPLING.defaultInstance),
    FISHING(CommonCollectionScreen::class, Items.FISHING_ROD.defaultInstance),
    RIFT(CommonCollectionScreen::class, Items.MYCELIUM.defaultInstance),
    ;

    override val isSelected: Boolean
        get() = McScreen.self?.let {
            if (it is CommonCollectionScreen) {
                return@let it.category == this.name
            } else return@let it::class.isSubclassOf(screen)
        } ?: false

    override fun create(gameProfile: GameProfile, profile: SkyBlockProfile?): BasePvScreen =
        if (screen == CommonCollectionScreen::class) CommonCollectionScreen(gameProfile, profile, this.name) else screen.constructors.first()
            .call(gameProfile, profile, this.name)
}

