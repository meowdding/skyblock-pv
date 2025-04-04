package tech.thatgravyboat.skyblockpv.screens.tabs.mining

import com.mojang.authlib.GameProfile
import net.minecraft.ChatFormatting
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.client.gui.layouts.LinearLayout
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockpv.SkyBlockPv
import tech.thatgravyboat.skyblockpv.api.ItemAPI
import tech.thatgravyboat.skyblockpv.api.RemindersAPI
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.data.EssenceData.addMiningPerk
import tech.thatgravyboat.skyblockpv.data.skills.mining.ForgeTimeData
import tech.thatgravyboat.skyblockpv.data.skills.mining.MiningCore
import tech.thatgravyboat.skyblockpv.data.skills.mining.RockBrackets
import tech.thatgravyboat.skyblockpv.utils.ChatUtils
import tech.thatgravyboat.skyblockpv.utils.LayoutBuild
import tech.thatgravyboat.skyblockpv.utils.LayoutUtils.asScrollable
import tech.thatgravyboat.skyblockpv.utils.LayoutUtils.centerHorizontally
import tech.thatgravyboat.skyblockpv.utils.Utils.formatReadableTime
import tech.thatgravyboat.skyblockpv.utils.Utils.shorten
import tech.thatgravyboat.skyblockpv.utils.Utils.sortByKey
import tech.thatgravyboat.skyblockpv.utils.Utils.text
import tech.thatgravyboat.skyblockpv.utils.Utils.toTitleCase
import tech.thatgravyboat.skyblockpv.utils.Utils.whiteText
import tech.thatgravyboat.skyblockpv.utils.components.PvWidgets
import tech.thatgravyboat.skyblockpv.utils.displays.*
import java.text.SimpleDateFormat
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class MainMiningScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseMiningScreen(gameProfile, profile) {

    override fun getLayout(bg: DisplayWidget): Layout {
        val profile = profile ?: return LayoutBuild.horizontal { }
        val mining = profile.mining ?: return LayoutBuild.horizontal { }

        val info = getInformation(profile)
        val powder = getPowder(mining)
        val crystal = getCrystal(mining)
        val forge = getForge()

        return if (maxOf(info.width, powder.width) + maxOf(crystal.width, forge?.width ?: 0) > uiWidth) {
            LayoutBuild.vertical(5, 0.5f) {
                widget(info)
                widget(powder)
                widget(crystal)
                forge?.let { widget(it) }
            }.asScrollable(uiWidth, uiHeight)
        } else {
            LayoutBuild.horizontal(5, 0.5f) {
                vertical(5, 0.5f) {
                    widget(info)
                    widget(powder)
                }
                vertical(5, 0.5f) {
                    widget(crystal)
                    forge?.let { widget(it) }
                }
            }
        }
    }

    private fun getInformation(profile: SkyBlockProfile) = PvWidgets.label(
        "Information",
        LayoutBuild.vertical(3) {
            val mining = profile.mining ?: return@vertical
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

            val oresMined = profile.petMilestones["ores_mined"] ?: 0
            val rockPet = RockBrackets.getByOres(oresMined)

            grayText("HotM: $hotmLevel")
            grayText("Total Runs: ${totalRuns.toFormattedString()}")

            display(
                Displays.text(
                    Text.join(
                        Text.of("Rock Pet: ") { this.color = TextColor.DARK_GRAY },
                        rockPet?.rarity?.displayText ?: Text.of("None") { this.color = TextColor.RED },
                    ),
                    shadow = false,
                ).withTooltip(
                    Text.join(
                        Text.of("Ores Mined: ") { this.color = TextColor.WHITE },
                        Text.of(oresMined.toFormattedString()) { this.color = TextColor.AQUA },
                    ),
                    "",
                    RockBrackets.entries.map {
                        whiteText {
                            val hasObtained = it.oresRequired <= oresMined
                            if (!hasObtained) {
                                withStyle(ChatFormatting.STRIKETHROUGH)
                                withStyle(ChatFormatting.DARK_GRAY)
                            }
                            append(
                                text("${it.rarity.displayName} Rock") {
                                    if (hasObtained) {
                                        withColor((it.rarity.color))
                                    }
                                },
                            )
                            append("!")
                        }
                    },
                ),
            )

            addMiningPerk(profile, "fungus_fortuna")
            addMiningPerk(profile, "harena_fortuna")
            addMiningPerk(profile, "treasures_of_the_earth")
            addMiningPerk(profile, "dwarven_training")
            addMiningPerk(profile, "eager_miner")
            addMiningPerk(profile, "rhinestone_infusion")
            addMiningPerk(profile, "high_roller")
            addMiningPerk(profile, "return_to_sender")
        },
        padding = 10,
    )

    private fun getPowder(mining: MiningCore) = PvWidgets.label(
        "Powder",
        LayoutBuild.vertical(3) {
            display(
                listOf(
                    listOf("", "Current", "Total"),
                    listOf("§2Mithril", mining.powderMithril.shorten(), (mining.powderSpentMithril + mining.powderMithril).shorten()),
                    listOf("§dGemstone", mining.powderGemstone.shorten(), (mining.powderSpentGemstone + mining.powderGemstone).shorten()),
                    listOf("§bGlacite", mining.powderGlacite.shorten(), (mining.powderSpentGlacite + mining.powderGlacite).shorten()),
                ).asTable(5),
            )
        },
        padding = 10,
    )

    private fun getCrystal(mining: MiningCore) = PvWidgets.label(
        "Crystals",
        LayoutBuild.vertical(5) {
            val width = uiWidth / 3

            val convertedElements = mining.crystals.map { (name, crystal) ->
                val icon = ItemAPI.getItem(name.uppercase()).let { Displays.item(it) }
                val state = ("§2✔".takeIf { crystal.state in listOf("FOUND", "PLACED") } ?: "§4❌").let {
                    Displays.padding(0, 0, 4, 0, Displays.text("§l$it"))
                }

                val display = Displays.background(SkyBlockPv.id("box/rounded_box_thin"), Displays.padding(2, listOf(icon, state).toRow(1)))
                display.withTooltip {
                    add("§l${name.toTitleCase()}")
                    add("§7State: ${crystal.state.toTitleCase()}")
                    add("§7Found: ${crystal.totalFound.toFormattedString()}")
                    if (crystal.totalPlaced > 0) {
                        add("§7Placed: ${crystal.totalPlaced.toFormattedString()}")
                    }
                }
                display.asWidget()
            }

            val elementsPerRow = width / (convertedElements.first().width + 5)
            if (elementsPerRow < 1) return@vertical

            convertedElements.chunked(elementsPerRow).forEach { chunk ->
                val element = LinearLayout.horizontal().spacing(5)
                chunk.forEach { element.addChild(it) }
                widget(element.centerHorizontally(width))
            }
        },
        padding = 10,
    )

    private fun getForge(): Layout? {
        val forgeSlots = profile?.forge?.slots ?: return null
        if (forgeSlots.isEmpty()) return null
        val quickForgeLevel = profile?.mining?.nodes?.entries?.find { it.key == "forge_time" }?.value ?: 0

        return PvWidgets.label(
            "Forge",
            LayoutBuild.vertical(5) {
                forgeSlots.sortByKey().forEach { (index, slot) ->
                    val itemDisplay = Displays.item(slot.itemStack)

                    val timeRemaining = ForgeTimeData.getForgeTime(
                        slot.id,
                        quickForgeLevel,
                    ) + (slot.startTime - System.currentTimeMillis()).toDuration(DurationUnit.MILLISECONDS)

                    val timeDisplay = if (timeRemaining.inWholeMilliseconds <= 0) "§aReady"
                    else "§8${timeRemaining.formatReadableTime(DurationUnit.DAYS, 2)}"

                    val display = listOf(
                        Displays.text("§8§lSlot $index", shadow = false),
                        Displays.padding(0, 0, -4, 0, itemDisplay),
                        Displays.text("§8${timeDisplay}", shadow = false),
                    ).toRow(1).withTooltip {
                        add("§l${slot.itemStack.hoverName?.stripped}")
                        add("§7Time Remaining: $timeDisplay")
                        add("§7Started: ${SimpleDateFormat("dd.MM HH:mm:ss").format(slot.startTime)}")
                        if (isProfileOfUser()) {
                            add("")
                            add("§aClick to set a reminder")
                        }
                    }

                    val widget = if (!isProfileOfUser()) display.asWidget()
                    else display.asButton {
                        ChatUtils.chat("Reminder set for ${slot.itemStack.hoverName?.stripped} to be ready in $timeDisplay")
                        RemindersAPI.addReminder(
                            "forge_slot_$index",
                            Text.of("Reminder: ${slot.itemStack.hoverName?.stripped} is ready!"),
                            System.currentTimeMillis() + timeRemaining.inWholeMilliseconds,
                        )
                    }

                    widget(widget)
                }
            },
            padding = 10,
        )
    }
}
