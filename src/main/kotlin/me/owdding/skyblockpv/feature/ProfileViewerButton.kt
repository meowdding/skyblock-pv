package me.owdding.skyblockpv.feature

import me.owdding.ktmodules.Module
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.config.Config
import me.owdding.skyblockpv.utils.Utils
import me.owdding.skyblockpv.utils.theme.PvColors
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.screen.InventoryChangeEvent
import tech.thatgravyboat.skyblockapi.api.item.replaceVisually
import tech.thatgravyboat.skyblockapi.impl.tagkey.ItemTag
import tech.thatgravyboat.skyblockapi.utils.regex.RegexUtils.match
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

@Module
object ProfileViewerButton {

    private val titleRegex = "(?<name>.*)'s Profile(?: \\[GUEST])?".toRegex()

    @Subscription
    fun onInventoryChange(event: InventoryChangeEvent) {
        if (!Config.profileSpying) return
        if (event.slot.index != 13) return

        titleRegex.match(event.title, "name") { (name) ->
            if (event.item !in ItemTag.GLASS_PANES) {
                SkyBlockPv.warn("Failed to replace profile viewer button, item is not a glass pane")
                return@match
            }

            event.item.replaceVisually {
                item = Items.SPYGLASS
                name(
                    Text.of(event.title) {
                        color = PvColors.GREEN
                    },
                )
                tooltip {
                    add("(By SkyBlockPv)") {
                        color = PvColors.DARK_GRAY
                    }
                    add("Click to spy on $name's profile") {
                        color = PvColors.GRAY
                    }
                }
                onClick {
                    event.screen.onClose()
                    Utils.openPv(name)
                }
            }
        }
    }
}
