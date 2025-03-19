package tech.thatgravyboat.skyblockpv.screens.tabs.farming

import com.mojang.authlib.GameProfile
import net.minecraft.client.gui.layouts.Layout
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.data.StaticGardenData
import tech.thatgravyboat.skyblockpv.utils.LayoutBuild
import tech.thatgravyboat.skyblockpv.utils.LayoutBuilder
import kotlin.jvm.optionals.getOrNull

class FarmingScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseFarmingScreen(gameProfile, profile) {
    override fun getLayout(): Layout {
        val data = gardenData?.getOrNull()

        return LayoutBuild.frame {
            string(StaticGardenData.barnSkins[data?.selectedBarnSkin]?.displayName?: "Unknown :(")

        }
    }

    fun LayoutBuilder.string(message: String?) {
        val data = gardenData
        if (data == null) {
            string(Text.of("Loading...") { this.color = TextColor.RED })
            return
        }

        if (data.isEmpty) {
            string(Text.of("Failed Loading") { this.color = TextColor.DARK_RED })
        }

        if (message == null) {
            string(Text.of("null") { this.color = TextColor.GRAY })
        }

        string(Text.of(message!!) { this.color = TextColor.GRAY })
    }
}
