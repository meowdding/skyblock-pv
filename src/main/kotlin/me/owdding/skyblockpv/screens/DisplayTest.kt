package me.owdding.skyblockpv.screens

import me.owdding.lib.builder.LayoutFactory
import me.owdding.lib.displays.Displays
import me.owdding.lib.displays.asWidget
import me.owdding.lib.displays.toRow
import me.owdding.lib.displays.withTooltip
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.CommonComponents
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.utils.text.Text

object DisplayTest : Screen(CommonComponents.EMPTY) {

    override fun init() {
        super.init()
        addRenderableWidget(Displays.item(Items.STRING).asWidget())
        LayoutFactory.vertical {
            listOf(
                Displays.item(Items.STRING),
                Displays.item(Items.DIAMOND),
                Displays.item(Items.NETHERRACK).withTooltip(Text.of("meow")),
            ).toRow().add()
            Displays.item(Items.DIAMOND).add()
            Displays.text("asdafsafa").add()
        }.also {
            it.setPosition(10, 10)
            it.arrangeElements()
        }.visitWidgets { addRenderableWidget(it) }
    }

}

