package tech.thatgravyboat.skyblockpv.screens.tabs.mining

import com.mojang.authlib.GameProfile
import net.minecraft.client.gui.screens.Screen
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.helpers.McScreen
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.data.SkullTextures
import tech.thatgravyboat.skyblockpv.screens.tabs.base.AbstractCategorizedScreen
import tech.thatgravyboat.skyblockpv.screens.tabs.base.Category
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

val hotmIcon by lazy { SkullTextures.HOTM.createSkull() }

abstract class BaseMiningScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : AbstractCategorizedScreen("MINING", gameProfile, profile)  {
    override val categories: List<Category> get() = MiningCategory.entries
}

enum class MiningCategory(val screen: KClass<out BaseMiningScreen>, override val icon: ItemStack) : Category {
    MAIN(MainMiningScreen::class, Items.DIAMOND_PICKAXE.defaultInstance),
    GEAR(MiningGearScreen::class, Items.PRISMARINE_SHARD.defaultInstance),
    HOTM(HotmScreen::class, hotmIcon),
    GlACITE(GlaciteScreen::class, Items.BLUE_ICE.defaultInstance),
    ;

    override val isSelected: Boolean get() = McScreen.self?.takeIf { it::class.isSubclassOf(screen) } != null
    override fun create(gameProfile: GameProfile, profile: SkyBlockProfile?): Screen = screen.constructors.first().call(gameProfile, profile)
}
