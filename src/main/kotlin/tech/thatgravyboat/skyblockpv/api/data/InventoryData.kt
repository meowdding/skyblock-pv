package tech.thatgravyboat.skyblockpv.api.data

import com.google.gson.JsonObject
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockpv.utils.getNbt
import tech.thatgravyboat.skyblockpv.utils.legacyStack

data class InventoryData(
    val inventoryItems: Inventory?,
    val armorItems: Inventory?,
    val equipmentItems: Inventory?,
    val enderChestPages: List<EnderChestPage>?,
    val backpacks: List<Backpack>?,
    val potionBag: Inventory?,
    val talismans: List<TalismansPage>?,
    val fishingBag: Inventory?,
    val sacks: Inventory?,
    val quiver: Inventory?,
    val personalVault: Inventory?,
    val wardrobe: Wardrobe?,
) {

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
        addAll(sacks?.inventory)
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
            fun fromJson(json: JsonObject): WardrobeArmor {
                return WardrobeArmor(Inventory.fromJson(json))
            }
        }
    }

    // todo: last ec page if not maxed uses full page instead of only 1 row
    data class EnderChestPage(
        val items: Inventory,
    ) {
        companion object {
            fun fromJson(json: JsonObject): List<EnderChestPage> {
                return json.get("data").getNbt().let {
                    it.getList("i", 10).map { item -> item.legacyStack() }.chunked(45).map { EnderChestPage(Inventory(it)) }
                }
            }
        }
    }

    data class Backpack(
        val items: Inventory,
        val icon: ItemStack,
    ) {
        companion object {
            fun icons(json: JsonObject): Map<Int, ItemStack> {
                return json.entrySet().associate { entry ->
                    entry.key.toInt() to entry.value.asJsonObject.get("data").getNbt().getList("i", 10).first().legacyStack()
                }
            }

            fun fromJson(json: JsonObject): Map<Int, Inventory> {
                return json.entrySet().associate { entry ->
                    entry.key.toInt() to Inventory.fromJson(entry.value.asJsonObject)
                }.toList().sortedBy { it.first }.toMap()
            }
        }
    }

    data class TalismansPage(
        val talismans: Inventory,
    ) {
        companion object {
            fun fromJson(json: JsonObject): List<TalismansPage> {
                return json.get("data").getNbt().getList("i", 10)?.let {
                    it.map { it.legacyStack() }.chunked(45).map { TalismansPage(Inventory(it)) }
                } ?: listOf()
            }
        }
    }

    data class Inventory(
        val inventory: List<ItemStack>,
    ) {
        companion object {
            fun fromJson(json: JsonObject): Inventory {
                if (!json.has("data")) return Inventory(listOf())
                val itemList = json.get("data").getNbt().getList("i", 10)
                return Inventory(itemList.map { item -> item.legacyStack() })
            }
        }
    }
}
