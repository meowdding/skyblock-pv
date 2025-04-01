package tech.thatgravyboat.skyblockpv.screens.tabs.rift

import com.mojang.authlib.GameProfile
import net.minecraft.client.gui.screens.Screen
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.helpers.McScreen
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.screens.tabs.base.AbstractCategorizedScreen
import tech.thatgravyboat.skyblockpv.screens.tabs.base.Category
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

abstract class BaseRiftScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : AbstractCategorizedScreen("RIFT", gameProfile, profile) {
    override val categories: List<Category> get() = RiftCategory.entries
}

enum class RiftCategory(val screen: KClass<out BaseRiftScreen>, override val icon: ItemStack) : Category {
    MAIN(MainRiftScreen::class, Items.LILAC.defaultInstance),
    INVENTORY(RiftInventoryScreen::class, Items.CHEST.defaultInstance),
    ENDER_CHEST(RiftEnderChestScreen::class, Items.ENDER_CHEST.defaultInstance),
    ;

    override val isSelected: Boolean get() = McScreen.self?.takeIf { it::class.isSubclassOf(screen) } != null
    override fun create(gameProfile: GameProfile, profile: SkyBlockProfile?): Screen = screen.constructors.first().call(gameProfile, profile)
}
