package tech.thatgravyboat.skyblockpv.screens.tabs.inventory

import com.mojang.authlib.GameProfile
import earth.terrarium.olympus.client.components.buttons.Button
import earth.terrarium.olympus.client.components.renderers.WidgetRenderers
import earth.terrarium.olympus.client.layouts.Layouts
import earth.terrarium.olympus.client.layouts.LinearViewLayout
import earth.terrarium.olympus.client.ui.UIConstants
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.helpers.McScreen
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.data.repo.SkullTextures
import tech.thatgravyboat.skyblockpv.screens.BasePvScreen
import tech.thatgravyboat.skyblockpv.screens.tabs.base.AbstractCategorizedScreen
import tech.thatgravyboat.skyblockpv.screens.tabs.base.Category
import tech.thatgravyboat.skyblockpv.utils.ExtraWidgetRenderers
import tech.thatgravyboat.skyblockpv.utils.LayoutBuild
import tech.thatgravyboat.skyblockpv.utils.LayoutUtils.centerHorizontally
import tech.thatgravyboat.skyblockpv.utils.components.CarouselWidget
import tech.thatgravyboat.skyblockpv.utils.displays.Display
import tech.thatgravyboat.skyblockpv.utils.displays.DisplayWidget
import tech.thatgravyboat.skyblockpv.utils.displays.Displays
import tech.thatgravyboat.skyblockpv.utils.displays.asWidget
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

enum class InventoryCategory(val screen: KClass<out BasePvScreen>, override val icon: ItemStack) : Category {
    INVENTORY(InventoryScreen::class, Items.CHEST.defaultInstance),
    ENDER_CHEST(EnderChestScreen::class, Items.ENDER_CHEST.defaultInstance),
    BACKPACK(BackpackScreen::class, SkullTextures.BACKPACK.skull),
    WARDROBE(WardrobeScreen::class, Items.LEATHER_CHESTPLATE.defaultInstance),
    ACCESSORY(AccessoryScreen::class, SkullTextures.ACCESSORY_BAG.skull),
    SACKS(SacksScreen::class, SkullTextures.SACKS.skull),
    PERSONAL_VAULT(ItemVaultScreen::class, SkullTextures.PERSONAL_VAULT.skull),
    POTION_BAG(PotionBagScreen::class, Items.POTION.defaultInstance),
    FISHING_BAG(FishingBagScreen::class, Items.FISHING_ROD.defaultInstance),
    QUIVER_BAG(QuiverBagScreen::class, Items.ARROW.defaultInstance),
    ;

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

    override fun getLayout(bg: DisplayWidget) = LayoutBuild.vertical {
        val inventories = getInventories()
        val icons = getIcons()

        carousel = CarouselWidget(
            inventories,
            carousel?.index ?: 0,
            246,
        )

        val buttons = List(inventories.size) { index ->
            val icon = icons[index]
            if (itemStackSize) icon.count = index + 1
            val itemDisplay = Displays.item(icon, showStackSize = true)

            Button()
                .withSize(20, 20)
                .withRenderer(
                    WidgetRenderers.layered(
                        ExtraWidgetRenderers.conditional(
                            WidgetRenderers.sprite(UIConstants.PRIMARY_BUTTON),
                            WidgetRenderers.sprite(UIConstants.DARK_BUTTON),
                        ) { index == carousel?.index },
                        WidgetRenderers.center(16, 20, ExtraWidgetRenderers.display(itemDisplay)),
                    ),
                )
                .withCallback {
                    carousel?.index = index
                }
        }

        val buttonContainer = buttons.chunked(9)
            .map { it.fold(Layouts.row().withGap(1), LinearViewLayout::withChild) }
            .fold(Layouts.column().withGap(1), LinearViewLayout::withChild)

        widget(buttonContainer.centerHorizontally(uiWidth))
        spacer(height = 10)
        widget(carousel!!.centerHorizontally(uiWidth))

        getExtraLine()?.let {
            spacer(height = 5)
            widget(it.asWidget().centerHorizontally(uiWidth))
        }
    }
}
