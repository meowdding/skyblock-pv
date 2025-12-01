@file:Suppress("JavaDefaultMethodsNotOverriddenByDelegation")

package me.owdding.skyblockpv.api.data

import com.google.gson.JsonObject
import me.owdding.lib.extensions.rightPad
import me.owdding.lib.extensions.sortedByKeys
import me.owdding.skyblockpv.utils.getNbt
import me.owdding.skyblockpv.utils.json.getAs
import me.owdding.skyblockpv.utils.legacyStack
import net.minecraft.nbt.NbtAccounter
import net.minecraft.nbt.NbtIo
import net.minecraft.nbt.Tag
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.utils.extentions.asInt
import tech.thatgravyboat.skyblockapi.utils.extentions.asLong
import tech.thatgravyboat.skyblockapi.utils.extentions.asMap
import java.io.ByteArrayInputStream
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.io.encoding.Base64
import kotlin.jvm.optionals.getOrNull

typealias Inventory = List<ItemStack>

data class InventoryData(
    val inventoryItems: Inventory?,
    val armorItems: Inventory?,
    val equipmentItems: Inventory?,
    val enderChestPages: List<Inventory>?,
    val backpacks: List<Backpack>?,
    val potionBag: Inventory?,
    val talismans: List<Inventory>?,
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

        addAll(inventoryItems)
        addAll(armorItems)
        addAll(equipmentItems)
        addAll(enderChestPages?.flatten())
        addAll(backpacks?.flatten())
        addAll(potionBag)
        addAll(talismans?.flatten())
        addAll(fishingBag)
        addAll(quiver)
        addAll(personalVault)
        addAll(wardrobe)
    }

    data class Wardrobe(
        val equippedArmor: Int,
        val armor: Inventory,
    ) : List<ItemStack> by armor

    data class Backpack(
        val items: Inventory,
        val icon: ItemStack,
    ) : List<ItemStack> by items {
        companion object {
            fun icons(json: JsonObject): Map<Int, ItemStack> = json.entrySet().associate { entry ->
                entry.key.toInt() to entry.value.asJsonObject.get("data").getNbt().getListOrEmpty("i").first().legacyStack()
            }

            fun fromJson(json: JsonObject): Map<Int, CompletableFuture<Inventory>> = json.entrySet().associate { entry ->
                entry.key.toInt() to entry.value.asJsonObject.completableInventory()
            }.sortedByKeys()
        }
    }

    companion object {
        fun fromJson(inventory: JsonObject, sharedInventory: JsonObject?): CompletableFuture<InventoryData?> {
            val backpackIcons: Map<Int, ItemStack> = inventory.getAs<JsonObject>("backpack_icons")?.let { Backpack.icons(it) } ?: emptyMap()
            val bagContents = inventory.getAs<JsonObject>("bag_contents")
            val inventoryItems = inventory.getAs<JsonObject>("inv_contents")?.completableInventory()
            val enderChestPages = inventory.getAs<JsonObject>("ender_chest_contents")?.completableInventory()?.thenApply { it.chunked(45) }
            val potionBag = bagContents?.getAs<JsonObject>("potion_bag")?.completableInventory()
            val talismanBag = bagContents?.getAs<JsonObject>("talisman_bag")?.completableInventory()?.thenApply { it.chunked(45) }
            val fishingBag = bagContents?.getAs<JsonObject>("fishing_bag")?.completableInventory()
            val quiver = bagContents?.getAs<JsonObject>("quiver")?.completableInventory()
            val invArmor = inventory.getAs<JsonObject>("inv_armor")?.completableInventory()
            val equipment = inventory.getAs<JsonObject>("equipment_contents")?.completableInventory()
            val vault = inventory.getAs<JsonObject>("personal_vault_contents")?.completableInventory()
            val candy = sharedInventory?.getAs<JsonObject>("candy_inventory_contents")?.completableInventory()
            val carnivalMaskBag = sharedInventory?.getAs<JsonObject>("carnival_mask_inventory_contents")?.completableInventory()

            val wardrobe = inventory.getAs<JsonObject>("wardrobe_contents")?.completableInventory()?.thenApply {
                Wardrobe(
                    equippedArmor = inventory.get("wardrobe_equipped_slot").asInt,
                    armor = it,
                )
            }

            val backpacks = inventory.getAs<JsonObject>("backpack_contents")?.let {
                Backpack.fromJson(it).map { (id, inv) ->
                    inv.thenApply {
                        Backpack(items = it, icon = backpackIcons[id] ?: ItemStack.EMPTY)
                    }
                }
            }

            val backpackFuture = CompletableFuture.allOf(*(backpacks ?: emptyList()).toTypedArray()).thenApply {
                backpacks?.map { it.get() }
            }

            return CompletableFuture.allOf(
                *listOfNotNull(
                    inventoryItems,
                    enderChestPages,
                    potionBag,
                    talismanBag,
                    fishingBag,
                    quiver,
                    invArmor,
                    equipment,
                    vault,
                    candy,
                    carnivalMaskBag,
                    wardrobe,
                    backpackFuture,
                ).toTypedArray(),
            ).thenApply {
                InventoryData(
                    inventoryItems = inventoryItems?.get(),
                    enderChestPages = enderChestPages?.get(),
                    potionBag = potionBag?.get(),
                    talismans = talismanBag?.get(),
                    fishingBag = fishingBag?.get(),
                    quiver = quiver?.get(),
                    armorItems = invArmor?.get(),
                    equipmentItems = equipment?.get(),
                    personalVault = vault?.get(),
                    backpacks = backpackFuture.get(),
                    wardrobe = wardrobe?.get(),
                    candy = candy?.get(),
                    carnivalMaskBag = carnivalMaskBag?.get(),
                    sacks = inventory.getAs<JsonObject>("sacks_counts")?.asMap { key, value -> key to value.asLong(0) }?.filterValues { it > 0 } ?: emptyMap(),
                )
            }
        }
    }
}


private fun JsonObject.getInventoryData(): List<Tag> = this.get("data")?.getNbt()?.getList("i")?.getOrNull() ?: emptyList()

private fun List<Any>.createTempInventory(): MutableList<ItemStack> {
    return CopyOnWriteArrayList<ItemStack>().apply {
        this.rightPad(this@createTempInventory.size, ItemStack.EMPTY)
    }
}

private fun JsonObject.completableInventory(): CompletableFuture<Inventory> {
    val inv = this.parseInvData()
    return CompletableFuture.allOf(*inv.toTypedArray()).thenApply { inv.map { it.get() } }
}

internal fun JsonObject.parseInvData(): List<CompletableFuture<ItemStack>> = runCatching {
    when (this.get("type").asInt(-1)) {
        0 -> parseV0InventoryData(this)
        else -> emptyList()
    }
}.getOrElse { emptyList() }

private fun parseV0InventoryData(json: JsonObject): List<CompletableFuture<ItemStack>> {
    val data = json.get("data").asString
    val tag = NbtIo.readCompressed(ByteArrayInputStream(Base64.decode(data)), NbtAccounter.unlimitedHeap())
    return tag.getList("i").getOrNull()?.mapNotNull {
        runCatching { CompletableFuture.supplyAsync { it.legacyStack() } }.getOrDefault(CompletableFuture.completedFuture(ItemStack.EMPTY))
    } ?: emptyList()
}
