package me.owdding.skyblockpv.data.api.skills

import com.google.gson.JsonObject
import me.owdding.skyblockpv.utils.Utils
import me.owdding.skyblockpv.utils.json.getAs
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.data.SkyBlockRarity
import tech.thatgravyboat.skyblockapi.api.datatype.defaults.trophy.TrophyTier
import tech.thatgravyboat.skyblockapi.api.repo.apis.SkyBlockItemsRepo
import tech.thatgravyboat.skyblockapi.utils.extentions.asInt
import tech.thatgravyboat.skyblockapi.utils.extentions.asString
import tech.thatgravyboat.skyblockapi.utils.extentions.asStringList
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

data class TrophyFishData(
    val obtainedTypes: Map<String, Int>,
    val lastCatch: TrophyFish?,
    val totalCatches: Int,
    val rewards: List<Int>,
) {
    companion object {
        val EMPTY = TrophyFishData(emptyMap(), null, 0, emptyList())
        fun fromJson(member: JsonObject): TrophyFishData {
            val trophyFishData = member.getAs<JsonObject>("trophy_fish") ?: return TrophyFishData(mapOf(), null, 0, listOf())

            return TrophyFishData(
                obtainedTypes = trophyFishData.entrySet().mapNotNull {
                    if (!it.value.isJsonPrimitive) return@mapNotNull null
                    return@mapNotNull it.key to it.value.asInt(0)
                }.toMap(),
                lastCatch = trophyFishData.get("last_caught").asString("").let { TrophyFish.fromString(it) },
                totalCatches = trophyFishData.get("total_caught").asInt(0),
                rewards = trophyFishData.get("rewards")?.asJsonArray?.map { it.asInt(0) }?.filterNot { it == 0 } ?: emptyList(),
            )
        }
    }
}

data class FishData(
    val treasuresCaught: Int,
    val festivalSharksKilled: Int,
    val itemsFished: ItemsFished,
    val trophyFrogs: TrophyFrogData,
) {
    companion object {
        val EMPTY = FishData(0, 0, ItemsFished(0, 0, 0, 0, 0), TrophyFrogData.EMPTY)
        fun fromJson(member: JsonObject, playerStats: JsonObject?, playerData: JsonObject?): FishData {
            val itemsFished = playerStats?.get("items_fished") as JsonObject?
            val leveling = member.get("leveling") as JsonObject?
            val completedTasks = leveling?.get("completed_tasks").asStringList().toSet()
            return FishData(
                treasuresCaught = playerData?.get("fishing_treasure_caught").asInt(0),
                festivalSharksKilled = leveling?.get("fishing_festival_sharks_killed").asInt(0),
                itemsFished = ItemsFished(
                    total = itemsFished?.get("total").asInt(0),
                    normal = itemsFished?.get("normal").asInt(0),
                    treasure = itemsFished?.get("treasure").asInt(0),
                    largeTreasure = itemsFished?.get("large_treasure").asInt(0),
                    trophyFish = itemsFished?.get("trophy_fish").asInt(0),
                ),
                trophyFrogs = TrophyFrogData(
                    totalCatches = itemsFished?.get("trophy_frog").asInt(0),
                    completedTasks = completedTasks.filterTo(mutableSetOf()) { it.startsWith("TROPHY_") },
                ),
            )
        }
    }
}

data class ItemsFished(
    val total: Int,
    val normal: Int,
    val treasure: Int,
    val largeTreasure: Int,
    val trophyFish: Int,
)

data class TrophyFrogData(
    val totalCatches: Int,
    val completedTasks: Set<String>,
) {
    fun hasCompleted(frog: TrophyFrog) = frog.completedTask in completedTasks

    companion object {
        val EMPTY = TrophyFrogData(0, emptySet())
    }
}

data class TrophyFish(val type: TrophyFishType, val tier: TrophyTier) {
    val item: ItemStack by lazy { type.getItem(tier) }
    val displayName: Component by lazy {
        if (tier == TrophyTier.NONE) {
            return@lazy Component.empty().append(type.displayName)
        }

        Text.join(type.displayName, " ", tier.nameSuffix)
    }

