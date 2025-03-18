package tech.thatgravyboat.skyblockpv.screens.tabs

import com.mojang.authlib.GameProfile
import earth.terrarium.olympus.client.components.buttons.Button
import earth.terrarium.olympus.client.layouts.Layouts
import earth.terrarium.olympus.client.layouts.LinearViewLayout
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.layouts.Layout
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockpv.SkyBlockPv
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.data.Pet
import tech.thatgravyboat.skyblockpv.data.SortedEntry
import tech.thatgravyboat.skyblockpv.screens.BasePvScreen
import tech.thatgravyboat.skyblockpv.utils.ExtraWidgetRenderers
import tech.thatgravyboat.skyblockpv.utils.LayoutBuild
import tech.thatgravyboat.skyblockpv.utils.LayoutBuilder.Companion.setPos
import tech.thatgravyboat.skyblockpv.utils.Utils
import tech.thatgravyboat.skyblockpv.utils.Utils.asScrollable
import tech.thatgravyboat.skyblockpv.utils.Utils.round
import tech.thatgravyboat.skyblockpv.utils.Utils.toTitleCase
import tech.thatgravyboat.skyblockpv.utils.displays.DisplayWidget
import tech.thatgravyboat.skyblockpv.utils.displays.Displays

class PetScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BasePvScreen("PETS", gameProfile, profile) {

    private var activePet: Pet? = null

    override fun create(bg: DisplayWidget) {
        val pets = profile?.pets ?: return
        val sortedPets = pets.sortedWith(compareBy<Pet> { SortedEntry.RARITY.list.indexOf(it.tier) }.thenByDescending { it.exp })
        val leftColumnWidth = ((bg.width - 10) * 0.65).toInt()
        val rightColumnWidth = (bg.width - 10 - leftColumnWidth).toInt()

        LayoutBuild.horizontal(5) {
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
        val texture = "inventory/inventory-1x1-highlighted".takeIf { pet == activePet } ?: "inventory/inventory-1x1"
        val display = Displays.background(SkyBlockPv.id(texture), Displays.padding(3, itemDisplay))
        return Button()
            .withSize(22, 22)
            .withTexture(null)
            .withRenderer(ExtraWidgetRenderers.display(display))
            .withCallback {
                activePet = pet
                this.rebuildWidgets()
            }
    }

    private fun createInfoRow(width: Int) = LayoutBuild.vertical {
        val petInfo = LayoutBuild.vertical {
            string(Text.join("Name: ", activePet?.type?.toTitleCase() ?: "None"))
            val activePet = activePet ?: return@vertical

            string(Text.join("Level: ${activePet.level}"))
            string(Text.join("Exp: ${activePet.exp.toFormattedString()}"))

            if (activePet.progressToMax == 1f) {
                string(Text.join("Progress Max: Maxed"))
            } else {
                string(Text.join("Progress Next: ${(activePet.progressToNextLevel * 100).round()}%"))
                string(Text.join("Progress Max: ${(activePet.progressToMax * 100).round()}%"))
            }
        }

        widget(Utils.getTitleWidget("Pet Info", width))
        widget(Utils.getMainContentWidget(petInfo, width))
    }
}

