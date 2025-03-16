package tech.thatgravyboat.skyblockpv.screens.tabs.mining

import com.mojang.authlib.GameProfile
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.client.gui.layouts.LinearLayout
import tech.thatgravyboat.skyblockapi.api.remote.SkyBlockItems
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.data.ForgeTimeData
import tech.thatgravyboat.skyblockpv.data.MiningCore
import tech.thatgravyboat.skyblockpv.utils.LayoutBuild
import tech.thatgravyboat.skyblockpv.utils.Utils.centerHorizontally
import tech.thatgravyboat.skyblockpv.utils.Utils.formatReadableTime
import tech.thatgravyboat.skyblockpv.utils.Utils.getMainContentWidget
import tech.thatgravyboat.skyblockpv.utils.Utils.getTitleWidget
import tech.thatgravyboat.skyblockpv.utils.Utils.shorten
import tech.thatgravyboat.skyblockpv.utils.Utils.toTitleCase
import tech.thatgravyboat.skyblockpv.utils.displays.*
import java.text.SimpleDateFormat
import kotlin.time.DurationUnit
import kotlin.time.toDuration


// TODO:
//  rock pet
//  separate page for hotm tree (@Sophie you promised :3)

class MainMiningScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseMiningScreen(gameProfile, profile) {

    override fun getLayout(): Layout {
        val mining = profile?.mining ?: return LayoutBuild.horizontal {  }
        val columnWidth = uiWidth / 2

        return LayoutBuild.horizontal(5) {
            widget(createLeftColumn(mining, columnWidth))
            widget(createRightColumn(mining, columnWidth))
        }
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
            val hotmLevel = mining.getHotmLevel()

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

    private fun createRightColumn(mining: MiningCore, width: Int) = LayoutBuild.vertical(alignment = 0.5f) {
        spacer(width, 5)

        val crystalContent = LayoutBuild.vertical(5) {
            val convertedElements = mining.crystals.map { (name, crystal) ->
                val icon = SkyBlockItems.getItemById(name.uppercase())?.let { Displays.item(it) } ?: Displays.text("§cFailed to load")
                val state = ("§2✔".takeIf { crystal.state in listOf("FOUND", "PLACED") } ?: "§4❌").let {
                    Displays.padding(0, 0, 4, 0, Displays.text("§l$it"))
                }

                val widget = listOf(icon, state).toRow(1).asWidget()
                widget.withTooltip(
                    Text.multiline(
                        "§l${name.toTitleCase()}",
                        "§7State: ${crystal.state.toTitleCase()}",
                        "§7Found: ${crystal.totalFound.toFormattedString()}",
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

        widget(getTitleWidget("Placed Crystals", width - 5))
        widget(getMainContentWidget(crystalContent, width - 5))


        val forgeSlots = profile?.forge?.slots ?: return@vertical
        if (forgeSlots.isEmpty()) return@vertical
        val quickForgeLevel = profile?.mining?.nodes?.entries?.find { it.key == "forge_time" }?.value ?: 0

        spacer(height = 5)

        val forgeContent = LayoutBuild.vertical(5) {
            forgeSlots.forEach { (index, slot) ->
                val item = SkyBlockItems.getItemById(slot.id)
                val itemDisplay = item?.let { Displays.item(it) } ?: Displays.text("§cFailed to load")
                val timeRemaining = (slot.startTime + ForgeTimeData.getForgeTime(slot.id, quickForgeLevel) - System.currentTimeMillis())
                    .toDuration(DurationUnit.MILLISECONDS)
                val timeDisplay = if (timeRemaining.inWholeMilliseconds <= 0) {
                    "§aReady"
                } else {
                    "§8${timeRemaining.formatReadableTime(DurationUnit.DAYS, 2)}"
                }
                val widget = listOf(
                    Displays.text("§8§lSlot $index", shadow = false),
                    Displays.padding(0, 0, -4, 0, itemDisplay),
                    Displays.text("§8${timeDisplay}", shadow = false),
                ).toRow(1).asWidget()
                widget.withTooltip(
                    Text.multiline(
                        "§l${item?.hoverName?.stripped}",
                        "§7Time Remaining: $timeDisplay",
                        "§7Started: ${SimpleDateFormat("dd.MM HH:mm:ss").format(slot.startTime)}",
                    ),
                )
                widget(widget)
            }
        }

        widget(getTitleWidget("Forge", width - 5))
        widget(getMainContentWidget(forgeContent, width - 5))
    }
}