    val apiName by lazy {
        if (tier == TrophyTier.NONE) {
            return@lazy type.internalName.lowercase()
        }

        "${type.internalName.lowercase()}_${tier.name.lowercase()}"
    }

    companion object {
        fun fromString(fish: String): TrophyFish? {
            if (fish.contains("/")) {
                return fish.split("/").let {
                    TrophyFish(
                        TrophyFishType.getByInternalName(it[0]) ?: return null,
                        TrophyTier.getByName(it[1]),
                    )
                }
            }
            return null
        }
    }
}

data class TrophyFrog(val type: TrophyFrogType, val tier: TrophyTier) {
    val item: ItemStack by lazy { type.getItem(tier) }
    val displayName: Component by lazy { Text.join(type.displayName, " ", tier.nameSuffix) }
    val completedTask: String by lazy { "TROPHY_${type.internalName}_${tier.name}" }
}

enum class FishingGear {
    RODS,
    ARMOR,
    TROPHY_ARMOR,
    BELTS,
    CLOAKS,
    NECKLACES,
    GLOVES,
    HOOK,
    LINE,
    SINKER,
    ;

    var list: List<String> = emptyList()
        private set

    companion object {
        init {
            Utils.loadFromRemoteRepo<Map<String, List<String>>>("pv/gear/fishing")?.forEach { (key, value) ->
                runCatching { valueOf(key.uppercase()).list = value }.onFailure { it.printStackTrace() }
            }
        }

        val cloaks = CLOAKS.list
        val gloves = GLOVES.list
        val necklaces = NECKLACES.list
        val belts = BELTS.list
        val equipment = listOf(cloaks, gloves, necklaces, belts).flatten()
        val rods = RODS.list
        val armor = ARMOR.list
        val trophyArmor = TROPHY_ARMOR.list
        val hook = HOOK.list
        val line = LINE.list
        val sinker = SINKER.list
        val parts = listOf(hook, line, sinker).flatten()
    }
}

enum class DolphinBracket(val killsRequired: Int, val rarity: SkyBlockRarity) {
    COMMON(250, SkyBlockRarity.COMMON),
    UNCOMMON(1000, SkyBlockRarity.UNCOMMON),
    RARE(2500, SkyBlockRarity.RARE),
    EPIC(5000, SkyBlockRarity.EPIC),
    LEGENDARY(10000, SkyBlockRarity.LEGENDARY);

    companion object {
        fun getByKills(kills: Int): DolphinBracket? {
            return DolphinBracket.entries.reversed().firstOrNull { it.killsRequired <= kills }
        }
    }
}

