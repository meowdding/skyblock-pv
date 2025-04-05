package tech.thatgravyboat.skyblockpv.screens.tabs.combat

import com.mojang.authlib.GameProfile
import net.minecraft.client.gui.screens.Screen
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.helpers.McScreen
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.data.repo.SkullTextures
import tech.thatgravyboat.skyblockpv.screens.tabs.base.AbstractCategorizedScreen
import tech.thatgravyboat.skyblockpv.screens.tabs.base.Category
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

val dungeonsIcon by lazy { SkullTextures.DUNGEONS.createSkull() }

abstract class BaseCombatScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : AbstractCategorizedScreen("COMBAT", gameProfile, profile) {

    override val categories: List<Category> get() = CombatCategory.entries
}

enum class CombatCategory(val screen: KClass<out BaseCombatScreen>, override val icon: ItemStack) : Category {
    DUNGEONS(DungeonScreen::class, dungeonsIcon),
    MOBS(MobScreen::class, Items.ZOMBIE_HEAD.defaultInstance),
    ;

    override val isSelected: Boolean get() = McScreen.self?.takeIf { it::class.isSubclassOf(screen) } != null
    override fun create(gameProfile: GameProfile, profile: SkyBlockProfile?): Screen = screen.constructors.first().call(gameProfile, profile)
}
