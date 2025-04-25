package me.owdding.skyblockpv.screens.tabs.combat

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

abstract class BaseCombatScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : AbstractCategorizedScreen("COMBAT", gameProfile, profile) {
    override val categories: List<Category> get() = CombatCategory.entries
}

enum class CombatCategory(val screen: KClass<out BaseCombatScreen>, override val icon: ItemStack) : Category {
    DUNGEONS(DungeonScreen::class, SkullTextures.DUNGEONS.skull),
    BESTIARY(BestiaryScreen::class, Items.WRITABLE_BOOK.defaultInstance),
    ISLE(CrimsonIsleScreen::class, Items.NETHERRACK.defaultInstance),
    MOBS(MobScreen::class, Items.ZOMBIE_HEAD.defaultInstance),
    ;

    override val isSelected: Boolean get() = McScreen.self?.takeIf { it::class.isSubclassOf(screen) } != null
    override fun create(gameProfile: GameProfile, profile: SkyBlockProfile?): Screen = screen.constructors.first().call(gameProfile, profile)
}
