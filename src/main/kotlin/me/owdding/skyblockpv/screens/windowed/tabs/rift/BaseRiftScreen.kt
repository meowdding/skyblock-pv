package me.owdding.skyblockpv.screens.windowed.tabs.rift

import com.mojang.authlib.GameProfile
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.screens.windowed.tabs.base.AbstractCategorizedScreen
import me.owdding.skyblockpv.screens.windowed.tabs.base.Category
import me.owdding.skyblockpv.utils.CatharsisSupport.withCatharsisId
import net.minecraft.client.gui.screens.Screen
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.helpers.McScreen
import tech.thatgravyboat.skyblockapi.utils.extentions.toTitleCase
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

abstract class BaseRiftScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : AbstractCategorizedScreen("RIFT", gameProfile, profile) {
    override val categories: List<Category> get() = Category.getCategories<RiftCategory>(profile)
}

enum class RiftCategory(val screen: KClass<out BaseRiftScreen>, override val icon: ItemStack, hoverName: String? = null) : Category {
    MAIN(MainRiftScreen::class, Items.LILAC.withCatharsisId("tab/rift/main")),
    INVENTORY(RiftInventoryScreen::class, Items.CHEST.withCatharsisId("tab/rift/inventory")),
    ENDER_CHEST(RiftEnderChestScreen::class, Items.ENDER_CHEST.withCatharsisId("tab/rift/ender_chest"))
    ;

    override val hover: String = hoverName ?: name.toTitleCase()

    override val isSelected: Boolean get() = McScreen.self?.takeIf { it::class.isSubclassOf(screen) } != null
    override fun create(gameProfile: GameProfile, profile: SkyBlockProfile?): Screen = screen.constructors.first().call(gameProfile, profile)

    override fun canDisplay(profile: SkyBlockProfile?): Boolean {
        if (!super.canDisplay(profile)) return false
        return when (this) {
            ENDER_CHEST -> profile?.rift?.inventory?.enderChest?.isNotEmpty() == true
            else -> true
        }
    }
}
