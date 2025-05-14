package me.owdding.skyblockpv.screens.tabs.general

import com.mojang.authlib.GameProfile
import me.owdding.skyblockpv.api.data.SkyBlockProfile
import me.owdding.skyblockpv.screens.tabs.base.AbstractCategorizedLoadingScreen
import me.owdding.skyblockpv.screens.tabs.base.AbstractCategorizedScreen
import me.owdding.skyblockpv.screens.tabs.base.Category
import net.minecraft.client.gui.screens.Screen
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.helpers.McScreen
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

interface GeneralScreenMarker

abstract class BaseGeneralScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : AbstractCategorizedScreen("MAIN", gameProfile, profile), GeneralScreenMarker {
    override val categories: List<Category> get() = GeneralCategory.entries
}

abstract class BaseGeneralLoadingScreen<V>(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : AbstractCategorizedLoadingScreen<V>("MAIN", gameProfile, profile), GeneralScreenMarker {
    override val categories: List<Category> get() = GeneralCategory.entries
}

enum class GeneralCategory(val screen: KClass<out AbstractCategorizedScreen>, override val icon: ItemStack) : Category {
    MAIN(MainScreen::class, Items.PLAYER_HEAD.defaultInstance),
    AUCTION(AuctionsGeneralScreen::class, Items.BUNDLE.defaultInstance),
    ;

    override val isSelected: Boolean get() = McScreen.self?.takeIf { it::class.isSubclassOf(screen) } != null
    override fun create(gameProfile: GameProfile, profile: SkyBlockProfile?): Screen = screen.constructors.first().call(gameProfile, profile)
}
