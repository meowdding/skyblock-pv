package tech.thatgravyboat.skyblockpv.screens.tabs

import com.mojang.authlib.GameProfile
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockpv.SkyBlockPv
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.data.CFCodecs
import tech.thatgravyboat.skyblockpv.screens.BasePvScreen
import tech.thatgravyboat.skyblockpv.utils.LayoutBuild
import tech.thatgravyboat.skyblockpv.utils.LayoutBuilder.Companion.setPos
import tech.thatgravyboat.skyblockpv.utils.components.PvWidgets
import tech.thatgravyboat.skyblockpv.utils.displays.DisplayWidget
import tech.thatgravyboat.skyblockpv.utils.displays.Displays
import tech.thatgravyboat.skyblockpv.utils.displays.toRow

class ChocolateFactoryScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BasePvScreen("CF", gameProfile, profile) {

    override fun create(bg: DisplayWidget) {
        val profile = profile ?: return
        val cf = profile.chocolateFactoryData ?: return

        LayoutBuild.horizontal(5) {
            cf.employees.mapNotNull { data ->
                val item = CFCodecs.data?.textures?.find { it.id == data.name }?.createSkull() ?: return@mapNotNull null
                listOf(
                    Displays.item(item, 12, 12),
                    Displays.padding(0, 0, 2, 0, Displays.text(Text.of("${data.level}") { color = data.color })),
                ).toRow(1).let { display(Displays.background(SkyBlockPv.id("box/rounded_box_thin"), Displays.padding(2, it))) }
            }
        }.let {
            PvWidgets.label("Employees", it, 20)
        }.setPos(bg.x + 5, bg.y + 5).visitWidgets(this::addRenderableWidget)
    }
}
