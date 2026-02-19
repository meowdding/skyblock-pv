package me.owdding.skyblockpv.screens.windowed.tabs.mining

import com.mojang.authlib.GameProfile
import me.owdding.lib.builder.MIDDLE
import me.owdding.lib.displays.*
import me.owdding.lib.extensions.shorten
import me.owdding.lib.extensions.sortedByKeys
import me.owdding.lib.extensions.toReadableTime
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.api.RemindersAPI
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.data.api.skills.Crystal
import me.owdding.skyblockpv.data.api.skills.MiningCore
import me.owdding.skyblockpv.data.api.skills.RockBracket
import me.owdding.skyblockpv.data.repo.EssenceData.addMiningPerk
import me.owdding.skyblockpv.data.repo.ForgeTimeData
import me.owdding.skyblockpv.utils.ChatUtils.sendWithPrefix
import me.owdding.skyblockpv.utils.LayoutUtils.asScrollable
import me.owdding.skyblockpv.utils.LayoutUtils.centerHorizontally
import me.owdding.skyblockpv.utils.Utils.asTranslated
import me.owdding.skyblockpv.utils.Utils.text
import me.owdding.skyblockpv.utils.Utils.unaryPlus
import me.owdding.skyblockpv.utils.Utils.whiteText
import me.owdding.skyblockpv.utils.components.PvLayouts
import me.owdding.skyblockpv.utils.components.PvWidgets
import me.owdding.skyblockpv.utils.displays.ExtraDisplays
import me.owdding.skyblockpv.utils.displays.ExtraDisplays.asTable
import me.owdding.skyblockpv.utils.theme.PvColors
import me.owdding.skyblockpv.utils.theme.ThemeSupport
import net.minecraft.ChatFormatting
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.client.gui.layouts.LinearLayout
import tech.thatgravyboat.skyblockapi.api.remote.RepoItemsAPI
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.extentions.toTitleCase
import tech.thatgravyboat.skyblockapi.utils.text.Text
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
        "onyx_crystal",
    )
    val crystals = listOf(
        *nucleusRunCrystals.toTypedArray(),
        "ruby_crystal",
        "jasper_crystal",
        "opal_crystal",
        *glaciteCrystals.toTypedArray(),
    )

    override fun getLayout(bg: DisplayWidget): Layout {
        val mining = profile.mining ?: return PvLayouts.empty()

        val info = getInformation(profile)
        val powder = getPowder(mining)
        val crystal = getCrystal(mining).takeIf { mining.crystals.isNotEmpty() } ?: PvLayouts.empty()
        val forge = getForge()

        return if (maxOf(info.width, powder.width) + maxOf(crystal.width, forge?.width ?: 0) >= uiWidth - 5) {
            PvLayouts.vertical(5, MIDDLE) {
                widget(info)
                widget(powder)
                widget(crystal)
                forge?.let { widget(it) }
            }.asScrollable(uiWidth, uiHeight)
        } else {
            PvLayouts.horizontal(2, MIDDLE) {
                vertical(5, MIDDLE) {
                    widget(info)
                    widget(powder)
                }
                vertical(5, MIDDLE) {
                    widget(crystal)
                    forge?.let { widget(it) }
                }
            }.asScrollable(uiWidth, uiHeight)
        }
    }

    private fun getInformation(profile: SkyBlockProfile) = PvWidgets.label(
        "Information",
        PvLayouts.vertical(3) {

            val oresMined = profile.petMilestones["ores_mined"] ?: 0
            val rockPet = RockBracket.getByOres(oresMined)

            val rockPetDisplay = ExtraDisplays.text(
                Text.join(
                    Text.of("Rock Pet: ") { this.color = PvColors.DARK_GRAY },
                    rockPet?.rarity?.displayText ?: Text.of("None") { this.color = PvColors.RED },
                ),
                shadow = false,
            ).withTooltip(
                Text.join(
                    Text.of("Ores Mined: ") { this.color = PvColors.WHITE },
                    Text.of(oresMined.toFormattedString()) { this.color = PvColors.AQUA },
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
            )

            if (!profile.onStranded) {

                val mining = profile.mining ?: return@vertical
                val tree = profile.skillTrees?.mining
                fun grayText(text: String) = display(ExtraDisplays.grayText(text))
                val totalRuns = mining.crystals.filter { it.key in nucleusRunCrystals }.minOfOrNull { it.value.totalPlaced } ?: 0
                val hotmLevel = tree?.getTreeLevel() ?: 0

                grayText("HotM: $hotmLevel")
                grayText("Total Runs: ${totalRuns.toFormattedString()}")

                display(rockPetDisplay)

                addMiningPerk(profile, "fungus_fortuna")
                addMiningPerk(profile, "harena_fortuna")
                addMiningPerk(profile, "treasures_of_the_earth")
                addMiningPerk(profile, "dwarven_training")
                addMiningPerk(profile, "eager_miner")
                addMiningPerk(profile, "rhinestone_infusion")
                addMiningPerk(profile, "high_roller")
                addMiningPerk(profile, "return_to_sender")

            } else {
                display(rockPetDisplay)
            }
        },
        padding = 10,
        icon = SkyBlockPv.id("icon/item/clipboard"),
    )

    private fun getPowder(mining: MiningCore) = PvWidgets.label(
        "Powder",
        PvLayouts.vertical(3) {
            display(
                listOf(
                    listOf("", "Current", "Total"),
                    listOf(
                        Text.of("Mithril") { this.color = PvColors.DARK_GREEN },
                        mining.powderMithril.shorten(),
                        (mining.powderSpentMithril + mining.powderMithril).shorten(),
                    ),
                    listOf(
                        Text.of("Gemstone") { this.color = PvColors.LIGHT_PURPLE },
                        mining.powderGemstone.shorten(),
                        (mining.powderSpentGemstone + mining.powderGemstone).shorten(),
                    ),
                    listOf(
                        Text.of("Glacite") { this.color = PvColors.AQUA },
                        mining.powderGlacite.shorten(),
                        (mining.powderSpentGlacite + mining.powderGlacite).shorten(),
                    ),
                ).asTable(5),
            )
        },
        padding = 10,
    )

    private fun getCrystal(mining: MiningCore) = PvWidgets.label(
        "Crystals",
        PvLayouts.vertical(5) {
            val width = uiWidth / 3

            val convertedElements = crystals.map { id ->
                val (name, crystal) = id to (mining.crystals[id] ?: Crystal.EMPTY)
                val icon = RepoItemsAPI.getItem(name.uppercase()).let { Displays.item(it) }
                val state = ("§2✔".takeIf { crystal.state in listOf("FOUND", "PLACED") } ?: "§4❌").let {
                    Displays.padding(0, 0, 4, 0, Displays.text("§l$it"))
                }

                val display =
                    Displays.background(ThemeSupport.texture(SkyBlockPv.id("box/rounded_box_thin")), Displays.padding(2, listOf(icon, state).toRow(1)))
                        .withTooltip {
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
        val quickForgeLevel = profile.skillTrees?.mining?.nodes?.entries?.find { it.key == "forge_time" }?.value ?: 0

        return PvWidgets.label(
            "Forge",
            PvLayouts.vertical(5) {
                forgeSlots.sortedByKeys().forEach { (index, slot) ->
                    val itemDisplay = Displays.item(slot.itemStack)

                    val timeRemaining = ForgeTimeData.getForgeTime(
                        slot.id,
                        quickForgeLevel,
                    ) + (slot.startTime - System.currentTimeMillis()).toDuration(DurationUnit.MILLISECONDS)

                    val timeDisplay = if (timeRemaining.inWholeMilliseconds <= 0) "§aReady"
                    else "§7${timeRemaining.toReadableTime()}"

                    val defaultConditions = isProfileOfUser() && timeRemaining.inWholeMilliseconds > 0
                    val canSetReminder = defaultConditions || SkyBlockPv.isSuperUser

                    val display = listOf(
                        ExtraDisplays.text("§lSlot $index", color = { PvColors.DARK_GRAY.toUInt() }, shadow = false),
                        Displays.padding(0, 0, -4, 0, itemDisplay),
                        ExtraDisplays.text(timeDisplay, color = { PvColors.DARK_GRAY.toUInt() }, shadow = false),
                    ).toRow(1).withTooltip {
                        add("§l${slot.itemStack.hoverName.stripped}")
                        add("§7Time Remaining: $timeDisplay")
                        add("§7Started: ${SimpleDateFormat("dd.MM HH:mm:ss").format(slot.startTime)}")
                        if (canSetReminder) {
                            add("")
                            add("§aClick to set a reminder")
                            if (!defaultConditions) {
                                add("§7Checks bypassed by being a super user :3")
                            }
                        }
                    }

                    val widget = if (!canSetReminder) display.asWidget()
                    else display.asButtonLeft {
                        if (timeRemaining.inWholeMilliseconds <= 0) {
                            (+"messages.forge.already_finished").sendWithPrefix()
                            return@asButtonLeft
                        }

                        val name = slot.itemStack.hoverName ?: Text.of("Slot $index") { this.color = PvColors.GRAY }

                        "messages.forge.reminder_set".asTranslated(name, timeDisplay).sendWithPrefix()

                        RemindersAPI.addReminder(
                            "forge_slot_$index",
                            "messages.forge.reminder".asTranslated(name),
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
