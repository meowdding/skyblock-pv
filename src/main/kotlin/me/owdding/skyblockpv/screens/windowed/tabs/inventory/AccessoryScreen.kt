package me.owdding.skyblockpv.screens.windowed.tabs.inventory

import com.mojang.authlib.GameProfile
import me.owdding.lib.builder.DisplayFactory
import me.owdding.lib.displays.Alignment
import me.owdding.lib.displays.Display
import me.owdding.lib.displays.Displays
import me.owdding.lib.displays.withTooltip
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.data.repo.SkullTextures
import me.owdding.skyblockpv.utils.Utils.append
import me.owdding.skyblockpv.utils.components.PvWidgets
import me.owdding.skyblockpv.utils.displays.withTranslatedTooltip
import me.owdding.skyblockpv.utils.theme.PvColors
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.extentions.toTitleCase
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.shadowColor

class AccessoryScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BasePagedInventoryScreen<List<List<ItemStack>>>(gameProfile, profile) {

    override fun getRawInventory() = profile.inventory?.talismans
    override fun List<List<ItemStack>>.getInventories(): List<Display> = map { PvWidgets.createInventory(it) }

    override fun List<List<ItemStack>>.getIcons(): List<ItemStack> = List(size) { SkullTextures.ACCESSORY_BAG.skull.copy() }

    override fun getExtraLine() = DisplayFactory.vertical(alignment = Alignment.CENTER) {
        val maxwell = profile.maxwell ?: return@vertical

        val display = profile.magicalPower.thenApply { (magicalPower, breakdown) ->
            Displays.text(
                Text.of("Magical Power: $magicalPower") {
                    color = PvColors.DARK_GRAY
                    shadowColor = null
                },
            ).withTooltip(breakdown)
        }
        val loadingDisplay = Displays.text(
            Text.of("Magical Power: ") {
                color = PvColors.DARK_GRAY
                shadowColor = null
                append("Loading...") {
                    this.color = TextColor.RED
                }
            },
        )
        display(Displays.supplied { display.getNow(loadingDisplay) })

        display(
            Displays.component(
                Text.of("Highest Magical Power: ${maxwell.highestMp.toFormattedString()}") {
                    color = PvColors.DARK_GRAY
                    shadowColor = null
                }
            ).withTranslatedTooltip("skyblockpv.screens.inventory.accessory.highest_magical_power"),
        )

        string("Selected Power: ${maxwell.selectedPower.toTitleCase()}") {
            color = PvColors.DARK_GRAY
            shadowColor = null
        }
    }
}
