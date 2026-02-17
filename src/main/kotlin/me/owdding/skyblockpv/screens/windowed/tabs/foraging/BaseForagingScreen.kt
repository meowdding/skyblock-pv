package me.owdding.skyblockpv.screens.windowed.tabs.foraging

import com.mojang.authlib.GameProfile
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.data.repo.SkullTextures
import me.owdding.skyblockpv.screens.windowed.tabs.base.AbstractCategorizedScreen
import me.owdding.skyblockpv.screens.windowed.tabs.base.Category
import me.owdding.skyblockpv.screens.windowed.tabs.mining.MiningSkillTreeScreen
import net.minecraft.client.gui.screens.Screen
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.helpers.McScreen
import tech.thatgravyboat.skyblockapi.utils.extentions.toTitleCase

abstract class BaseForagingScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : AbstractCategorizedScreen("MINING", gameProfile, profile) {

    abstract val type: ForagingCategory

    override val categories: List<Category> get() = Category.getCategories<ForagingCategory>(profile)
}

enum class ForagingCategory(
    val screen: (GameProfile, SkyBlockProfile?) -> BaseForagingScreen,
    override val icon: ItemStack,
    hoverName: String? = null,
    override val hideOnStranded: Boolean = false,
) : Category {
    // contests and stuff probably on main page?
    HOTF(::ForagingSkillTreeScreen, SkullTextures.HOTM.skull, "HotF Tree", true),
    //ATTRIBUTES
    ;

    override val hover: String = hoverName ?: name.toTitleCase()

    override val isSelected: Boolean get() = (McScreen.self as? BaseForagingScreen)?.type == this
    override fun create(gameProfile: GameProfile, profile: SkyBlockProfile?): Screen = screen(gameProfile, profile)

    override fun canDisplay(profile: SkyBlockProfile?): Boolean {
        if (!super.canDisplay(profile)) return false
        return when (this) {
            else -> true
        }
    }
}
