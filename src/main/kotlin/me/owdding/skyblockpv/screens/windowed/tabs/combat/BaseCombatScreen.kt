package me.owdding.skyblockpv.screens.windowed.tabs.combat

import com.mojang.authlib.GameProfile
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.data.repo.SkullTextures
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

abstract class BaseCombatScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : AbstractCategorizedScreen("COMBAT", gameProfile, profile) {
    override val categories: List<Category> get() = Category.getCategories<CombatCategory>(profile)
}

enum class CombatCategory(
    val screen: KClass<out BaseCombatScreen>,
    override val icon: ItemStack,
    hoverName: String? = null,
    override val hideOnStranded: Boolean = false,
) : Category {
    DUNGEONS(DungeonScreen::class, SkullTextures.DUNGEONS.skull.withCatharsisId("tab/combat/dungeons"), "Dungeons", true),
    BESTIARY(BestiaryScreen::class, Items.WRITABLE_BOOK.withCatharsisId("tab/combat/bestiary"), "Bestiary"),
    ISLE(CrimsonIsleScreen::class, Items.NETHERRACK.withCatharsisId("tab/combat/crimson_isle"), "Crimson Isle", true),
    MOBS(MobScreen::class, Items.ZOMBIE_HEAD.withCatharsisId("tab/combat/mobs"), "Kills and Deaths"),
    ;

    override val hover: String = hoverName ?: name.toTitleCase()

    override val isSelected: Boolean get() = McScreen.self?.takeIf { it::class.isSubclassOf(screen) } != null
    override fun create(gameProfile: GameProfile, profile: SkyBlockProfile?): Screen = screen.constructors.first().call(gameProfile, profile)
}