enum class TrophyFrogType(
    val displayName: Component,
    val obtaining: Component,
    internalName: String = "",
) {
    COMMON_FROG(
        displayName = Text.of("Common Frog") {
            color = TextColor.WHITE
        },
        obtaining = "Caught everywhere.",
    ),
    LEAP_FROG(
        displayName = Text.of("Leap Frog") {
            color = TextColor.GREEN
        },
        obtaining = "Caught while midair.",
    ),
    WETLANDS_FROG(
        displayName = Text.of("Wetlands Frog") {
            color = TextColor.GREEN
        },
        obtaining = "Caught during rain.",
    ),
    REALITY_HOPPER(
        displayName = Text.of("Reality Hopper") {
            color = TextColor.GREEN
        },
        obtaining = "Caught in Wormholes on the Lotus Atoll.",
    ),
    EXPLODING_FROG(
        displayName = Text.of("Exploding Frog") {
            color = TextColor.GREEN
        },
        obtaining = "Obtained by combining Lily Pads until they explode.",
    ),
    BLESSED_FROG(
        displayName = Text.of("Blessed Frog") {
            color = TextColor.BLUE
        },
        obtaining = "Caught with an active Frogcoin blessing.",
    ),
    SEA_FROG(
        displayName = Text.of("Sea Frog") {
            color = TextColor.BLUE
        },
        obtaining = "Caught while underwater.",
    ),
    BULLFROG(
        displayName = Text.of("Bullfrog") {
            color = TextColor.BLUE
        },
        obtaining = "Caught while wearing a Red Sweater.",
    ),
    TREE_FROG(
        displayName = Text.of("Tree Frog") {
            color = TextColor.DARK_PURPLE
        },
        obtaining = "Caught while standing on leaves.",
    ),
    CAVE_FROG(
        displayName = Text.of("Cave Frog") {
            color = TextColor.DARK_PURPLE
        },
        obtaining = "Found in the Lotus Eater's Cave.",
    ),
    HIGHLANDS_FROG(
        displayName = Text.of("Highlands Frog") {
            color = TextColor.DARK_PURPLE
        },
        obtaining = "Found in the Lotus Highlands.",
    ),
    PUDDLE_JUMPER(
        displayName = Text.of("Puddle Jumper") {
            color = TextColor.GOLD
        },
        obtaining = "Caught when flying around the Lotus Atoll.",
    );

    constructor(displayName: Component, obtaining: String, internalName: String = "") : this(
        displayName,
        Text.of(obtaining) { color = TextColor.GRAY },
        internalName,
    )

    val internalName: String = internalName.takeUnless { it.isEmpty() } ?: name

    val bronze get() = SkyBlockItemsRepo.getItemStackOrDefault("${this.internalName}_BRONZE")
    val silver get() = SkyBlockItemsRepo.getItemStackOrDefault("${this.internalName}_SILVER")
    val gold get() = SkyBlockItemsRepo.getItemStackOrDefault("${this.internalName}_GOLD")
    val diamond get() = SkyBlockItemsRepo.getItemStackOrDefault("${this.internalName}_DIAMOND")

    fun getItem(tier: TrophyTier): ItemStack {
        return when (tier) {
            TrophyTier.NONE -> bronze
            TrophyTier.BRONZE -> bronze
            TrophyTier.SILVER -> silver
            TrophyTier.GOLD -> gold
            TrophyTier.DIAMOND -> diamond
        }
    }
}

