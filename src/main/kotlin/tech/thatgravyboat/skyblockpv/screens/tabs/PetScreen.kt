package tech.thatgravyboat.skyblockpv.screens.tabs

import com.mojang.authlib.GameProfile
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.api.data.SkyBlockRarity
import tech.thatgravyboat.skyblockapi.api.remote.SkyBlockItems
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.data.Pet
import tech.thatgravyboat.skyblockpv.data.SortedEntry
import tech.thatgravyboat.skyblockpv.screens.BasePvScreen
import tech.thatgravyboat.skyblockpv.utils.LayoutBuild
import tech.thatgravyboat.skyblockpv.utils.LayoutBuilder.Companion.setPos
import tech.thatgravyboat.skyblockpv.utils.displays.DisplayWidget
import tech.thatgravyboat.skyblockpv.utils.displays.Displays

class PetScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BasePvScreen("PETS", gameProfile, profile) {

    override fun create(bg: DisplayWidget) {
        val pets = profile?.pets ?: return
        val sortedPets = pets.sortedWith(compareBy({ SortedEntry.RARITY.list.indexOf(it.tier) }, { it.exp }))

        val petLayouts = sortedPets.map { createPetLayout(it) }.chunked(20)

        LayoutBuild.horizontal(5) {
            widget(petLayouts.map { LayoutBuild.vertical { widget(it) } })
        }.setPos(bg.x + 5, bg.y + 5).visitWidgets(this::addRenderableOnly)
    }

    private fun createPetLayout(pet: Pet) = LayoutBuild.horizontal(3, 0.5f) {
        val rarity = SkyBlockRarity.entries.find { it.name == pet.tier } ?: SkyBlockRarity.COMMON
        val itemStack = SkyBlockItems.getItemById(pet.type) ?: Items.BARRIER.defaultInstance

        display(Displays.item(itemStack))
        string(Text.of(pet.type) { color = rarity.color })
    }
}

