package me.owdding.skyblockpv.data.api.skills

import com.google.gson.JsonObject
import me.owdding.skyblockpv.utils.Utils
import me.owdding.skyblockpv.utils.json.getAs
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.data.SkyBlockRarity
import tech.thatgravyboat.skyblockapi.api.remote.RepoItemsAPI
import tech.thatgravyboat.skyblockapi.utils.extentions.asInt
import tech.thatgravyboat.skyblockapi.utils.extentions.asString
import tech.thatgravyboat.skyblockapi.utils.text.Text

data class TrophyFishData(
    val obtainedTypes: Map<String, Int>,
    val lastCatch: TrophyFish?,
    val totalCatches: Int,
    val rewards: List<Int>,
) {
    companion object {
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
) {
    companion object {
        fun fromJson(member: JsonObject, playerStats: JsonObject?, playerData: JsonObject?): FishData {
            val itemsFished = playerStats?.get("items_fished") as JsonObject?
            val leveling = member.get("leveling") as JsonObject?
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

data class TrophyFish(val type: TrophyFishType, val tier: TrophyFishTier) {
    val item: ItemStack by lazy { type.getItem(tier) }
    val displayName: Component by lazy {
        if (tier == TrophyFishTier.NONE) {
            return@lazy Component.empty().append(type.displayName)
        }

        Text.join(type.displayName, " ", tier.nameSuffix)
    }

    val apiName by lazy {
        if (tier == TrophyFishTier.NONE) {
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
                        TrophyFishTier.getByName(it[1]),
                    )
                }
            }
            return null
        }
    }
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
            Utils.loadFromRepo<Map<String, List<String>>>("gear/fishing")?.forEach { (key, value) ->
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

enum class TrophyFishRank(val displayName: Component) {
    NOVICE(displayName = Text.of("Novice") { withStyle(ChatFormatting.DARK_GRAY) }),
    ADEPT(displayName = Text.of("Adept") { withStyle(ChatFormatting.GRAY) }),
    EXPERT(displayName = Text.of("Expert") { withStyle(ChatFormatting.GOLD) }),
    MASTER(displayName = Text.of("Master") { withStyle(ChatFormatting.AQUA) });

    companion object {
        fun getById(id: Int): TrophyFishRank? {
            return entries.firstOrNull { it.ordinal == id }
        }
    }
}

enum class TrophyFishType(
    val displayName: Component,
    internalName: String = "",
) {
    SULPHUR_SKITTER(
        displayName = Text.of("Sulphur Skitter") {
            withStyle(ChatFormatting.WHITE)
        },
    ),
    OBFUSCATED_ONE(
        displayName = Text.of("Obfuscated 1") {
            withStyle(ChatFormatting.WHITE, ChatFormatting.OBFUSCATED)
        },
        internalName = "OBFUSCATED_FISH_1",
    ),
    STEAMING_HOT_FLOUNDER(
        displayName = Text.of("Steaming-Hot Flounder") {
            withStyle(ChatFormatting.WHITE)
        },
    ),
    GUSHER(
        displayName = Text.of("Gusher") {
            withStyle(ChatFormatting.WHITE)
        },
    ),
    BLOBFISH(
        displayName = Text.of("Blobfish") {
            withStyle(ChatFormatting.WHITE)
        },
    ),
    OBFUSCATED_TWO(
        displayName = Text.of("Obfuscated 2") {
            withStyle(ChatFormatting.GREEN, ChatFormatting.OBFUSCATED)
        },
        internalName = "OBFUSCATED_FISH_2",
    ),
    SLUGFISH(
        displayName = Text.of("Slugfish") {
            withStyle(ChatFormatting.GREEN)
        },
    ),
    FLYFISH(
        displayName = Text.of("Flyfish") {
            withStyle(ChatFormatting.GREEN)
        },
    ),
    OBFUSCATED_THREE(
        displayName = Text.of("Obfuscated 3") {
            withStyle(ChatFormatting.BLUE, ChatFormatting.OBFUSCATED)
        },
        internalName = "OBFUSCATED_FISH_3",
    ),
    LAVA_HORSE(
        displayName = Text.of("Lavahorse") {
            withStyle(ChatFormatting.BLUE)
        },
    ),
    MANA_RAY(
        displayName = Text.of("Mana Ray") {
            withStyle(ChatFormatting.BLUE)
        },
    ),
    VOLCANIC_STONEFISH(
        displayName = Text.of("Volcanic Stonefish") {
            withStyle(ChatFormatting.BLUE)
        },
    ),
    VANILLE(
        displayName = Text.of("Vanille") {
            withStyle(ChatFormatting.BLUE)
        },
    ),
    SKELETON_FISH(
        displayName = Text.of("Skeleton Fish") {
            withStyle(ChatFormatting.DARK_PURPLE)
        },
    ),
    MOLDFIN(
        displayName = Text.of("Moldfin") {
            withStyle(ChatFormatting.DARK_PURPLE)
        },
    ),
    SOUL_FISH(
        displayName = Text.of("Soul Fish") {
            withStyle(ChatFormatting.DARK_PURPLE)
        },
    ),
    KARATE_FISH(
        displayName = Text.of("Karate Fish") {
            withStyle(ChatFormatting.DARK_PURPLE)
        },
    ),
    GOLDEN_FISH(
        displayName = Text.of("Golden Fish") {
            withStyle(ChatFormatting.GOLD)
        },
    );

    val internalName: String = internalName.takeUnless { it.isEmpty() } ?: name

    val bronze by RepoItemsAPI.getItemLazy("${this.internalName}_BRONZE")
    val silver by RepoItemsAPI.getItemLazy("${this.internalName}_SILVER")
    val gold by RepoItemsAPI.getItemLazy("${this.internalName}_GOLD")
    val diamond by RepoItemsAPI.getItemLazy("${this.internalName}_DIAMOND")

    fun getItem(tier: TrophyFishTier): ItemStack {
        return when (tier) {
            TrophyFishTier.NONE -> bronze
            TrophyFishTier.BRONZE -> bronze
            TrophyFishTier.SILVER -> silver
            TrophyFishTier.GOLD -> gold
            TrophyFishTier.DIAMOND -> diamond
        }
    }

    companion object {
        fun getByInternalName(internalName: String): TrophyFishType? {
            return entries.firstOrNull { internalName.equals(it.internalName, ignoreCase = true) }
        }
    }
}

enum class TrophyFishTier(val nameSuffix: Component, val displayName: String) {
    NONE(
        nameSuffix = Component.empty(),
        displayName = "Total",
    ),
    BRONZE(
        nameSuffix = Text.of("BRONZE") {
            withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD)
        },
        displayName = "§8Bronze",
    ),
    SILVER(
        nameSuffix = Text.of("SILVER") {
            withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD)
        },
        displayName = "§7Silver",
    ),
    GOLD(
        nameSuffix = Text.of("GOLD") {
            withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)
        },
        displayName = "§6Gold",
    ),
    DIAMOND(
        nameSuffix = Text.of("DIAMOND") {
            withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD)
        },
        displayName = "§bDiamond",
    );

    companion object {
        fun getByName(name: String): TrophyFishTier {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: NONE
        }
    }
}
