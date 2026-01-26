package me.owdding.skyblockpv.screens.windowed.tabs.mining

import com.mojang.authlib.GameProfile
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.data.repo.SkullTextures
import me.owdding.skyblockpv.screens.PvTab
import me.owdding.skyblockpv.screens.windowed.BaseWindowedPvScreen
import me.owdding.skyblockpv.screens.windowed.tabs.base.AbstractCategorizedScreen
import me.owdding.skyblockpv.screens.windowed.tabs.base.Category
import net.minecraft.client.gui.screens.Screen
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.helpers.McScreen
import tech.thatgravyboat.skyblockapi.utils.extentions.toTitleCase
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

abstract class BaseMiningScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : AbstractCategorizedScreen("MINING", gameProfile, profile) {
    override val tab: PvTab = PvTab.MINING
    override val categories: List<Category> get() = Category.getCategories<MiningCategory>(profile)
}

enum class MiningCategory(
    val screen: KClass<out BaseMiningScreen>,
    override val icon: ItemStack,
    hoverName: String? = null,
    override val hideOnStranded: Boolean = false,
) : Category {
    MAIN(MainMiningScreen::class, Items.DIAMOND_PICKAXE.defaultInstance),
    GEAR(MiningGearScreen::class, Items.PRISMARINE_SHARD.defaultInstance, "Mining Gear"),
    HOTM(HotmScreen::class, SkullTextures.HOTM.skull, "HotM Tree", true),
    GLACITE(GlaciteScreen::class, Items.BLUE_ICE.defaultInstance, "Glacite Tunnels", true),
    ;

    override val hover: String = hoverName ?: name.toTitleCase()

    override val isSelected: Boolean get() = McScreen.self?.takeIf { it::class.isSubclassOf(screen) } != null
    override fun create(gameProfile: GameProfile, profile: SkyBlockProfile?): BaseWindowedPvScreen = screen.constructors.first().call(gameProfile, profile)

    override fun canDisplay(profile: SkyBlockProfile?): Boolean {
        if (!super.canDisplay(profile)) return false
        return when (this) {
            GLACITE -> profile?.glacite != null
            else -> true
        }
    }
}
