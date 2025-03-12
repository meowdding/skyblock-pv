package tech.thatgravyboat.skyblockpv.screens.tabs

import com.mojang.authlib.GameProfile
import net.minecraft.client.gui.layouts.LinearLayout
import tech.thatgravyboat.skyblockapi.api.remote.SkyBlockItems
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.data.MiningCore
import tech.thatgravyboat.skyblockpv.screens.BasePvScreen
import tech.thatgravyboat.skyblockpv.utils.LayoutBuild
import tech.thatgravyboat.skyblockpv.utils.Utils.centerHorizontally
import tech.thatgravyboat.skyblockpv.utils.Utils.getMainContentWidget
import tech.thatgravyboat.skyblockpv.utils.Utils.getTitleWidget
import tech.thatgravyboat.skyblockpv.utils.Utils.shorten
import tech.thatgravyboat.skyblockpv.utils.Utils.toTitleCase
import tech.thatgravyboat.skyblockpv.utils.displays.*


// TODO:
//  add forge
//  separate page for hotm tree (@Sophie you promised :3)

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
        val columnWidth = bg.width / 2

        val columns = LayoutBuild.horizontal(5) {
            widget(createLeftColumn(mining, columnWidth))
            widget(createRightColumn(mining, columnWidth))
        }

        columns.setPosition(bg.x, bg.y)
        columns.visitWidgets(this::addRenderableWidget)
    }

    private fun createLeftColumn(mining: MiningCore, width: Int) = LayoutBuild.vertical {
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
            listOf("§2Mithril", mining.powderMithril.shorten(), (mining.powderSpentMithril + mining.powderMithril).shorten()),
            listOf("§dGemstone", mining.powderGemstone.shorten(), (mining.powderSpentGemstone + mining.powderGemstone).shorten()),
            listOf("§bGlacite", mining.powderGlacite.shorten(), (mining.powderSpentGlacite + mining.powderGlacite).shorten()),
        ).asTable(5).asWidget()
        widget(getTitleWidget("Powder", width - 5))
        widget(getMainContentWidget(powderTable, width - 5))
    }

    private fun createRightColumn(mining: MiningCore, width: Int) = LayoutBuild.vertical {
        spacer(width, 5)

        val mainContent = LayoutBuild.vertical(5) {
            val convertedElements = mining.crystals.map { (name, crystal) ->
                val icon = SkyBlockItems.getItemById(name.uppercase())?.let { Displays.item(it, 12, 12) } ?: Displays.text("§cFailed to load")
                val state = ("§2✔".takeIf { crystal.state in listOf("FOUND", "PLACED") } ?: "§4❌").let {
                    Displays.text("§l$it")
                }

                val widget = listOf(icon, state).toRow(1).asWidget()
                widget.withTooltip(
                    Text.join(
                        "§l${name.toTitleCase()}\n",
                        "§7State: ${crystal.state.toTitleCase()}\n",
                        "§7Found: ${crystal.totalFound.toFormattedString()}\n",
                        "§7Placed: ${crystal.totalPlaced.toFormattedString()}",
                    ),
                )
            }

            val elementsPerRow = width / (convertedElements.first().width + 15)
            if (elementsPerRow < 1) return@vertical

            convertedElements.chunked(elementsPerRow).forEach { chunk ->
                val element = LinearLayout.horizontal().spacing(5)
                chunk.forEach { element.addChild(it) }
                widget(element.centerHorizontally(width))
            }
        }

        // TODO: fix the hardcoded .centerHori which is only needed here and nowhere else??
        widget(getTitleWidget("Placed Crystals", width - 5).centerHorizontally(width))
        widget(getMainContentWidget(mainContent, width - 5).centerHorizontally(width))
    }
}
