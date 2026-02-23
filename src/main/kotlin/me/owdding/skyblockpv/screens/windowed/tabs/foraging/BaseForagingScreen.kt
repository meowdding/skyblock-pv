package me.owdding.skyblockpv.screens.windowed.tabs.foraging

import com.mojang.authlib.GameProfile
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.data.repo.SkullTextures
import me.owdding.skyblockpv.screens.PvTab
import me.owdding.skyblockpv.screens.windowed.BaseWindowedPvScreen
import me.owdding.skyblockpv.screens.windowed.tabs.base.AbstractCategorizedScreen
import me.owdding.skyblockpv.screens.windowed.tabs.base.Category
import me.owdding.skyblockpv.utils.CatharsisSupport.withCatharsisId
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.api.remote.api.RepoAttributeAPI
import tech.thatgravyboat.skyblockapi.helpers.McScreen
import tech.thatgravyboat.skyblockapi.utils.extentions.toTitleCase

abstract class BaseForagingScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : AbstractCategorizedScreen("FORAGING", gameProfile, profile) {
    override val tab: PvTab = PvTab.FORAGING

    abstract val type: ForagingCategory

    override val categories: List<Category> get() = Category.getCategories<ForagingCategory>(profile)
}

private val attributesItem: ItemStack by lazy {
    (RepoAttributeAPI.getAttributeByIdOrNull("r43") ?: Items.BARRIER.defaultInstance)
        .withCatharsisId("tab/foraging/attributes")
}

enum class ForagingCategory(
    val screen: (GameProfile, SkyBlockProfile?) -> BaseForagingScreen,
    override val icon: ItemStack,
    hoverName: String? = null,
    override val hideOnStranded: Boolean = false,
) : Category {
    MAIN(::MainForagingScreen, Items.OAK_WOOD.withCatharsisId("tab/foraging/main")),
    HOTF(::ForagingSkillTreeScreen, SkullTextures.HOTF.skull.withCatharsisId("tab/foraging/hotf"), "HotF Tree", true),
    ATTRIBUTES(::AttributeScreen, attributesItem, "Attributes", true),
    ;

    override val hover: String = hoverName ?: name.toTitleCase()

    override val isSelected: Boolean get() = (McScreen.self as? BaseForagingScreen)?.type == this
    override fun create(gameProfile: GameProfile, profile: SkyBlockProfile?): BaseWindowedPvScreen = screen(gameProfile, profile)

    override fun canDisplay(profile: SkyBlockProfile?): Boolean {
        if (!super.canDisplay(profile)) return false
        return when (this) {
            else -> true
        }
    }
}
