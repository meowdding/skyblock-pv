package me.owdding.skyblockpv.screens.windowed.tabs.inventory

import com.mojang.authlib.GameProfile
import earth.terrarium.olympus.client.components.Widgets
import earth.terrarium.olympus.client.constants.MinecraftColors
import me.owdding.lib.displays.DisplayWidget
import me.owdding.lib.displays.Displays
import me.owdding.lib.displays.asTable
import me.owdding.lib.extensions.withTooltip
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.screens.windowed.tabs.base.FilterScreen
import me.owdding.skyblockpv.utils.components.PvLayouts
import me.owdding.skyblockpv.utils.displays.ExtraDisplays
import me.owdding.skyblockpv.utils.theme.PvColors
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.item.calculator.getItemValue
import tech.thatgravyboat.skyblockapi.utils.extentions.cleanName
import tech.thatgravyboat.skyblockapi.utils.extentions.getLore
import tech.thatgravyboat.skyblockapi.utils.extentions.getRawLore
import tech.thatgravyboat.skyblockapi.utils.extentions.getSkyBlockId
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import kotlin.math.max
import kotlin.math.min

class InventorySearchScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseInventoryScreen(gameProfile, profile),
    FilterScreen<InventorySearchFilter> {
    override fun getLayout(bg: DisplayWidget) = createLayout(bg)

    override var query: String? = null
    override var filter = InventorySearchFilter.ALL
    override fun filterEntries() = InventorySearchFilter.entries
    override fun InventorySearchFilter.display() = display

    fun entries(): List<ItemStack> {
        val inventory = profile.inventory?.inventoryItems ?: emptyList()
        val enderChests = profile.inventory?.enderChestPages ?: emptyList()
        val backpacks = profile.inventory?.backpacks ?: emptyList()
        val accessories = profile.inventory?.talismans ?: emptyList()
        val equipment = profile.inventory?.equipmentItems ?: emptyList()
        val armor = profile.inventory?.armorItems ?: emptyList()
        val personalVault = profile.inventory?.personalVault ?: emptyList()
        val maskBag = profile.inventory?.carnivalMaskBag ?: emptyList()
        val wardrobeSlots = profile.inventory?.loadouts?.armorSets ?: emptyMap()
        val equipmentSlots = profile.inventory?.loadouts?.equipmentSets ?: emptyMap()

        val finalItems = mutableListOf<ItemStack>()

        fun addWithSource(stack: ItemStack, source: Component) {
            if (stack.isEmpty) return
            finalItems.add(
                stack.copy().apply {
                    withTooltip {
                        add(hoverName)
                        val lore = getLore()
                        lore.forEach(::add)
                        add("From ") {
                            color = PvColors.GRAY
                            append(source)
                        }
                    }
                },
            )
        }

        for (stack in inventory) {
            addWithSource(stack, Text.of("Inventory"))
        }
        for (stack in equipment) {
            addWithSource(stack, Text.of("Equipment"))
        }
        for (stack in armor) {
            addWithSource(stack, Text.of("Armor"))
        }
        for (stack in personalVault) {
            addWithSource(stack, Text.of("Personal Vault"))
        }
        for (stack in maskBag) {
            addWithSource(stack, Text.of("Mask Bag"))
        }
        for ((index, enderChest) in enderChests.withIndex()) {
            for (stack in enderChest) {
                addWithSource(stack, Text.of("Ender Chest ${index + 1}"))
            }
        }
        for ((index, backpack) in backpacks.withIndex()) {
            for (stack in backpack) {
                addWithSource(stack, Text.of("Backpack ${index + 1}"))
            }
        }
        for ((index, accessoryPage) in accessories.withIndex()) {
            for (stack in accessoryPage) {
                addWithSource(stack, Text.of("Accessory Bag Page ${index + 1}"))
            }
        }
        for ((index, armorSet) in wardrobeSlots) {
            addWithSource(armorSet.helmet, Text.of("Armor Set ${index + 1}"))
            addWithSource(armorSet.chestplate, Text.of("Armor Set ${index + 1}"))
            addWithSource(armorSet.leggings, Text.of("Armor Set ${index + 1}"))
            addWithSource(armorSet.boots, Text.of("Armor Set ${index + 1}"))
        }
        for ((index, equipmentSet) in equipmentSlots) {
            addWithSource(equipmentSet.slot1, Text.of("Equipment Set ${index + 1}"))
            addWithSource(equipmentSet.slot2, Text.of("Equipment Set ${index + 1}"))
            addWithSource(equipmentSet.slot3, Text.of("Equipment Set ${index + 1}"))
            addWithSource(equipmentSet.slot4, Text.of("Equipment Set ${index + 1}"))
        }

        return finalItems.sortedByDescending { it.getItemValue().price }
    }

    private fun ItemStack.searchKey() = buildString {
        append(cleanName)
        append(" ")
        getRawLore().forEach {
            append(it)
            append(" ")
        }
        append(getSkyBlockId())
    }

    override fun createLayout(width: Int, height: Int) = PvLayouts.frame {
        val query = query
        var items = entries()

        if (query != null) {
            items = items.map { it to it.searchKey() }
                .filter { it.second.contains(query, ignoreCase = true) }
                .sortedBy { it.second.indexOf(query, ignoreCase = true) }
                .map { it.first }
        }

        if (items.isEmpty()) {
            widget(Widgets.text("No items match the query!").withColor(MinecraftColors.RED)) {
                alignHorizontallyCenter()
                alignVerticallyMiddle()
            }
            return@frame
        }

        val inventorySlots = ((width - 20) / 20).coerceAtMost(min(20, max(items.size / 3, 3)))

        val itemDisplays = items.map { item ->
            Displays.padding(2, Displays.item(item, showTooltip = true, showStackSize = true))
        }.chunked(inventorySlots)

        display(
            ExtraDisplays.inventoryBackground(
                itemDisplays.firstOrNull()?.size ?: 0, itemDisplays.size,
                Displays.padding(2, itemDisplays.asTable()),
            ),
        )
    }
}

enum class InventorySearchFilter(val display: String) {
    ALL("All")
}
