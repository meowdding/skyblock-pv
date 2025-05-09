package me.owdding.skyblockpv.screens.tabs

import com.mojang.authlib.GameProfile
import earth.terrarium.olympus.client.components.buttons.Button
import earth.terrarium.olympus.client.layouts.Layouts
import earth.terrarium.olympus.client.layouts.LinearViewLayout
import me.owdding.lib.builder.LayoutBuilder.Companion.setPos
import me.owdding.lib.builder.LayoutFactory
import me.owdding.lib.displays.DisplayWidget
import me.owdding.lib.displays.Displays
import me.owdding.lib.extensions.round
import me.owdding.skyblockpv.api.data.SkyBlockProfile
import me.owdding.skyblockpv.data.SortedEntry
import me.owdding.skyblockpv.data.api.skills.Pet
import me.owdding.skyblockpv.screens.BasePvScreen
import me.owdding.skyblockpv.utils.LayoutUtils.asScrollable
import me.owdding.skyblockpv.utils.components.PvWidgets
import me.owdding.skyblockpv.utils.displays.ExtraDisplays
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.layouts.Layout
import tech.thatgravyboat.skyblockapi.api.remote.RepoItemsAPI
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.extentions.toTitleCase
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextColor

class PetScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BasePvScreen("PETS", gameProfile, profile) {

    private var selectedPet: Pet? = profile?.pets?.find { it.active }

    override fun create(bg: DisplayWidget) {
        val pets = profile.pets
        val sortedPets = pets.sortedWith(compareBy<Pet> { SortedEntry.RARITY.list.indexOf(it.tier) }.thenByDescending { it.exp })
        val leftColumnWidth = ((bg.width - 10) * 0.65).toInt()
        val rightColumnWidth = (bg.width - 10 - leftColumnWidth)

        LayoutFactory.horizontal(5) {
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
            (-1).takeUnless { pet == selectedPet } ?: TextColor.GREEN,
        )
        return Button()
            .withSize(22, 22)
            .withTexture(null)
            .withRenderer(DisplayWidget.displayRenderer(display))
            .withCallback {
                selectedPet = pet
                this.rebuildWidgets()
            }
    }

    private fun createInfoRow(width: Int) = LayoutFactory.vertical {
        val petInfo = LayoutFactory.vertical {
            string(Text.join("Name: ", selectedPet?.type?.toTitleCase() ?: "None"))
            val activePet = selectedPet ?: return@vertical

            string(Text.join("Level: ${activePet.level}"))
            string(Text.join("Exp: ${activePet.exp.toFormattedString()}"))

            if (activePet.progressToMax == 1f) {
                string(Text.join("Progress Max: Maxed"))
            } else {
                string(Text.join("Progress Next: ${(activePet.progressToNextLevel * 100).round()}%"))
                string(Text.join("Progress Max: ${(activePet.progressToMax * 100).round()}%"))
            }

            activePet.candyUsed.takeIf { it > 0 }?.let { string("Candy Used: $it") }

            val petItemStack = activePet.heldItem?.let { RepoItemsAPI.getItem(it) } ?: return@vertical
            horizontal(alignment = 0.5f) {
                string("Held Item: ")
                display(Displays.item(petItemStack, showTooltip = true))
            }
        }

        widget(PvWidgets.getTitleWidget("Pet Info", width))
        widget(PvWidgets.getMainContentWidget(petInfo, width))
    }
}

