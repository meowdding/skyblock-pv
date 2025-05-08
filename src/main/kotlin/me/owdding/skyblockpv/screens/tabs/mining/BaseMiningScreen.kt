package me.owdding.skyblockpv.screens.tabs.mining

import com.mojang.authlib.GameProfile
import me.owdding.skyblockpv.api.data.SkyBlockProfile
import me.owdding.skyblockpv.data.repo.SkullTextures
import me.owdding.skyblockpv.screens.tabs.base.AbstractCategorizedScreen
import me.owdding.skyblockpv.screens.tabs.base.Category
import net.minecraft.client.gui.screens.Screen
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.helpers.McScreen
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

abstract class BaseMiningScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : AbstractCategorizedScreen("MINING", gameProfile, profile)  {
    override val categories: List<Category> get() = Category.getCategories<MiningCategory>(profile)
}

enum class MiningCategory(val screen: KClass<out BaseMiningScreen>, override val icon: ItemStack) : Category {
    MAIN(MainMiningScreen::class, Items.DIAMOND_PICKAXE.defaultInstance),
    GEAR(MiningGearScreen::class, Items.PRISMARINE_SHARD.defaultInstance),
    HOTM(HotmScreen::class, SkullTextures.HOTM.skull),
    GlACITE(GlaciteScreen::class, Items.BLUE_ICE.defaultInstance),
    ;

    override val isSelected: Boolean get() = McScreen.self?.takeIf { it::class.isSubclassOf(screen) } != null
    override fun create(gameProfile: GameProfile, profile: SkyBlockProfile?): Screen = screen.constructors.first().call(gameProfile, profile)

    override fun canDisplay(profile: SkyBlockProfile?) = when (this) {
        GlACITE -> profile?.glacite != null
        else -> true
    }
}
