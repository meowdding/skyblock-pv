package tech.thatgravyboat.skyblockpv.screens.tabs.rift

import com.mojang.authlib.GameProfile
import earth.terrarium.olympus.client.utils.Orientation
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.minecraft.world.item.TooltipFlag
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.data.RiftCodecs
import tech.thatgravyboat.skyblockpv.data.RiftData
import tech.thatgravyboat.skyblockpv.utils.LayoutBuild
import tech.thatgravyboat.skyblockpv.utils.components.PvWidgets
import tech.thatgravyboat.skyblockpv.utils.displays.Displays
import tech.thatgravyboat.skyblockpv.utils.displays.toRow
import tech.thatgravyboat.skyblockpv.utils.displays.withTooltip

class MainRiftScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseRiftScreen(gameProfile, profile) {
    override fun getLayout() = LayoutBuild.vertical {
        val rift = profile?.rift ?: return@vertical
        val data = RiftCodecs.data ?: return@vertical

        string("meow :3")

        widget(getTrophy(rift, data))
    }

    private fun getTrophy(rift: RiftData, data: RiftCodecs.RiftRepoData) = PvWidgets.label(
        "Trophies",
        LayoutBuild.horizontal {
            data.trophies.map { trophy ->
                val unlocked = rift.trophies.any { trophy.id == it.type }
                val item = trophy.item.takeIf { unlocked } ?: Items.GRAY_DYE.defaultInstance
                val lore = trophy.item.getTooltipLines(Item.TooltipContext.EMPTY, null, TooltipFlag.NORMAL)
                Displays.padding(2, Displays.item(item).withTooltip(lore))
            }.toRow().let { display(Displays.inventoryBackground(8, Orientation.HORIZONTAL, Displays.padding(2, it))) }
        },
    )
}
