package me.owdding.skyblockpv.screens.fullscreen

import me.owdding.skyblockpv.screens.fullscreen.tabs.main.MainTab
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.utils.text.Text

enum class FullScreenTabs(val tab: FullScreenTab, val displayName: Component, val item: ItemStack) {
    MAIN(MainTab, Text.of("Main"), Items.GRASS_BLOCK.defaultInstance),
}
