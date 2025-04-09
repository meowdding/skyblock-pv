package tech.thatgravyboat.skyblockpv.screens.tabs.museum

import com.mojang.authlib.GameProfile
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.data.museum.MuseumArmor
import tech.thatgravyboat.skyblockpv.data.museum.MuseumData
import tech.thatgravyboat.skyblockpv.data.museum.RepoMuseumData
import tech.thatgravyboat.skyblockpv.utils.LayoutBuild
import tech.thatgravyboat.skyblockpv.utils.displays.*
import tech.thatgravyboat.skyblockpv.utils.withTooltip

class MuseumArmorScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) :
    BaseMuseumScreen(gameProfile, profile) {

    override fun getLayout(bg: DisplayWidget): Layout {
        return LayoutBuild.frame {
            val chunked = RepoMuseumData.armor.map {
                loaded(
                    Displays.empty(16, 16),
                    Displays.empty(16, 16),
                ) { data -> createArmor(it, data) }
            }.map { Displays.padding(2, it) }.chunked(15)

            display(
                Displays.inventoryBackground(
                    chunked.firstOrNull()?.size ?: 0, chunked.size,
                    Displays.padding(2, chunked.map { it.toRow() }.toColumn()),
                ),
            )
        }
    }

    private fun createArmor(museumArmor: MuseumArmor, data: MuseumData): Display {
        return data.items.find { it.id == museumArmor.id }?.let {
            Displays.item(it.stacks.first(), showTooltip = true)
        } ?: Displays.item(Items.GRAY_DYE.defaultInstance.withTooltip {

        }, showTooltip = true)
    }

}
