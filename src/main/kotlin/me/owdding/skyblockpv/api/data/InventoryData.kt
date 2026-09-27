@file:Suppress("JavaDefaultMethodsNotOverriddenByDelegation")

package me.owdding.skyblockpv.api.data

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import me.owdding.lib.extensions.rightPad
import me.owdding.lib.extensions.sortedByKeys
import me.owdding.lib.utils.MeowddingLogger
import me.owdding.lib.utils.MeowddingLogger.Companion.featureLogger
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.api.data.profile.BackingSkyBlockProfile
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
import tech.thatgravyboat.skyblockapi.utils.extentions.asUUID
import java.io.ByteArrayInputStream
import java.util.UUID
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
    val candy: Inventory?,
    val carnivalMaskBag: Inventory?,
    val loadouts: LoadoutData?
) {

    data class LoadoutData(
        val equippedArmorSet: Int,
        val armorSets: Map<Int, ArmorSet>,
        val equippedEquipmentSet: Int,
        val equipmentSets: Map<Int, EquipmentSet>,
        val savedLoadouts: Map<Int, SavedLoadout>
    )

    interface ItemSet {
        fun getStacks(): List<ItemStack>
        val id: Int
    }

    data class ArmorSet(
        override val id: Int,
        val helmet: ItemStack,
        val chestplate: ItemStack,
        val leggings: ItemStack,
        val boots: ItemStack
    ) : ItemSet {
        override fun getStacks() = listOf(helmet, chestplate, leggings, boots)
    }

    data class EquipmentSet(
        override val id: Int,
        val slot1: ItemStack,
        val slot2: ItemStack,
        val slot3: ItemStack,
        val slot4: ItemStack
    ) : ItemSet {
        override fun getStacks() = listOf(slot1, slot2, slot3, slot4)
    }

    data class SavedLoadout(
        val id: Int,
        val name: String,
        val armorSetId: Int?,
        val equipmentSlotId: Int?,
        val miningCoreSelectedSlot: Int?,
        val foragingCoreSelectedSlot: Int?,
        val powerStone: String?,
        val tuningPointsSlot: Int?,
        val pet: UUID?
    ) {
        val isEmpty get() = armorSetId == null && equipmentSlotId == null && miningCoreSelectedSlot == null && foragingCoreSelectedSlot == null && powerStone == null && tuningPointsSlot == null && pet == null && name == "Loadout $id"
    }

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
        addAll(loadouts?.equipmentSets?.flatMap { it.value.getStacks() })
        addAll(loadouts?.armorSets?.flatMap { it.value.getStacks() })
    }

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

    companion object : MeowddingLogger by SkyBlockPv.featureLogger() {
        context(_: ProfileId)
        fun fromJson(member: JsonObject, inventory: JsonObject, sharedInventory: JsonObject?): CompletableFuture<InventoryData?> {
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

            val loadouts = parseLoadoutData(member.asJsonObject)

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
                    backpackFuture,
                    loadouts,
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
                    candy = candy?.get(),
                    carnivalMaskBag = carnivalMaskBag?.get(),
                    loadouts = loadouts.get(),
                    sacks = inventory.getAs<JsonObject>("sacks_counts")?.asMap { key, value -> key to value.asLong(0) }?.filterValues { it > 0 } ?: emptyMap(),
                )
            }
        }

        context(profileId: ProfileId)
        fun parseLoadoutData(json: JsonObject?): CompletableFuture<LoadoutData?> {
            if (json == null || !json.has("loadout")) return CompletableFuture.completedFuture(null)
            val loadoutObj = json.getAsJsonObject("loadout")

            val armorObj = loadoutObj.getAsJsonObject("armor")
            val equippedArmor = armorObj?.get("equipped_set")?.asInt ?: 0
            val armorSetFutures = mutableMapOf<Int, CompletableFuture<ArmorSet>>()

            armorObj?.entrySet()?.filter { it.key != "equipped_set" }?.forEach { (key, element) ->
                val setObj = element.asJsonObject
                val id = setObj.get("id")?.asInt ?: 0
                val helmetFut = parseItemStackSlot(setObj.getAsJsonObject("HELMET"))
                val chestplateFut = parseItemStackSlot(setObj.getAsJsonObject("CHESTPLATE"))
                val leggingsFut = parseItemStackSlot(setObj.getAsJsonObject("LEGGINGS"))
                val bootsFut = parseItemStackSlot(setObj.getAsJsonObject("BOOTS"))

                armorSetFutures[key.toInt()] = CompletableFuture.allOf(helmetFut, chestplateFut, leggingsFut, bootsFut).thenApply {
                    ArmorSet(id, helmetFut.join(), chestplateFut.join(), leggingsFut.join(), bootsFut.join())
                }
            }

            val equipmentObj = loadoutObj.getAsJsonObject("equipment")
            val equippedEquipment = equipmentObj?.get("equipped_set")?.asInt ?: 0
            val equipmentSetFutures = mutableMapOf<Int, CompletableFuture<EquipmentSet>>()

            equipmentObj?.entrySet()?.filter { it.key != "equipped_set" }?.forEach { (key, element) ->
                val setObj = element.asJsonObject
                val id = setObj.get("id")?.asInt ?: 0
                val slot1Fut = parseItemStackSlot(setObj.getAsJsonObject("EQUIPMENT_SLOT_1"))
                val slot2Fut = parseItemStackSlot(setObj.getAsJsonObject("EQUIPMENT_SLOT_2"))
                val slot3Fut = parseItemStackSlot(setObj.getAsJsonObject("EQUIPMENT_SLOT_3"))
                val slot4Fut = parseItemStackSlot(setObj.getAsJsonObject("EQUIPMENT_SLOT_4"))

                equipmentSetFutures[key.toInt()] = CompletableFuture.allOf(slot1Fut, slot2Fut, slot3Fut, slot4Fut).thenApply {
                    EquipmentSet(id, slot1Fut.join(), slot2Fut.join(), slot3Fut.join(), slot4Fut.join())
                }
            }

            val savedObj = loadoutObj.getAsJsonObject("loadouts")
            val savedLoadouts = mutableMapOf<Int, CompletableFuture<SavedLoadout>>()

            savedObj?.entrySet()?.forEach { (key, element) ->
                val setObj = element.asJsonObject
                    savedLoadouts[key.toInt()] = BackingSkyBlockProfile.future {SavedLoadout(
                        id = setObj.get("id")?.asInt!!,
                        name = setObj.get("name")?.asString!!,
                        armorSetId = setObj.getAs<JsonElement>("armor_set_id")?.asInt,
                        equipmentSlotId = setObj.getAs<JsonElement>("equipment_set_id")?.asInt,
                        miningCoreSelectedSlot = setObj.getAs<JsonElement>("mining_core_selected_slot")?.asInt,
                        foragingCoreSelectedSlot = setObj.getAs<JsonElement>("foraging_core_selected_slot")?.asInt,
                        powerStone = setObj.getAs<JsonElement>("power_stone")?.asString,
                        tuningPointsSlot = setObj.getAs<JsonElement>("tuning_points_slot")?.asInt,
                        pet = setObj.getAs<JsonElement>("pet")?.asUUID(),
                    )}
            }

            val allFutures = armorSetFutures.values + equipmentSetFutures.values + savedLoadouts.values
            return CompletableFuture.allOf(*allFutures.toTypedArray()).thenApply {
                LoadoutData(
                    equippedArmorSet = equippedArmor,
                    armorSets = armorSetFutures.mapValues { it.value.get() },
                    equippedEquipmentSet = equippedEquipment,
                    equipmentSets = equipmentSetFutures.mapValues { it.value.get() },
                    savedLoadouts = savedLoadouts.mapValues { (_, value) -> value.get() }
                )
            }
        }

        private fun parseItemStackSlot(obj: JsonObject?): CompletableFuture<ItemStack> {
            if (obj == null || !obj.has("data")) return CompletableFuture.completedFuture(ItemStack.EMPTY)

            val dataStr = obj.get("data").asString
            if (dataStr.isBlank()) return CompletableFuture.completedFuture(ItemStack.EMPTY)

            return runCatching {
                val parsedList = obj.parseInvData()
                parsedList.firstOrNull() ?: CompletableFuture.completedFuture(ItemStack.EMPTY)
            }.getOrDefault(CompletableFuture.completedFuture(ItemStack.EMPTY))
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
        runCatching { BackingSkyBlockProfile.future { it.legacyStack() } }.getOrDefault(CompletableFuture.completedFuture(ItemStack.EMPTY))
    } ?: emptyList()
}
