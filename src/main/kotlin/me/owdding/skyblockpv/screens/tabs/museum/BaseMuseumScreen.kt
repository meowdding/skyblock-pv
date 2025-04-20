package me.owdding.skyblockpv.screens.tabs.museum

import com.mojang.authlib.GameProfile
import me.owdding.skyblockpv.api.MuseumAPI
import me.owdding.skyblockpv.api.data.SkyBlockProfile
import me.owdding.skyblockpv.data.museum.MuseumData
import me.owdding.skyblockpv.screens.tabs.base.AbstractCategorizedLoadingScreen
import me.owdding.skyblockpv.screens.tabs.base.Category
import net.minecraft.client.gui.screens.Screen
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.helpers.McScreen
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

abstract class BaseMuseumScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) :
    AbstractCategorizedLoadingScreen<MuseumData>("MUSEUM", gameProfile, profile) {

    override val api get() = MuseumAPI
    override val categories = MuseumCategory.entries

}

enum class MuseumCategory(val screen: KClass<out BaseMuseumScreen>, override val icon: ItemStack) : Category {
    WEAPONS(MuseumItemScreen::class, Items.EMERALD.defaultInstance),
    ARMOR(MuseumArmorScreen::class, Items.NETHERITE_HELMET.defaultInstance)
    ;

    override val isSelected: Boolean get() = McScreen.self?.takeIf { it::class.isSubclassOf(screen) } != null
    override fun create(gameProfile: GameProfile, profile: SkyBlockProfile?): Screen = screen.constructors.first().call(gameProfile, profile)

}
