package tech.thatgravyboat.skyblockpv.data.api

import com.google.gson.JsonObject
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.utils.extentions.asInt
import tech.thatgravyboat.skyblockapi.utils.extentions.asList
import tech.thatgravyboat.skyblockapi.utils.json.getPath
import tech.thatgravyboat.skyblockpv.data.api.skills.Pet
import tech.thatgravyboat.skyblockpv.utils.getNbt
import tech.thatgravyboat.skyblockpv.utils.legacyStack
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds


data class RiftData(
    // MEMBER
    val secondsSitting: Duration,
    val unlockedEyes: List<String>,
    val deadCat: DeadCat,
    val foundSouls: List<String>,
    val trophies: List<Trophy>,
    val inventory: RiftInventory?,
    val grubberStacks: Int,
    // PLAYER-STATS
    val visits: Int,
    val lifetimeMotes: Int,
) {
    companion object {
        fun fromJson(member: JsonObject, playerStats: JsonObject): RiftData {
            return RiftData(
                secondsSitting = member.getPath("village_plaza.lonely.seconds_sitting").asInt(0).seconds,
                unlockedEyes = member.getPath("wither_cage.killed_eyes").asList { it.asString },
                deadCat = member.getPath("dead_cats")?.let { DeadCat.fromJson(it.asJsonObject) } ?: DeadCat(null, emptyList()),
                foundSouls = member.getPath("enigma.found_souls").asList { it.asString },
                trophies = member.getPath("gallery.secured_trophies").asList { Trophy.fromJson(it.asJsonObject) },
                inventory = member.getPath("inventory")?.let { RiftInventory.fromJson(it.asJsonObject) },
                grubberStacks = member.getPath("castle.grubber_stacks").asInt(0),
                visits = playerStats.getPath("visits").asInt(0),
                lifetimeMotes = playerStats.getPath("lifetime_motes_earned").asInt(0),
            )
        }
    }
}

data class RiftInventory(
    val inventory: List<ItemStack>,
    val armor: List<ItemStack>,
    val enderChest: List<List<ItemStack>>,
    val equipment: List<ItemStack>,
) {
    companion object {
        fun fromJson(json: JsonObject): RiftInventory {
            return RiftInventory(
                inventory = json.getAsJsonObject("inv_contents").getInventory(),
                armor = json.getAsJsonObject("inv_armor").getInventory(),
                enderChest = json.getAsJsonObject("ender_chest_contents").getInventory().chunked(45),
                equipment = json.getAsJsonObject("equipment_contents").getInventory(),
            )
        }

        private fun JsonObject.getInventory() = this.get("data").getNbt().getListOrEmpty("i").map { it.legacyStack() }
    }
}

data class DeadCat(
    val pet: Pet?,
    val foundCats: List<String>,
) {
    companion object {
        fun fromJson(json: JsonObject): DeadCat {
            return DeadCat(
                pet = json["montezuma"]?.let { Pet.fromJson(it.asJsonObject) },
                foundCats = json["found_cats"]?.asJsonArray?.map { it.asString } ?: emptyList(),
            )
        }
    }
}

data class Trophy(
    val type: String,
    val timestamp: Long,
    val visits: Int,
) {
    companion object {
        fun fromJson(json: JsonObject): Trophy {
            return Trophy(
                type = json["type"].asString,
                timestamp = json["timestamp"].asLong,
                visits = json["visits"].asInt,
            )
        }
    }
}
