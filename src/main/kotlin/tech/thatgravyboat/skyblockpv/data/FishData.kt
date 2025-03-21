package tech.thatgravyboat.skyblockpv.data

import com.google.gson.JsonObject
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.data.SkyBlockRarity
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockpv.utils.Utils
import tech.thatgravyboat.skyblockpv.utils.asInt
import tech.thatgravyboat.skyblockpv.utils.asString
import tech.thatgravyboat.skyblockpv.utils.createSkull

data class TrophyFishData(
    val obtainedTypes: Map<String, Int>,
    val lastCatch: TrophyFish?,
    val totalCatches: Int,
    val rewards: List<Int>,
) {
    companion object {
        fun fromJson(member: JsonObject): TrophyFishData {
            val trophyFishData =
                member.getAsJsonObject("trophy_fish") ?: return TrophyFishData(mapOf(), null, 0, listOf())

            return TrophyFishData(
                obtainedTypes = trophyFishData.entrySet().mapNotNull {
                    if (!it.value.isJsonPrimitive) return@mapNotNull null
                    return@mapNotNull it.key to it.value.asInt(0)
                }.toMap(),
                lastCatch = trophyFishData.get("last_caught").asString("").let {
                    TrophyFish.fromString(it)
                },
                totalCatches = trophyFishData.get("total_caught").asInt(0),
                rewards = trophyFishData.get("rewards")?.asJsonArray?.map { it.asInt(0) }?.filterNot { it == 0 }
                    ?: emptyList(),
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

data class TrophyFish(val type: TrophyFishTypes, val tier: TrophyFishTiers) {
    val item: ItemStack by lazy { createSkull(type.getTexture(tier)) }
    val displayName: Component by lazy {
        if (tier == TrophyFishTiers.NONE) {
            return@lazy Component.empty().append(type.displayName)
        }

        Text.join(type.displayName, " ", tier.nameSuffix)
    }

    val apiName by lazy {
        if (tier == TrophyFishTiers.NONE) {
            return@lazy type.internalName
        }

        "${type.internalName}_${tier.name.lowercase()}"
    }

    companion object {
        fun fromString(fish: String): TrophyFish? {
            if (fish.contains("/")) {
                return fish.split("/").let {
                    TrophyFish(
                        TrophyFishTypes.getByInternalName(it[0]) ?: return null,
                        TrophyFishTiers.getByName(it[1]),
                    )
                }
            }
            return null
        }
    }
}

private fun toArmorSet(baseId: String) = listOf(
    "${baseId}_HELMET",
    "${baseId}_LEGGINGS",
    "${baseId}_BOOTS",
    "${baseId}_CHESTPLATE"
)

enum class FishingEquipment(vararg ids: String) {
    RODS(
        // Water fishing rods
        "FISHING_ROD",
        "CHALLENGE_ROD",
        "CHAMP_ROD",
        "LEGEND_ROD",
        "ROD_OF_THE_SEA",
        "DIRT_ROD",
        "GIANT_FISHING_ROD",

        // Lava fishing rods
        "POLISHED_TOPAZ_ROD",
        "STARTER_LAVA_ROD",
        "MAGMA_ROD",
        "INFERNO_ROD",
        "HELLFIRE_ROD",
        "BINGO_LAVA_ROD",
    ),
    ARMOR(
        *listOf(
            "ANGLER",
            "DIVER",
            "SPONGE",
            "SHARK_SCALE",
            "THUNDER",
            "MAGMA_LORD",
            "BACKWATER",
        ).flatMap { toArmorSet(it) }.toTypedArray(),
        "SALMON_HELMET_NEW",
        "SALMON_LEGGINGS_NEW",
        "SALMON_CHESTPLATE_NEW",
        "SALMON_BOOTS_NEW",
        "SLUG_BOOTS",
        "FLAMING_CHESTPLATE",
        "TAURUS_HELMET",
        "MOOGMA_LEGGINGS",
        "TIKI_MASK",
    ),
    TROPHY_ARMOR(
        *listOf(
            "BRONZE_HUNTER",
            "SILVER_HUNTER",
            "GOLD_HUNTER",
            "DIAMOND_HUNTER"
        ).flatMap { toArmorSet(it) }.toTypedArray()
    ),
    BELTS(
        "ICHTHYIC_BELT",
        "FINWAVE_BELT",
        "GILLSPLASH_BELT",
        "ANGLER_BELT",
        "BACKWATER_BELT",
        "SPONGE_BELT"
    ),
    CLOAKS(
        "ICHTHYIC_CLOAK",
        "FINWAVE_CLOAK",
        "GILLSPLASH_CLOAK",
        "CLOWNFISH_CLOAK",
        "ANGLER_CLOAK",
        "BACKWATER_CLOAK"
    ),
    NECKLACES(
        "THUNDERBOLT_NECKLACE",
        "ANGLER_NECKLACE",
        "BACKWATER_NECKLACE",
        "PRISMARINE_NECKLACE",
        "TERA_SHELL_NECKLACE"
    ),
    GLOVES(
        "MAGMA_LORD_GAUNTLET",
        "ICHTHYIC_GLOVES",
        "FINWAVE_GLOVES",
        "GILLSPLASH_GLOVES",
        "LUMINOUS_BRACELET",
        "BACKWATER_GLOVES",
        "ANGLER_BRACELET",
        "CLAY_BRACELET",
    ),
    HOOK(
        "COMMON_HOOK",
        "HOTSPOT_HOOK",
        "PHANTOM_HOOK",
        "TREASURE_HOOK",
    ),
    LINE(
        "SHREDDED_LINE",
        "SPEEDY_LINE",
        "TITAN_LINE",
    ),
    SINKER(
        "CHUM_SINKER",
        "FESTIVE_SINKER",
        "HOTSPOT_SINKER",
        "ICY_SINKER",
        "JUNK_SINKER",
        "PRISMARINE_SINKER",
        "SPONGE_SINKER",
        "STINGY_SINKER",
    ),
    ;

    val list = ids.toList()

    companion object {
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

enum class DolphinBrackets(val killsRequired: Int, val rarity: SkyBlockRarity) {
    COMMON(250, SkyBlockRarity.COMMON),
    UNCOMMON(1000, SkyBlockRarity.UNCOMMON),
    RARE(2500, SkyBlockRarity.RARE),
    EPIC(5000, SkyBlockRarity.EPIC),
    LEGENDARY(10000, SkyBlockRarity.LEGENDARY);

    companion object {
        fun getByKills(kills: Int): DolphinBrackets? {
            return DolphinBrackets.entries.reversed().firstOrNull { it.killsRequired <= kills }
        }
    }
}

enum class TrophyFishRanks(val displayName: Component) {
    NOVICE(displayName = Text.of("Novice") { withStyle(ChatFormatting.DARK_GRAY) }),
    ADEPT(displayName = Text.of("Adept") { withStyle(ChatFormatting.GRAY) }),
    EXPERT(displayName = Text.of("Expert") { withStyle(ChatFormatting.GOLD) }),
    MASTER(displayName = Text.of("Master") { withStyle(ChatFormatting.AQUA) });

    companion object {
        fun getById(id: Int): TrophyFishRanks? {
            return entries.firstOrNull { it.ordinal == id }
        }
    }
}

enum class TrophyFishTypes(
    private var bronze: String = "",
    private var silver: String = "",
    private var gold: String = "",
    private var diamond: String = "",
    val displayName: Component,
    internalName: String = "",
) {
    SULPHUR_SKITTER(
        displayName = Text.of("Sulphur Skitter") {
            withStyle(ChatFormatting.WHITE)
        },
    ),
    OBFUSCATED_ONE(
        internalName = "obfuscated_fish_1",
        displayName = Text.of("Obfuscated 1") {
            withStyle(ChatFormatting.WHITE, ChatFormatting.OBFUSCATED)
        },
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
        internalName = "obfuscated_fish_2",
        displayName = Text.of("Obfuscated 2") {
            withStyle(ChatFormatting.GREEN, ChatFormatting.OBFUSCATED)
        },
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
        internalName = "obfuscated_fish_3",
        displayName = Text.of("Obfuscated 3") {
            withStyle(ChatFormatting.BLUE, ChatFormatting.OBFUSCATED)
        },
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

    val internalName: String

    fun getTexture(tier: TrophyFishTiers): String {
        return when (tier) {
            TrophyFishTiers.NONE -> bronze
            TrophyFishTiers.BRONZE -> bronze
            TrophyFishTiers.SILVER -> silver
            TrophyFishTiers.GOLD -> gold
            TrophyFishTiers.DIAMOND -> diamond
        }
    }

    private fun setTexture(tier: TrophyFishTiers, skin: String) {
        when (tier) {
            TrophyFishTiers.NONE -> bronze = skin
            TrophyFishTiers.BRONZE -> bronze = skin
            TrophyFishTiers.SILVER -> silver = skin
            TrophyFishTiers.GOLD -> gold = skin
            TrophyFishTiers.DIAMOND -> diamond = skin
        }
    }

    companion object {
        init {
            val textures = Utils.loadFromRepo<Map<String, Map<String, String>>>("trophy_fish_skins") ?: emptyMap()

            textures.entries.forEach { (key, tiers) ->
                val type = TrophyFishTypes.valueOf(key.uppercase())

                tiers.entries.forEach { (tier, skin) ->
                    type.setTexture(TrophyFishTiers.valueOf(tier.uppercase()), skin)
                }
            }
        }

        fun getByInternalName(internalName: String): TrophyFishTypes? {
            return entries.firstOrNull { internalName.equals(it.internalName, ignoreCase = true) }
        }
    }

    init {
        if (internalName.isEmpty()) {
            this.internalName = name.lowercase()
        } else {
            this.internalName = internalName
        }
    }
}

enum class TrophyFishTiers(val nameSuffix: Component, val displayName: String) {
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
        fun getByName(name: String): TrophyFishTiers {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: NONE
        }
    }
}
