package tech.thatgravyboat.skyblockpv.screens.tabs.farming

import com.mojang.authlib.GameProfile
import net.minecraft.client.gui.screens.Screen
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.helpers.McScreen
import tech.thatgravyboat.skyblockpv.api.GardenApi
import tech.thatgravyboat.skyblockpv.api.ItemAPI
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.data.api.skills.farming.GardenProfile
import tech.thatgravyboat.skyblockpv.screens.tabs.base.AbstractCategorizedLoadingScreen
import tech.thatgravyboat.skyblockpv.screens.tabs.base.Category
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

abstract class BaseFarmingScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : AbstractCategorizedLoadingScreen<GardenProfile>("FARMING", gameProfile, profile) {

    override val api get() = GardenApi
    override val categories: List<Category> get() = FarmingCategory.entries

}

enum class FarmingCategory(val screen: KClass<out BaseFarmingScreen>, override val icon: ItemStack) : Category {
    MAIN(FarmingScreen::class, Items.WHEAT.defaultInstance),
    VISITORS(VisitorScreen::class, Items.VILLAGER_SPAWN_EGG.defaultInstance),
    CROP(CropScreen::class, Items.CARROT.defaultInstance),
    COMPOSTER(ComposterScreen::class, ItemAPI.getItem("COMPOST")),
    ;

    override val isSelected: Boolean get() = McScreen.self?.takeIf { it::class.isSubclassOf(screen) } != null
    override fun create(gameProfile: GameProfile, profile: SkyBlockProfile?): Screen = screen.constructors.first().call(gameProfile, profile)

}
