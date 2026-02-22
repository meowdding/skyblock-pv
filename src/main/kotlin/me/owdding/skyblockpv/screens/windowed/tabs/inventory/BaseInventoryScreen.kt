package me.owdding.skyblockpv.screens.windowed.tabs.inventory

import com.mojang.authlib.GameProfile
import me.owdding.lib.builder.MIDDLE
import me.owdding.lib.displays.Display
import me.owdding.lib.displays.DisplayWidget
import me.owdding.lib.displays.Displays
import me.owdding.lib.displays.asWidget
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.data.repo.SkullTextures
import me.owdding.skyblockpv.screens.windowed.BaseWindowedPvScreen
import me.owdding.skyblockpv.screens.windowed.tabs.base.AbstractCategorizedScreen
import me.owdding.skyblockpv.screens.windowed.tabs.base.Category
import me.owdding.skyblockpv.utils.CatharsisSupport.withCatharsisId
import me.owdding.skyblockpv.utils.components.CarouselWidget
import me.owdding.skyblockpv.utils.components.PvLayouts
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.helpers.McScreen
import tech.thatgravyboat.skyblockapi.utils.extentions.toTitleCase
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

enum class InventoryCategory(val screen: KClass<out BaseWindowedPvScreen>, override val icon: ItemStack, hoverName: String? = null) : Category {
    INVENTORY(InventoryScreen::class, Items.CHEST.withCatharsisId("tab/inventory/inventory")),
    ENDER_CHEST(EnderChestScreen::class, Items.ENDER_CHEST.withCatharsisId("tab/inventory/ender_chest")),
    BACKPACK(BackpackScreen::class, SkullTextures.BACKPACK.skull.withCatharsisId("tab/inventory/backpack")),
    WARDROBE(WardrobeScreen::class, Items.LEATHER_CHESTPLATE.withCatharsisId("tab/inventory/wardrobe")),
    ACCESSORY(AccessoryScreen::class, SkullTextures.ACCESSORY_BAG.skull.withCatharsisId("tab/inventory/accessory_bag")),
    SACKS(SacksScreen::class, SkullTextures.SACKS.skull.withCatharsisId("tab/inventory/sacks")),
    MISC_BAGS(MiscBagScreen::class, Items.BUNDLE.withCatharsisId("tab/inventory/misc_bag")),
    ;

    override val hover: String = hoverName ?: name.toTitleCase()

    override val isSelected: Boolean get() = McScreen.self?.takeIf { it::class.isSubclassOf(screen) } != null
    override fun create(gameProfile: GameProfile, profile: SkyBlockProfile?) = screen.constructors.first().call(gameProfile, profile)
}

abstract class BaseInventoryScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : AbstractCategorizedScreen("INVENTORY", gameProfile, profile) {
    override val categories get() = InventoryCategory.entries
    protected fun List<ItemStack>?.orEmpty(size: Int) = this ?: List(size) { ItemStack.EMPTY }
}

abstract class BasePagedInventoryScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseInventoryScreen(gameProfile, profile) {

    protected var carousel: CarouselWidget? = null

    abstract fun getInventories(): List<Display>
    abstract fun getIcons(): List<ItemStack>
    open fun getExtraLine(): Display? = null

    open val itemStackSize = true

    override fun getLayout(bg: DisplayWidget) = PvLayouts.vertical(5, MIDDLE) {
        val inventories = getInventories()
        val icons = getIcons()

        carousel = CarouselWidget(
            inventories,
            carousel?.index ?: 0,
            246,
        )

        val buttonContainer = carousel!!.getIcons {
            List(inventories.size) { index ->
                val icon = icons[index]
                if (itemStackSize) icon.count = index + 1
                Displays.item(icon, showStackSize = true)
            }
        }

        widget(buttonContainer)
        spacer()
        widget(carousel!!)

        getExtraLine()?.let {
            spacer(height = 5)
            widget(it.asWidget())
        }
    }
}
