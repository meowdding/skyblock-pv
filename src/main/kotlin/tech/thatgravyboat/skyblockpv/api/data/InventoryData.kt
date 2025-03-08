package tech.thatgravyboat.skyblockpv.api.data

import com.google.gson.JsonObject
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockpv.utils.getNbt
import tech.thatgravyboat.skyblockpv.utils.getNbtJson
import tech.thatgravyboat.skyblockpv.utils.itemStack
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

    data class Wardrobe(
        val equippedArmor: Int,
        val armor: WardrobeArmor,
    ) {
        data class WardrobeArmor(
            val armor: Inventory,
        )

        companion object {
            fun fromJson(json: JsonObject): WardrobeArmor {

                return WardrobeArmor(Inventory.fromJson(json.get("armor").asJsonObject))
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
                    entry.key.toInt() to entry.value.asJsonObject.get("data").itemStack()
                }
            }

            fun fromJson(json: JsonObject): Map<Int, Inventory> {
                return json.entrySet().associate { entry ->
                    entry.key.toInt() to Inventory.fromJson(entry.value.asJsonObject)
                }
            }
        }
    }

    data class TalismansPage(
        val talismans: Inventory,
    ) {
        companion object {
            fun fromJson(json: JsonObject): List<TalismansPage> {
                return json.get("data").getNbtJson()?.let {
                    Inventory.fromJson(it).inventory.chunked(45).map { TalismansPage(Inventory(it)) }
                } ?: listOf()
            }
        }
    }

    class Inventory(
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
