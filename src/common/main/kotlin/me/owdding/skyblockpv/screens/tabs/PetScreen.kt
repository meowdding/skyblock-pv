package me.owdding.skyblockpv.screens.tabs

import com.mojang.authlib.GameProfile
import earth.terrarium.olympus.client.components.buttons.Button
import earth.terrarium.olympus.client.layouts.Layouts
import earth.terrarium.olympus.client.layouts.LinearViewLayout
import me.owdding.lib.builder.LayoutBuilder
import me.owdding.lib.builder.MIDDLE
import me.owdding.lib.displays.Display
import me.owdding.lib.displays.DisplayWidget
import me.owdding.lib.displays.Displays
import me.owdding.lib.displays.asWidget
import me.owdding.lib.extensions.round
import me.owdding.lib.extensions.shorten
import me.owdding.lib.layouts.setPos
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.api.data.SkyBlockProfile
import me.owdding.skyblockpv.data.SortedEntry
import me.owdding.skyblockpv.data.api.skills.Pet
import me.owdding.skyblockpv.screens.BasePvScreen
import me.owdding.skyblockpv.utils.LayoutUtils.asScrollable
import me.owdding.skyblockpv.utils.components.PvLayouts
import me.owdding.skyblockpv.utils.components.PvWidgets
import me.owdding.skyblockpv.utils.displays.ExtraDisplays
import me.owdding.skyblockpv.utils.theme.PvColors
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.client.gui.layouts.LayoutElement
import tech.thatgravyboat.skyblockapi.api.remote.RepoItemsAPI
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.extentions.toTitleCase
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

class PetScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BasePvScreen("PETS", gameProfile, profile) {

    private var selectedPet: Pet? = profile?.pets?.find { it.active }

    override fun create(bg: DisplayWidget) {
        val pets = profile.pets
        val sortedPets = pets.sortedWith(compareBy<Pet> { SortedEntry.RARITY.list.indexOf(it.tier) }.thenByDescending { it.exp })
        val leftColumnWidth = ((bg.width - 10) * 0.65).toInt()
        val rightColumnWidth = (bg.width - 10 - leftColumnWidth)

        PvLayouts.horizontal(5) {
            widget(createPetRow(sortedPets, leftColumnWidth).asScrollable(leftColumnWidth, uiHeight - 10))
            widget(createInfoRow(rightColumnWidth))
        }.setPos(bg.x + 5, bg.y + 5).visitWidgets(this::addRenderableWidget)
    }

    private fun createPetRow(pets: List<Pet>, width: Int): Layout {
        val amountPerRow = width / 28

        val petLayouts = pets.map { createPetLayout(it) }
        val petContainer = petLayouts.chunked(amountPerRow)
            .map { it.fold(Layouts.row().withGap(5), LinearViewLayout::withChild) }
            .fold(Layouts.column().withGap(5), LinearViewLayout::withChild)
        petContainer.arrangeElements()
        return petContainer
    }

    private fun createPetLayout(pet: Pet): AbstractWidget {
        val itemDisplay = Displays.item(pet.itemStack, showTooltip = true, customStackText = Text.of(pet.level.toString()).withColor(pet.rarity.color))
        val display = ExtraDisplays.inventorySlot(
            Displays.padding(3, itemDisplay),
            (-1).takeUnless { pet == selectedPet } ?: PvColors.GREEN,
        )
        return Button()
            .withSize(22, 22)
            .withTexture(null)
            .withRenderer(DisplayWidget.displayRenderer(display))
            .withCallback {
                selectedPet = pet
                this.safelyRebuild()
            }
    }

