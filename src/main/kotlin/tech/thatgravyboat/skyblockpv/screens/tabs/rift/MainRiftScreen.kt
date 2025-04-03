package tech.thatgravyboat.skyblockpv.screens.tabs.rift

import com.mojang.authlib.GameProfile
import earth.terrarium.olympus.client.utils.Orientation
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.minecraft.world.item.TooltipFlag
import tech.thatgravyboat.skyblockapi.utils.text.CommonText
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
import tech.thatgravyboat.skyblockpv.data.RiftCodecs
import tech.thatgravyboat.skyblockpv.data.RiftData
import tech.thatgravyboat.skyblockpv.utils.LayoutBuild
import tech.thatgravyboat.skyblockpv.utils.Utils.append
import tech.thatgravyboat.skyblockpv.utils.Utils.toReadableString
import tech.thatgravyboat.skyblockpv.utils.components.PvWidgets
import tech.thatgravyboat.skyblockpv.utils.displays.DisplayWidget
import tech.thatgravyboat.skyblockpv.utils.displays.Displays
import tech.thatgravyboat.skyblockpv.utils.displays.TooltipBuilder
import tech.thatgravyboat.skyblockpv.utils.displays.toRow
import tech.thatgravyboat.skyblockpv.utils.displays.withTooltip
import java.time.Instant

class MainRiftScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseRiftScreen(gameProfile, profile) {
    override fun getLayout(bg: DisplayWidget) = LayoutBuild.vertical {
        val rift = profile?.rift ?: return@vertical
        val data = RiftCodecs.data ?: return@vertical

        string("meow :3")

        widget(getTrophy(rift, data))
    }

    private fun getTrophy(rift: RiftData, data: RiftCodecs.RiftRepoData) = PvWidgets.label(
        "Trophies",
        LayoutBuild.horizontal {
            data.trophies.map { trophy ->
                val ingameTrophy = rift.trophies.find { it.type == trophy.id }
                val unlocked = ingameTrophy != null
                val item = trophy.item.takeIf { unlocked } ?: Items.GRAY_DYE.defaultInstance
                val lore = TooltipBuilder(trophy.item.getTooltipLines(Item.TooltipContext.EMPTY, null, TooltipFlag.NORMAL)).apply {
                    if (!unlocked) return@apply
                    add(CommonText.EMPTY)
                    add("Found at visits: ") {
                        color = TextColor.GRAY
                        append("${ingameTrophy.visits}") {
                            color = TextColor.GREEN
                        }
                    }
                    add("Timestamp: ") {
                        color = TextColor.GRAY
                        append(Instant.ofEpochMilli(ingameTrophy.timestamp).toReadableString()) {
                            color = TextColor.GREEN
                        }
                    }
                }.build()
                Displays.padding(2, Displays.item(item).withTooltip(lore))
            }.toRow().let { display(Displays.inventoryBackground(8, Orientation.HORIZONTAL, Displays.padding(2, it))) }
        },
    )
}
