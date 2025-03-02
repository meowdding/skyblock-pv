package tech.thatgravyboat.skyblockpv.api.data

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.utils.Logger
import tech.thatgravyboat.skyblockpv.utils.getNbt
import tech.thatgravyboat.skyblockpv.utils.getNbtJson
import tech.thatgravyboat.skyblockpv.utils.itemStack
import tech.thatgravyboat.skyblockpv.utils.legacyStack
import kotlin.io.encoding.ExperimentalEncodingApi

data class InventoryData(
    val inventoryItems: Inventory,
    val armorItems: Inventory,
    val equipmentItems: Inventory,
    val enderChestPages: MutableList<EnderChestPage>,
    val backpacks: MutableList<Backpack>,
    val potionBag: Inventory,
    val talismans: MutableList<TalismansPage>,
    val fishingBag: Inventory,
    val sacks: Inventory,
    val quiver: Inventory,
    val personalVault: Inventory,
    val wardrobe: Wardrobe
) {

    data class Wardrobe(
        val equippedArmor: Int,
        val armor: WardrobeArmor
    ) {
        data class WardrobeArmor(
            val armor: Inventory
        )

        companion object {
            fun fromJson(json: JsonObject): WardrobeArmor {

                return WardrobeArmor(Inventory.fromJson(json.get("armor").asJsonObject))
            }
        }
    }

    data class EnderChestPage(
        val items: Inventory
    ) {
        companion object {
            fun fromJson(json: JsonObject): MutableList<EnderChestPage> {
                Logger.info("Ender Chest: \n${json.get("data").getNbtJson()?.asString}")
                return mutableListOf()
            }
        }
    }

    data class Backpack(
        val items: Inventory,
        val icon: ItemStack
    ) {
        companion object {
            fun icons(json: JsonObject): MutableMap<Int, ItemStack> {
                var icons = mutableMapOf<Int, ItemStack>()
                json.entrySet().forEach { entry ->
                    icons[entry.key.toInt()] = entry.value.asJsonObject.get("data").itemStack()
                }
                return icons
            }

            fun fromJson(json: JsonObject): MutableMap<Int, Inventory> {
                var backpacks = mutableMapOf<Int, Inventory>()
                json.entrySet().forEach { entry ->
                    backpacks[entry.key.toInt()] = Inventory.fromJson(entry.value.asJsonObject.getAsJsonObject("data"))
                }
                return backpacks
            }
        }
    }

    data class TalismansPage(
        val talismans: Inventory
    ) {
        companion object {
            fun fromJson(json: JsonObject): MutableList<TalismansPage> {
                Logger.info("Talisman Bag: \n${json.getNbtJson()?.asString}")
                return mutableListOf()
            }
        }
    }

    class Inventory(
        val inventory: MutableList<ItemStack>
    ) {
        companion object {
            @OptIn(ExperimentalEncodingApi::class)
            fun fromJson(json: JsonObject): Inventory {
                if (!json.has("data")) return Inventory(mutableListOf())
                val itemList = json.get("data").getNbt().getList("i", 10)
                return Inventory(itemList.map { item -> item.legacyStack() }.toMutableList())
            }
        }
    }
}
