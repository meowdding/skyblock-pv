package tech.thatgravyboat.skyblockpv.api.data

import com.google.gson.JsonObject
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.utils.extentions.asLong
import tech.thatgravyboat.skyblockapi.utils.extentions.asMap
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
    val sacks: Map<String, Long>,
    val quiver: Inventory?,
    val personalVault: Inventory?,
    val wardrobe: Wardrobe?,
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

    companion object {
        fun fromJson(json: JsonObject): InventoryData {
            val backpackIcons: Map<Int, ItemStack> = json.getAsJsonObject("backpack_icons")?.let { Backpack.icons(it) } ?: emptyMap()
            val bagContents = json.getAsJsonObject("bag_contents")
            return InventoryData(
                inventoryItems = json.getAsJsonObject("inv_contents")?.let { Inventory.fromJson(it) },
                enderChestPages = json.getAsJsonObject("ender_chest_contents")?.let { EnderChestPage.fromJson(it) },
                potionBag = bagContents?.getAsJsonObject("potion_bag")?.let { Inventory.fromJson(it) },
                talismans = bagContents?.getAsJsonObject("talisman_bag")?.let { TalismansPage.fromJson(it) },
                fishingBag = bagContents?.getAsJsonObject("fishing_bag")?.let { Inventory.fromJson(it) },
                sacks = json.getAsJsonObject("sacks_counts")?.asMap { key, value -> key to value.asLong(0) }?.filterValues { it > 0 } ?: emptyMap(),
                quiver = bagContents?.getAsJsonObject("quiver")?.let { Inventory.fromJson(it) },
                armorItems = json.getAsJsonObject("inv_armor")?.let { Inventory.fromJson(it) },
                equipmentItems = json.getAsJsonObject("equipment_contents")?.let { Inventory.fromJson(it) },
                personalVault = json.getAsJsonObject("personal_vault_contents")?.let { Inventory.fromJson(it) },
                backpacks = json.getAsJsonObject("backpack_contents")?.let {
                    Backpack.fromJson(it).map { (id, inv) ->
                        Backpack(items = inv, icon = backpackIcons[id] ?: ItemStack.EMPTY)
                    }
                },
                wardrobe = json.getAsJsonObject("wardrobe_contents")?.let {
                    Wardrobe(
                        equippedArmor = json.get("wardrobe_equipped_slot").asInt,
                        armor = Wardrobe.fromJson(it),
                    )
                },
            )
        }
    }
}
