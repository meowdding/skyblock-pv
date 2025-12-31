package me.owdding.skyblockpv.screens.fullscreen

import me.owdding.skyblockpv.screens.fullscreen.tabs.main.MainTab
import me.owdding.skyblockpv.screens.fullscreen.tabs.main.MainTab1
import me.owdding.skyblockpv.screens.fullscreen.tabs.main.MainTab2
import me.owdding.skyblockpv.screens.fullscreen.tabs.main.MainTab3
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.utils.text.Text

enum class FullScreenTabs(val tab: FullScreenTab, val displayName: Component, val item: ItemStack) {
    MAIN(MainTab, Text.of("Main"), Items.GRASS_BLOCK.defaultInstance),
    MAIN1(MainTab1, Text.of("Main"), Items.WOODEN_AXE.defaultInstance),
    MAIN2(MainTab2, Text.of("Main"), Items.WOODEN_SHOVEL.defaultInstance),
    MAIN3(MainTab3, Text.of("Main"), Items.WOODEN_SPEAR.defaultInstance),
}
