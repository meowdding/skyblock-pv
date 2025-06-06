package me.owdding.skyblockpv.api.data

import com.google.gson.JsonObject
import me.owdding.lib.extensions.rightPad
import me.owdding.lib.extensions.sortedByKeys
import me.owdding.skyblockpv.utils.Utils
import me.owdding.skyblockpv.utils.getNbt
import me.owdding.skyblockpv.utils.json.getAs
import me.owdding.skyblockpv.utils.legacyStack
import net.minecraft.nbt.Tag
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.utils.extentions.asLong
import tech.thatgravyboat.skyblockapi.utils.extentions.asMap
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.jvm.optionals.getOrNull

data class InventoryData(
    val inventoryItems: Inventory?,
    val armorItems: Inventory?,
    val equipmentItems: Inventory?,
    val enderChestPages: List<EnderChestPage>?,
    val backpacks: List<Backpack>?,
    val potionBag: Inventory?,
    val talismans: List<TalismansPage>?,
    val fishingBag: Inventory?,
    val sacks: Map<String, Long>,
    val quiver: Inventory?,
    val personalVault: Inventory?,
    val wardrobe: Wardrobe?,
    val candy: Inventory?,
    val carnivalMaskBag: Inventory?,
) {

    /** Get all items from all sources, **EXCEPT** for sacks.*/
    fun getAllItems() = buildList {
        fun addAll(list: List<ItemStack>?) = list?.let { this.addAll(it) }

        addAll(inventoryItems?.inventory)
        addAll(armorItems?.inventory)
        addAll(equipmentItems?.inventory)
        addAll(enderChestPages?.flatMap { it.items.inventory })
        addAll(backpacks?.flatMap { it.items.inventory })
        addAll(potionBag?.inventory)
        addAll(talismans?.flatMap { it.talismans.inventory })
        addAll(fishingBag?.inventory)
        addAll(quiver?.inventory)
        addAll(personalVault?.inventory)
        addAll(wardrobe?.armor?.armor?.inventory)
    }

    data class Wardrobe(
        val equippedArmor: Int,
        val armor: WardrobeArmor,
    ) {
        data class WardrobeArmor(
            val armor: Inventory,
        )

        companion object {
            fun fromJson(json: JsonObject): WardrobeArmor = WardrobeArmor(Inventory.fromJson(json))
        }
    }

    // todo: last ec page if not maxed uses full page instead of only 1 row
    data class EnderChestPage(
        val items: Inventory,
    ) {
        companion object {
            fun fromJson(json: JsonObject) = json.getInventoryData().chunked(45).map { it.completableInventory() }.map { EnderChestPage(it) }
        }
    }

    data class Backpack(
        val items: Inventory,
        val icon: ItemStack,
    ) {
        companion object {
            fun icons(json: JsonObject): Map<Int, ItemStack> = json.entrySet().associate { entry ->
                entry.key.toInt() to entry.value.asJsonObject.get("data").getNbt().getListOrEmpty("i").first().legacyStack()
            }

            fun fromJson(json: JsonObject): Map<Int, Inventory> = json.entrySet().associate { entry ->
                entry.key.toInt() to Inventory.fromJson(entry.value.asJsonObject)
            }.sortedByKeys()
        }
    }

    data class TalismansPage(
        val talismans: Inventory,
    ) {
        companion object {
            fun fromJson(json: JsonObject) = json.getInventoryData().chunked(45).map { it.completableInventory() }.map { TalismansPage(it) }
        }
    }

    data class Inventory(
        val inventory: List<ItemStack>,
    ) {
        companion object {
            fun fromJson(json: JsonObject): Inventory = json.getInventoryData().completableInventory()
        }
    }

    companion object {
        fun fromJson(inventory: JsonObject, sharedInventory: JsonObject?): InventoryData {
            val backpackIcons: Map<Int, ItemStack> = inventory.getAs<JsonObject>("backpack_icons")?.let { Backpack.icons(it) } ?: emptyMap()
            val bagContents = inventory.getAs<JsonObject>("bag_contents")
            return InventoryData(
                inventoryItems = inventory.getAs<JsonObject>("inv_contents")?.let { Inventory.fromJson(it) },
                enderChestPages = inventory.getAs<JsonObject>("ender_chest_contents")?.let { EnderChestPage.fromJson(it) },
                potionBag = bagContents?.getAs<JsonObject>("potion_bag")?.let { Inventory.fromJson(it) },
                talismans = bagContents?.getAs<JsonObject>("talisman_bag")?.let { TalismansPage.fromJson(it) },
                fishingBag = bagContents?.getAs<JsonObject>("fishing_bag")?.let { Inventory.fromJson(it) },
                sacks = inventory.getAs<JsonObject>("sacks_counts")?.asMap { key, value -> key to value.asLong(0) }?.filterValues { it > 0 } ?: emptyMap(),
                quiver = bagContents?.getAs<JsonObject>("quiver")?.let { Inventory.fromJson(it) },
                armorItems = inventory.getAs<JsonObject>("inv_armor")?.let { Inventory.fromJson(it) },
                equipmentItems = inventory.getAs<JsonObject>("equipment_contents")?.let { Inventory.fromJson(it) },
                personalVault = inventory.getAs<JsonObject>("personal_vault_contents")?.let { Inventory.fromJson(it) },
                backpacks = inventory.getAs<JsonObject>("backpack_contents")?.let {
                    Backpack.fromJson(it).map { (id, inv) ->
                        Backpack(items = inv, icon = backpackIcons[id] ?: ItemStack.EMPTY)
                    }
                },
                wardrobe = inventory.getAs<JsonObject>("wardrobe_contents")?.let {
                    Wardrobe(
                        equippedArmor = inventory.get("wardrobe_equipped_slot").asInt,
                        armor = Wardrobe.fromJson(it),
                    )
                },
                candy = sharedInventory?.getAs<JsonObject>("candy_inventory_contents")?.let { Inventory.fromJson(it) },
                carnivalMaskBag = sharedInventory?.getAs<JsonObject>("carnival_mask_inventory_contents")?.let { Inventory.fromJson(it) },
            )
        }
    }
}


private fun JsonObject.getInventoryData(): List<Tag> = this.get("data")?.getNbt()?.getList("i")?.getOrNull() ?: emptyList()

private fun List<Tag>.createTempInventory(): MutableList<ItemStack> {
    return CopyOnWriteArrayList<ItemStack>().apply {
        this.rightPad(this@createTempInventory.size, ItemStack.EMPTY)
    }
}

private fun List<Tag>.completableInventory(): InventoryData.Inventory {
    val list = this.createTempInventory()

    Utils.runAsync {
        val stacks = this.map { tag -> tag.legacyStack() }
        list.clear()
        list.addAll(stacks)
    }

    return InventoryData.Inventory(list)
}