enum class TrophyFishType(
    val displayName: Component,
    val obtaining: Component,
    internalName: String = "",
) {
    SULPHUR_SKITTER(
        displayName = Text.of("Sulphur Skitter") {
            color = TextColor.WHITE
        },
        obtaining = "Caught near Sulphur blocks.",
    ),
    OBFUSCATED_ONE(
        displayName = Text.of("Obfuscated 1") {
            withStyle(ChatFormatting.WHITE, ChatFormatting.OBFUSCATED)
        },
        obtaining = Text.of("Caught with Corrupted Bait.") {
            color = TextColor.GRAY
        },
        internalName = "OBFUSCATED_FISH_1",
    ),
    STEAMING_HOT_FLOUNDER(
        displayName = Text.of("Steaming-Hot Flounder") {
            color = TextColor.WHITE
        },
        obtaining = "Found in Volcano Geysers.",
    ),
    GUSHER(
        displayName = Text.of("Gusher") {
            color = TextColor.WHITE
        },
        obtaining = "Caught after a volcano eruption.",
    ),
    BLOBFISH(
        displayName = Text.of("Blobfish") {
            color = TextColor.WHITE
        },
        obtaining = "Caught everywhere.",
    ),
    OBFUSCATED_TWO(
        displayName = Text.of("Obfuscated 2") {
            withStyle(ChatFormatting.GREEN, ChatFormatting.OBFUSCATED)
        },
        obtaining = Text.of("Caught with ") {
            color = TextColor.GRAY
            append("Obfuscated 1 ") { withStyle(ChatFormatting.OBFUSCATED) }
            append("Bait.")
        },
        internalName = "OBFUSCATED_FISH_2",
    ),
    SLUGFISH(
        displayName = Text.of("Slugfish") {
            color = TextColor.GREEN
        },
        obtaining = "Bobber must be active for 20 seconds.",
    ),
    FLYFISH(
        displayName = Text.of("Flyfish") {
            color = TextColor.GREEN
        },
        obtaining = listOf("Caught from 8 blocks above.", "Found in Blazing Volcano."),
    ),
    OBFUSCATED_THREE(
        displayName = Text.of("Obfuscated 3") {
            withStyle(ChatFormatting.BLUE, ChatFormatting.OBFUSCATED)
        },
        obtaining = Text.of("Caught with ") {
            color = TextColor.GRAY
            append("Obfuscated 2 ") { withStyle(ChatFormatting.OBFUSCATED) }
            append("Bait.")

        },
        internalName = "OBFUSCATED_FISH_3",
    ),
    LAVA_HORSE(
        displayName = Text.of("Lavahorse") {
            color = TextColor.BLUE
        },
        obtaining = "Caught everywhere.",
    ),
    MANA_RAY(
        displayName = Text.of("Mana Ray") {
            color = TextColor.BLUE
        },
        obtaining = listOf("Lured by having a high amount of mana.", " §o(at least 1,200)"),
    ),
    VOLCANIC_STONEFISH(
        displayName = Text.of("Volcanic Stonefish") {
            color = TextColor.BLUE
        },
        obtaining = "Found in Blazing Volcano.",
    ),
    VANILLE(
        displayName = Text.of("Vanille") {
            color = TextColor.BLUE
        },
        obtaining = Text.multiline(
            Text.of("Only caught with ") {
                color = TextColor.GRAY
                append("Starter Lava") { color = TextColor.GREEN }
            },
            Text.of("Rod ") {
                color = TextColor.GREEN
                append("with no enchantments.") { color = TextColor.GRAY }
            },
        ),
    ),
    SKELETON_FISH(
        displayName = Text.of("Skeleton Fish") {
            color = TextColor.DARK_PURPLE
        },
        obtaining = "Found in Burning Desert.",
    ),
    MOLDFIN(
        displayName = Text.of("Moldfin") {
            color = TextColor.DARK_PURPLE
        },
        obtaining = "Found in Mystic Marsh.",
    ),
    SOUL_FISH(
        displayName = Text.of("Soul Fish") {
            color = TextColor.DARK_PURPLE
        },
        obtaining = "Found in Stronghold.",
    ),
    KARATE_FISH(
        displayName = Text.of("Karate Fish") {
            color = TextColor.DARK_PURPLE
        },
        obtaining = "Found in Dojo.",
    ),
    GOLDEN_FISH(
        displayName = Text.of("Golden Fish") {
            color = TextColor.GOLD
        },
        obtaining = "Found swimming around in the lava.",
    );

    constructor(displayName: Component, obtaining: String, internalName: String = "") : this(
        displayName,
        Text.of(obtaining) { color = TextColor.GRAY },
        internalName,
    )

    constructor(displayName: Component, obtaining: List<String>, internalName: String = "") : this(
        displayName,
        Text.multiline(obtaining) { color = TextColor.GRAY },
        internalName,
    )

    val internalName: String = internalName.takeUnless { it.isEmpty() } ?: name

    val bronze get() = SkyBlockItemsRepo.getItemStackOrDefault("${this.internalName}_BRONZE")
    val silver get() = SkyBlockItemsRepo.getItemStackOrDefault("${this.internalName}_SILVER")
    val gold get() = SkyBlockItemsRepo.getItemStackOrDefault("${this.internalName}_GOLD")
    val diamond get() = SkyBlockItemsRepo.getItemStackOrDefault("${this.internalName}_DIAMOND")

    fun getItem(tier: TrophyTier): ItemStack {
        return when (tier) {
            TrophyTier.NONE -> bronze
            TrophyTier.BRONZE -> bronze
            TrophyTier.SILVER -> silver
            TrophyTier.GOLD -> gold
            TrophyTier.DIAMOND -> diamond
        }
    }

    companion object {
        fun getByInternalName(internalName: String): TrophyFishType? {
            return entries.firstOrNull { internalName.equals(it.internalName, ignoreCase = true) }
        }
    }
}
