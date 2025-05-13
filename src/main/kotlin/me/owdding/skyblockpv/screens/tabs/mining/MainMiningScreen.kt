package me.owdding.skyblockpv.screens.tabs.mining

import com.mojang.authlib.GameProfile
import me.owdding.lib.builder.LayoutFactory
import me.owdding.lib.displays.*
import me.owdding.lib.extensions.shorten
import me.owdding.lib.extensions.sortByKey
import me.owdding.lib.extensions.toReadableTime
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.api.RemindersAPI
import me.owdding.skyblockpv.api.data.SkyBlockProfile
import me.owdding.skyblockpv.data.api.skills.Crystal
import me.owdding.skyblockpv.data.api.skills.MiningCore
import me.owdding.skyblockpv.data.api.skills.RockBracket
import me.owdding.skyblockpv.data.repo.EssenceData.addMiningPerk
import me.owdding.skyblockpv.data.repo.ForgeTimeData
import me.owdding.skyblockpv.utils.ChatUtils
import me.owdding.skyblockpv.utils.LayoutUtils.asScrollable
import me.owdding.skyblockpv.utils.LayoutUtils.centerHorizontally
import me.owdding.skyblockpv.utils.Utils.text
import me.owdding.skyblockpv.utils.Utils.whiteText
import me.owdding.skyblockpv.utils.components.PvWidgets
import net.minecraft.ChatFormatting
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.client.gui.layouts.LinearLayout
import tech.thatgravyboat.skyblockapi.api.remote.RepoItemsAPI
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.extentions.toTitleCase
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import java.text.SimpleDateFormat
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class MainMiningScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseMiningScreen(gameProfile, profile) {

    val nucleusRunCrystals = listOf(
        "jade_crystal",
        "amethyst_crystal",
        "topaz_crystal",
        "sapphire_crystal",
        "amber_crystal",
    )
    val glaciteCrystals = listOf(
        "aquamarine_crystal",
        "citrine_crystal",
        "peridot_crystal",
    )
    val crystals = listOf(
        *nucleusRunCrystals.toTypedArray(),
        "ruby_crystal",
        "jasper_crystal",
        "opal_crystal",
        *glaciteCrystals.toTypedArray(),
    )

    override fun getLayout(bg: DisplayWidget): Layout {
        val mining = profile.mining ?: return LayoutFactory.empty()

        val info = getInformation(profile)
        val powder = getPowder(mining)
        val crystal = getCrystal(mining).takeIf { mining.crystals.isNotEmpty() } ?: LayoutFactory.empty()
        val forge = getForge()

        return if (maxOf(info.width, powder.width) + maxOf(crystal.width, forge?.width ?: 0) > uiWidth) {
            LayoutFactory.vertical(5, 0.5f) {
                widget(info)
                widget(powder)
                widget(crystal)
                forge?.let { widget(it) }
            }.asScrollable(uiWidth, uiHeight)
        } else {
            LayoutFactory.horizontal(5, 0.5f) {
                vertical(5, 0.5f) {
                    widget(info)
                    widget(powder)
                }
                vertical(5, 0.5f) {
                    widget(crystal)
                    forge?.let { widget(it) }
                }
            }.asScrollable(uiWidth, uiHeight)
        }
    }

    private fun getInformation(profile: SkyBlockProfile) = PvWidgets.label(
        "Information",
        LayoutFactory.vertical(3) {
            val mining = profile.mining ?: return@vertical
            fun grayText(text: String) = display(Displays.text(text, color = { 0x555555u }, shadow = false))
            val totalRuns = mining.crystals.filter { it.key in nucleusRunCrystals }.minOfOrNull { it.value.totalPlaced } ?: 0
            val hotmLevel = mining.getHotmLevel()

            val oresMined = profile.petMilestones["ores_mined"] ?: 0
            val rockPet = RockBracket.getByOres(oresMined)

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
                    RockBracket.entries.map {
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
        SkyBlockPv.id("icon/item/clipboard"),
    )

    private fun getPowder(mining: MiningCore) = PvWidgets.label(
        "Powder",
        LayoutFactory.vertical(3) {
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
        LayoutFactory.vertical(5) {
            val width = uiWidth / 3

            val convertedElements = crystals.map { id ->
                val (name, crystal) = id to (mining.crystals[id] ?: Crystal("NOT_FOUND", 0, 0))
                val icon = RepoItemsAPI.getItem(name.uppercase()).let { Displays.item(it) }
                val state = ("§2✔".takeIf { crystal.state in listOf("FOUND", "PLACED") } ?: "§4❌").let {
                    Displays.padding(0, 0, 4, 0, Displays.text("§l$it"))
                }

                val display = Displays.background(SkyBlockPv.id("box/rounded_box_thin"), Displays.padding(2, listOf(icon, state).toRow(1))).withTooltip {
                    add("§l${name.toTitleCase()}")
                    add("§7State: ${crystal.state.toTitleCase()}")
                    add("§7Found: ${crystal.totalFound.toFormattedString()}")
                    if (crystal.totalPlaced > 0) {
                        add("§7Placed: ${crystal.totalPlaced.toFormattedString()}")
                    }
                }
                display.asWidget()
            }

            if (convertedElements.isEmpty()) return@vertical

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
        val forgeSlots = profile.forge?.slots ?: return null
        if (forgeSlots.isEmpty()) return null
        val quickForgeLevel = profile.mining?.nodes?.entries?.find { it.key == "forge_time" }?.value ?: 0

        return PvWidgets.label(
            "Forge",
            LayoutFactory.vertical(5) {
                forgeSlots.sortByKey().forEach { (index, slot) ->
                    val itemDisplay = Displays.item(slot.itemStack)

                    val timeRemaining = ForgeTimeData.getForgeTime(
                        slot.id,
                        quickForgeLevel,
                    ) + (slot.startTime - System.currentTimeMillis()).toDuration(DurationUnit.MILLISECONDS)

                    val timeDisplay = if (timeRemaining.inWholeMilliseconds <= 0) "§aReady"
                    else "§8${timeRemaining.toReadableTime()}"

                    val canSetReminder = isProfileOfUser() && timeRemaining.inWholeMilliseconds > 0

                    val display = listOf(
                        Displays.text("§8§lSlot $index", shadow = false),
                        Displays.padding(0, 0, -4, 0, itemDisplay),
                        Displays.text("§8${timeDisplay}", shadow = false),
                    ).toRow(1).withTooltip {
                        add("§l${slot.itemStack.hoverName?.stripped}")
                        add("§7Time Remaining: $timeDisplay")
                        add("§7Started: ${SimpleDateFormat("dd.MM HH:mm:ss").format(slot.startTime)}")
                        if (canSetReminder) {
                            add("")
                            add("§aClick to set a reminder")
                        }
                    }

                    val widget = if (!canSetReminder) display.asWidget()
                    else display.asButton {
                        ChatUtils.chat("Reminder set for ${slot.itemStack.hoverName?.stripped} to be ready in $timeDisplay")
                        RemindersAPI.addReminder(
                            "forge_slot_$index",
                            Text.join("Reminder: ", slot.itemStack.hoverName, " is ready!"),
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
