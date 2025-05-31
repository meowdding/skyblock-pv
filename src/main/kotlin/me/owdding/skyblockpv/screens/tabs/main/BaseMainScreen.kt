package me.owdding.skyblockpv.screens.tabs.main

import com.mojang.authlib.GameProfile
import me.owdding.skyblockpv.api.data.SkyBlockProfile
import me.owdding.skyblockpv.screens.tabs.base.AbstractCategorizedScreen
import me.owdding.skyblockpv.screens.tabs.base.Category
import net.minecraft.client.gui.screens.Screen
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.helpers.McScreen
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

abstract class BaseMainScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : AbstractCategorizedScreen("MAIN", gameProfile, profile) {
    override val categories: List<Category> get() = Category.getCategories<MainCategory>(profile)
}

enum class MainCategory(val screen: KClass<out BaseMainScreen>, override val icon: ItemStack) : Category {
    MAIN(MainScreen::class, Items.WRITABLE_BOOK.defaultInstance),
    NETWORTH(NetworthScreen::class, Items.SUNFLOWER.defaultInstance),
    ;

    override val isSelected: Boolean get() = McScreen.self?.takeIf { it::class.isSubclassOf(screen) } != null
    override fun create(gameProfile: GameProfile, profile: SkyBlockProfile?): Screen = screen.constructors.first().call(gameProfile, profile)
}