    private fun createInfoRow(width: Int) = PvWidgets.label(
        "Info",
        PvLayouts.vertical(spacing = 2) {
            val colon = ExtraDisplays.grayText(": ")
            val effectiveWidth = width - 20
            fun List<Display>.doesFit() = this.sumOf { it.getWidth() } <= effectiveWidth

            val nameText = ExtraDisplays.grayText("Name")
            val petName = ExtraDisplays.grayText(selectedPet?.type?.toTitleCase() ?: "Unknown")
            if (listOf(nameText, petName, colon).doesFit()) {
                horizontal {
                    display(nameText)
                    display(colon)
                    display(petName)
                }
            } else {
                vertical {
                    spacer(effectiveWidth)
                    display(nameText)
                    indentedDisplay(petName)
                }
            }
            val activePet = selectedPet ?: return@vertical

            string(Text.join("Level: ${activePet.level}"))

            val exp = ExtraDisplays.grayText("Exp: ${activePet.exp.toFormattedString()}")
            if (listOf(exp).doesFit()) {
                display(exp)
            } else {
                display(ExtraDisplays.grayText("Exp: ${activePet.exp.shorten(1)}"))
            }

            if (activePet.progressToMax == 1f) {
                val maxed = ExtraDisplays.grayText("Progress Max: Maxed")
                if (listOf(maxed).doesFit()) {
                    display(maxed)
                } else {
                    vertical {
                        spacer(effectiveWidth)
                        display(ExtraDisplays.grayText("Progress"))
                        indentedDisplay(ExtraDisplays.text(Text.of("MAXED") { this.color = PvColors.RED }, shadow = false))
                    }
                }
            } else {
                val progressNext = ExtraDisplays.grayText("Progress Next: ")
                val progressMax = ExtraDisplays.grayText("Progress Max: ")
                val progressNextPercentage = ExtraDisplays.grayText("${(activePet.progressToNextLevel * 100).round()}%")
                val progressMaxPercentage = ExtraDisplays.grayText("${(activePet.progressToMax * 100).round()}%")

                if (listOf(progressMax, progressMaxPercentage, colon).doesFit() && listOf(progressNext, progressNextPercentage, colon).doesFit()) {
                    vertical {
                        horizontal {
                            display(progressNext)
                            display(colon)
                            display(progressNextPercentage)
                        }
                        horizontal {
                            display(progressMax)
                            display(colon)
                            display(progressMaxPercentage)
                        }
                    }
                } else {
                    vertical {
                        spacer(effectiveWidth)
                        display(ExtraDisplays.grayText("Progress"))
                        indentedHorizonal {
                            string("Next: ")
                            display(progressNextPercentage)
                        }
                        indentedHorizonal {
                            string("Max: ")
                            display(progressMaxPercentage)
                        }
                    }
                }
            }

            activePet.candyUsed.takeIf { it > 0 }?.let {
                val number = ExtraDisplays.grayText(it.toString())
                val candies = ExtraDisplays.grayText("Candy Used")
                if (listOf(number, candies, colon).doesFit()) {
                    horizontal {
                        display(candies)
                        display(colon)
                        display(number)
                    }
                } else {
                    vertical {
                        spacer(effectiveWidth)
                        display(ExtraDisplays.grayText("Candy Used"))
                        indentedHorizonal {
                            display(number)
                            string("/10")
                        }
                    }
                }
            }

            val petItemStack = activePet.heldItem?.let { RepoItemsAPI.getItem(it) } ?: return@vertical
            val itemText = ExtraDisplays.grayText("Held Item")
            val itemDisplay = Displays.item(petItemStack, showTooltip = true)
            if (listOf(itemText, itemDisplay, colon).doesFit()) {
                horizontal(alignment = MIDDLE) {
                    display(itemText)
                    display(colon)
                    display(itemDisplay)
                }
            } else {
                horizontal {
                    spacer(effectiveWidth)
                    display(ExtraDisplays.grayText("Held Item"))
                    indentedDisplay(itemDisplay)
                }
            }
        },
        width = width,
        icon = SkyBlockPv.id("icon/item/clipboard"),
    )

    fun LayoutBuilder.indentedDisplay(display: Display) = this.indented(display.asWidget())

    fun LayoutBuilder.indentedHorizonal(spacing: Int = 0, alignment: Float = 0f, builder: LayoutBuilder.() -> Unit) =
        this.indented(PvLayouts.horizontal(spacing, alignment, builder))

    fun LayoutBuilder.indentedVertical(spacing: Int = 0, alignment: Float = 0f, builder: LayoutBuilder.() -> Unit) =
        this.indented(PvLayouts.vertical(spacing, alignment, builder))

    fun LayoutBuilder.indented(widget: LayoutElement) = horizontal { spacer(2); widget(widget) }


}

