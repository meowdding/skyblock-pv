package me.owdding.skyblockpv.screens.windowed.tabs.farming

import com.mojang.authlib.GameProfile
import me.owdding.skyblockpv.api.GardenAPI
import me.owdding.skyblockpv.api.data.SkyBlockProfile
import me.owdding.skyblockpv.data.api.skills.farming.GardenProfile
import me.owdding.skyblockpv.screens.windowed.tabs.base.AbstractCategorizedLoadingScreen
import me.owdding.skyblockpv.screens.windowed.tabs.base.Category
import net.minecraft.client.gui.screens.Screen
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.api.remote.RepoItemsAPI
import tech.thatgravyboat.skyblockapi.helpers.McScreen
import tech.thatgravyboat.skyblockapi.utils.extentions.toTitleCase
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

abstract class BaseFarmingScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) :
    AbstractCategorizedLoadingScreen<GardenProfile>("FARMING", gameProfile, profile) {

    override val api get() = GardenAPI
    override val categories: List<Category> get() = FarmingCategory.entries

}

enum class FarmingCategory(val screen: KClass<out BaseFarmingScreen>, override val icon: ItemStack, hoverName: String? = null) : Category {
    MAIN(FarmingScreen::class, Items.WHEAT.defaultInstance),
    VISITORS(VisitorScreen::class, Items.VILLAGER_SPAWN_EGG.defaultInstance),
    CROP(CropScreen::class, Items.CARROT.defaultInstance),
    COMPOSTER(ComposterScreen::class, RepoItemsAPI.getItem("COMPOST")),
    ;

    override val hover: String = hoverName ?: name.toTitleCase()

    override val isSelected: Boolean get() = McScreen.self?.takeIf { it::class.isSubclassOf(screen) } != null
    override fun create(gameProfile: GameProfile, profile: SkyBlockProfile?): Screen = screen.constructors.first().call(gameProfile, profile)

}
