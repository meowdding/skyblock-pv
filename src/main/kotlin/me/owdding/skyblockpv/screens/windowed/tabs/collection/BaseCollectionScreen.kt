package me.owdding.skyblockpv.screens.windowed.tabs.collection

import com.mojang.authlib.GameProfile
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.screens.PvTab
import me.owdding.skyblockpv.screens.windowed.BaseWindowedPvScreen
import me.owdding.skyblockpv.screens.windowed.tabs.base.AbstractCategorizedScreen
import me.owdding.skyblockpv.screens.windowed.tabs.base.Category
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.api.remote.RepoItemsAPI
import tech.thatgravyboat.skyblockapi.helpers.McScreen
import tech.thatgravyboat.skyblockapi.utils.extentions.toTitleCase
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

abstract class BaseCollectionScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) :
    AbstractCategorizedScreen("COLLECTION", gameProfile, profile) {
    override val tab: PvTab = PvTab.COLLECTION
    override val categories: List<Category> get() = Category.getCategories<CollectionCategories>(profile)
}

enum class CollectionCategories(val screen: KClass<out BaseCollectionScreen>, override val icon: ItemStack, hoverName: String? = null) : Category {
    FARMING(CommonCollectionScreen::class, Items.GOLDEN_HOE.defaultInstance),
    MINING(CommonCollectionScreen::class, Items.STONE_PICKAXE.defaultInstance),
    COMBAT(CommonCollectionScreen::class, Items.STONE_SWORD.defaultInstance),
    FORAGING(CommonCollectionScreen::class, Items.JUNGLE_SAPLING.defaultInstance),
    FISHING(CommonCollectionScreen::class, Items.FISHING_ROD.defaultInstance),
    RIFT(CommonCollectionScreen::class, Items.MYCELIUM.defaultInstance),
    MINION(MinionScreen::class, RepoItemsAPI.getItem("SNOW_GENERATOR_12"))
    ;

    override val hover: String = hoverName ?: name.toTitleCase()

    override val isSelected: Boolean
        get() = McScreen.self?.let {
            if (it is CommonCollectionScreen) {
                return@let it.category == this.name
            } else return@let it::class.isSubclassOf(screen)
        } == true

    override fun create(gameProfile: GameProfile, profile: SkyBlockProfile?): BaseWindowedPvScreen =
        if (screen == CommonCollectionScreen::class) CommonCollectionScreen(gameProfile, profile, this.name) else screen.constructors.first()
            .call(gameProfile, profile)

    override fun canDisplay(profile: SkyBlockProfile?): Boolean {
        if (!super.canDisplay(profile)) return false
        return when (this) {
            MINION -> profile?.minions != null
            else -> profile?.collections != null
        }
    }

    companion object {

        fun createScreen(gameProfile: GameProfile, profile: SkyBlockProfile?): BaseWindowedPvScreen {
            if (profile?.collections == null) {
                return MINION.create(gameProfile, profile)
            }
            return FARMING.create(gameProfile, profile)
        }
    }
}

