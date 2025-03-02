package tech.thatgravyboat.skyblockpv.screens.tabs

import com.mojang.authlib.GameProfile
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.data.MiningCore
import tech.thatgravyboat.skyblockpv.screens.BasePvScreen
import tech.thatgravyboat.skyblockpv.utils.LayoutBuild
import tech.thatgravyboat.skyblockpv.utils.Utils.getMainContentWidget
import tech.thatgravyboat.skyblockpv.utils.Utils.getTitleWidget
import tech.thatgravyboat.skyblockpv.utils.Utils.shorten
import tech.thatgravyboat.skyblockpv.utils.displays.DisplayWidget
import tech.thatgravyboat.skyblockpv.utils.displays.Displays
import tech.thatgravyboat.skyblockpv.utils.displays.asTable
import tech.thatgravyboat.skyblockpv.utils.displays.asWidget

class MiningScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BasePvScreen("MINING", gameProfile, profile) {
    val levelToExp = mapOf(
        1 to 0,
        2 to 3_000,
        3 to 12_000,
        4 to 37_000,
        5 to 97_000,
        6 to 197_000,
        7 to 347_000,
        8 to 557_000,
        9 to 847_000,
        10 to 1_247_000,
    )

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

        val info = LayoutBuild.vertical(5) {
            fun grayText(text: String) = display(Displays.text(text, color = { 0x555555u }, shadow = false))
            val nucleusRunCrystals = listOf(
                "jade_crystal",
                "amethyst_crystal",
                "topaz_crystal",
                "sapphire_crystal",
                "amber_crystal",
            )
            val totalRuns = mining.crystals.filter { it.key in nucleusRunCrystals }.minOfOrNull { it.value.totalPlaced } ?: 0
            val hotmLevel = levelToExp.entries.findLast { it.value <= (profile?.mining?.experience ?: 0) }?.key ?: 0

            grayText("HotM: $hotmLevel")
            grayText("Total Runs: ${totalRuns.toFormattedString()}")
        }
        widget(getTitleWidget("Info", width - 5))
        widget(getMainContentWidget(info, width - 5))

        spacer(height = 5)

        val powderTable = listOf(
            listOf("", "Current", "Total"),
            listOf("§2Mithril Powder", mining.powderMithril.shorten(), (mining.powderSpentMithril + mining.powderMithril).shorten()),
            listOf("§dGemstone Powder", mining.powderGemstone.shorten(), (mining.powderSpentGemstone + mining.powderGemstone).shorten()),
            listOf("§bGlacite Powder", mining.powderGlacite.shorten(), (mining.powderSpentGlacite + mining.powderGlacite).shorten()),
        ).asTable(5).asWidget()
        widget(getTitleWidget("Powder", width - 5))
        widget(getMainContentWidget(powderTable, width - 5))

        spacer(height = 5)

        mining.crystals.map { (name, crystal) ->
            val formattedName = name.split("_").first().lowercase().replaceFirstChar { it.uppercase() }
            val state = "§2✔".takeIf { crystal.state == "FOUND" } ?: "§4❌"
            listOf(formattedName, "§l$state", "")
        }.chunked(2).map { it.flatten() }.asTable(5).asWidget().let {
            widget(getTitleWidget("Placed Crystals", width - 5))
            widget(getMainContentWidget(it, width - 5))
        }
    }
}
