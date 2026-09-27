package me.owdding.skyblockpv.screens.windowed.tabs.cf

import com.mojang.authlib.GameProfile
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.screens.PvTab
import me.owdding.skyblockpv.screens.windowed.BaseWindowedPvScreen
import me.owdding.skyblockpv.screens.windowed.tabs.base.AbstractCategorizedScreen
import me.owdding.skyblockpv.screens.windowed.tabs.base.Category
import me.owdding.skyblockpv.utils.CatharsisSupport.withCatharsisId
import me.owdding.skyblockpv.utils.Utils.lookup
import net.minecraft.core.registries.Registries
import net.minecraft.world.entity.raid.Raid
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.helpers.McScreen
import tech.thatgravyboat.skyblockapi.utils.extentions.toTitleCase
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

abstract class BaseCfScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : AbstractCategorizedScreen("COLLECTION", gameProfile, profile) {
    override val categories: List<Category> = CfCategory.entries


    override val tab: PvTab = PvTab.CHOCOLATE_FACTORY
}

enum class CfCategory(val screen: KClass<out BaseWindowedPvScreen>, override val icon: ItemStack, hoverName: String? = null) : Category {
    MAIN(ChocolateFactoryScreen::class, Items.COOKIE.withCatharsisId("tab/cf/main")),
    FACTIONS(FactionCfScreen::class, Raid.getOminousBannerInstance(Registries.BANNER_PATTERN.lookup())),
    ;

    override val hover: String = hoverName ?: name.toTitleCase()

    override val isSelected: Boolean get() = McScreen.self?.takeIf { it::class.isSubclassOf(screen) } != null
    override fun create(gameProfile: GameProfile, profile: SkyBlockProfile?) = screen.constructors.first().call(gameProfile, profile)
}
