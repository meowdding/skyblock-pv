package me.owdding.skyblockpv.screens.tabs.inventory

import com.mojang.authlib.GameProfile
import me.owdding.lib.builder.DisplayFactory
import me.owdding.lib.displays.Alignment
import me.owdding.lib.displays.Display
import me.owdding.lib.displays.Displays
import me.owdding.lib.displays.withTooltip
import me.owdding.skyblockpv.api.data.SkyBlockProfile
import me.owdding.skyblockpv.data.repo.SkullTextures
import me.owdding.skyblockpv.utils.components.PvWidgets
import me.owdding.skyblockpv.utils.theme.PvColors
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.extentions.toTitleCase
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.shadowColor

class AccessoryScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BasePagedInventoryScreen(gameProfile, profile) {
    private val accessories get() = profile.inventory?.talismans ?: emptyList()

    override fun getInventories(): List<Display> = accessories.map { PvWidgets.createInventory(it.talismans.inventory) }

    override fun getIcons(): List<ItemStack> = List(accessories.size) { SkullTextures.ACCESSORY_BAG.skull.copy() }

    override fun getExtraLine() = DisplayFactory.vertical(alignment = Alignment.CENTER) {
        val maxwell = profile.maxwell ?: return@vertical

        val display = Displays.text(
            Text.of("Magical Power: ${profile.magicalPower.first.toFormattedString()}") {
                color = PvColors.DARK_GRAY
                shadowColor = null
            },
        ).withTooltip(profile.magicalPower.second)
        display(display)

        string("Highest Magical Power: ${maxwell.highestMp.toFormattedString()}") {
            color = PvColors.DARK_GRAY
            shadowColor = null
        }
        string("Selected Power: ${maxwell.selectedPower.toTitleCase()}") {
            color = PvColors.DARK_GRAY
            shadowColor = null
        }
    }
}
