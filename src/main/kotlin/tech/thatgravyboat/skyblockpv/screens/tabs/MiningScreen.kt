package tech.thatgravyboat.skyblockpv.screens.tabs

import com.mojang.authlib.GameProfile
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.data.MiningCore
import tech.thatgravyboat.skyblockpv.screens.BasePvScreen
import tech.thatgravyboat.skyblockpv.utils.LayoutBuild
import tech.thatgravyboat.skyblockpv.utils.Utils.getMainContentWidget
import tech.thatgravyboat.skyblockpv.utils.Utils.getTitleWidget
import tech.thatgravyboat.skyblockpv.utils.Utils.shorten
import tech.thatgravyboat.skyblockpv.utils.displays.DisplayWidget
import tech.thatgravyboat.skyblockpv.utils.displays.asTable
import tech.thatgravyboat.skyblockpv.utils.displays.asWidget

class MiningScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BasePvScreen("MINING", gameProfile, profile) {
    override fun create(bg: DisplayWidget) {
        val mining = profile?.mining ?: return
        val columnWidth = (bg.width - 10) / 2

        val columns = LayoutBuild.horizontal(5) {
            widget(createInfoColumn(mining, columnWidth))
            // TODO: hi sophie pls nodes graph here
        }

        columns.setPosition(bg.x + 5, bg.y + 5)
        columns.visitWidgets(this::addRenderableWidget)
    }

    private fun createInfoColumn(mining: MiningCore, width: Int) = LayoutBuild.vertical {
        spacer(width, 5)

        val powderTable = listOf(
            listOf("", "Current", "Total"),
            listOf("§2Mithril Powder", mining.powderMithril.shorten(), (mining.powderSpentMithril + mining.powderMithril).shorten()),
            listOf("§dGemstone Powder", mining.powderGemstone.shorten(), (mining.powderSpentGemstone + mining.powderGemstone).shorten()),
            listOf("§bGlacite Powder", mining.powderGlacite.shorten(), (mining.powderSpentGlacite + mining.powderGlacite).shorten()),
        ).asTable(5).asWidget()
        widget(getTitleWidget("Powder", width - 5))
        widget(getMainContentWidget(powderTable, width - 5))

    }
}
