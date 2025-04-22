package me.owdding.skyblockpv.screens.tabs.base

import com.mojang.authlib.GameProfile
import earth.terrarium.olympus.client.components.buttons.Button
import earth.terrarium.olympus.client.components.renderers.WidgetRenderers
import earth.terrarium.olympus.client.layouts.Layouts
import me.owdding.lib.displays.DisplayWidget
import me.owdding.skyblockpv.api.data.SkyBlockProfile
import me.owdding.skyblockpv.screens.BasePvScreen
import me.owdding.skyblockpv.screens.elements.ExtraConstants
import net.minecraft.client.gui.layouts.FrameLayout
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.client.gui.screens.Screen
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.helpers.McClient

abstract class AbstractCategorizedScreen(name: String, gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BasePvScreen(name, gameProfile, profile) {

    abstract val categories: List<Category>

    abstract fun getLayout(bg: DisplayWidget): Layout

    final override fun create(bg: DisplayWidget) {
        profile ?: return

        val layout = getLayout(bg)
        layout.arrangeElements()
        FrameLayout.centerInRectangle(layout, bg.x, bg.y, uiWidth, uiHeight)
        layout.visitWidgets(this::addRenderableWidget)


        this.categories.fold(Layouts.row().withGap(2)) { layout, category ->
            val button = Button()
                .withSize(20, 31)
                .withTexture(if (category.isSelected) ExtraConstants.TAB_TOP_SELECTED else ExtraConstants.TAB_TOP)
                .withCallback { McClient.tell { McClient.setScreen(category.create(gameProfile, profile)) } }
                .withRenderer(
                    WidgetRenderers.padded(
                        4, 0, 9, 0,
                        WidgetRenderers.center(16, 16) { gr, ctx, _ -> gr.renderItem(category.icon, ctx.x, ctx.y) },
                    ),
                )

            button.active = !category.isSelected
            layout.withChild(button)
        }.withPosition(bg.x + 20, bg.y - 22).build(this::addRenderableWidget)
    }

}

interface Category {

    val icon: ItemStack get() = ItemStack.EMPTY
    val isSelected: Boolean get() = false

    fun create(gameProfile: GameProfile, profile: SkyBlockProfile? = null): Screen
}
