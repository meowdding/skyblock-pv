package me.owdding.skyblockpv.screens.tabs.base

import com.mojang.authlib.GameProfile
import earth.terrarium.olympus.client.components.buttons.Button
import earth.terrarium.olympus.client.components.renderers.WidgetRenderers
import earth.terrarium.olympus.client.layouts.Layouts
import me.owdding.lib.displays.DisplayWidget
import me.owdding.lib.extensions.floorToHalf
import me.owdding.lib.layouts.Scalable
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.api.data.SkyBlockProfile
import me.owdding.skyblockpv.config.Config
import me.owdding.skyblockpv.screens.BasePvScreen
import me.owdding.skyblockpv.screens.elements.ExtraConstants
import me.owdding.skyblockpv.utils.components.PvWidgets
import net.minecraft.client.gui.layouts.FrameLayout
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.client.gui.screens.Screen
import net.minecraft.util.TriState
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.text.Text.asComponent
import kotlin.math.min

abstract class AbstractCategorizedScreen(name: String, gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BasePvScreen(name, gameProfile, profile) {

    abstract val categories: List<Category>

    abstract fun getLayout(bg: DisplayWidget): Layout

    final override fun create(bg: DisplayWidget) {
        val layout = getLayout(bg)
        val horizontalDelta = ((uiWidth - 20) / layout.width.toDouble()).floorToHalf()
        val verticalDelta = ((uiHeight - 20) / layout.height.toDouble()).floorToHalf()

        if (Config.displayScaling && horizontalDelta > 1 && verticalDelta > 1) {
            if (SkyBlockPv.isDevMode) {
                addRenderableWidget(PvWidgets.text("$verticalDelta x $horizontalDelta").withPosition(0, 100).withSize(100))
            }
            val min = min(horizontalDelta, verticalDelta)
            if (layout is Scalable) {
                layout.scale(min)
            } else {
                layout.arrangeElements()
            }
        } else {
            layout.arrangeElements()
        }
        FrameLayout.centerInRectangle(layout, bg.x, bg.y, uiWidth, uiHeight)
        layout.visitWidgets(this::addRenderableWidget)


        val x = if (Config.alignCategoryButtonsLeft) bg.x - 22 else bg.x + bg.width - 9
        val y = bg.y + 20

        this.categories.fold(Layouts.column().withGap(2)) { layout, category ->
            val button = Button().apply {
                withSize(31, 20)
                withCallback { McClient.setScreenAsync { category.create(gameProfile, profile) } }
                withTooltip(category.hover.asComponent())
            }

            if (Config.alignCategoryButtonsLeft) {
                button.withTexture(null)
                button.withRenderer(
                    WidgetRenderers.layered(
                        WidgetRenderers.sprite(if (category.isSelected) ExtraConstants.TAB_LEFT_SELECTED else ExtraConstants.TAB_LEFT),
                        WidgetRenderers.padded(
                            0, 9, 0, 4,
                            WidgetRenderers.center(16, 16) { gr, ctx, _ -> gr.renderItem(category.icon, ctx.x, ctx.y) },
                        ),
                    ),
                )
            } else {
                button.withTexture(null)
                button.withRenderer(
                    WidgetRenderers.layered(
                        WidgetRenderers.sprite(if (category.isSelected) ExtraConstants.TAB_RIGHT_SELECTED else ExtraConstants.TAB_RIGHT),
                        WidgetRenderers.padded(
                            0, 4, 0, 9,
                            WidgetRenderers.center(16, 16) { gr, ctx, _ -> gr.renderItem(category.icon, ctx.x, ctx.y) },
                        ),
                    ),
                )
            }

            layout.withChild(button)
        }.withPosition(x, y).build(this::addRenderableWidget)
    }

}

interface Category {

    val icon: ItemStack get() = ItemStack.EMPTY
    val isSelected: Boolean get() = false
    val hover: String get() = ""
    val hideOnStranded: Boolean get() = false

    fun create(gameProfile: GameProfile, profile: SkyBlockProfile? = null): Screen

    fun canDisplay(profile: SkyBlockProfile?): Boolean {
        return !(profile?.onStranded == true && this.hideOnStranded)
    }

    companion object {

        inline fun <reified T> getCategories(profile: SkyBlockProfile?): List<T> where T : Enum<T>, T : Category {
            return T::class.java.enumConstants.filter { it.canDisplay(profile) }
        }

        inline fun <reified T> getTabState(profile: SkyBlockProfile?): TriState where T : Enum<T>, T : Category {
            val visibleDisplays = getCategories<T>(profile)
            return when {
                visibleDisplays.isEmpty() -> TriState.FALSE
                visibleDisplays.size == T::class.java.enumConstants.count { it.hideOnStranded && profile?.onStranded == true } -> TriState.TRUE
                else -> TriState.DEFAULT
            }
        }
    }
}
