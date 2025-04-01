package tech.thatgravyboat.skyblockpv.data

import tech.thatgravyboat.skyblockapi.api.data.SkyBlockRarity

enum class SortedEntry(vararg entries: String) {
    SKILLS(
        "SKILL_COMBAT",
        "SKILL_FARMING",
        "SKILL_FISHING",
        "SKILL_MINING",
        "SKILL_FORAGING",
        "SKILL_ENCHANTING",
        "SKILL_ALCHEMY",
        "SKILL_CARPENTRY",
        "SKILL_RUNECRAFTING",
        "SKILL_TAMING",
        "SKILL_SOCIAL",
    ),
    COLLECTIONS(
        "WHEAT",
        "CARROT_ITEM",
        "POTATO_ITEM",
        "PUMPKIN",
        "MELON",
        "SEEDS",
        "MUSHROOM_COLLECTION",
        "INK_SACK:3",
        "CACTUS",
        "SUGAR_CANE",
        "FEATHER",
        "LEATHER",
        "PORK",
        "RAW_CHICKEN",
        "MUTTON",
        "RABBIT",
        "NETHER_STALK",
        "COBBLESTONE",
        "COAL",
        "IRON_INGOT",
        "GOLD_INGOT",
        "DIAMOND",
        "INK_SACK:4",
        "EMERALD",
        "REDSTONE",
        "QUARTZ",
        "OBSIDIAN",
        "GLOWSTONE_DUST",
        "GRAVEL",
        "ICE",
        "NETHERRACK",
        "SAND",
        "ENDER_STONE",
        "MITHRIL_ORE",
        "HARD_STONE",
        "GEMSTONE_COLLECTION",
        "MYCEL",
        "SAND:1",
        "SULPHUR_ORE",
        "GLACITE",
        "TUNGSTEN",
        "UMBER",
        "ROTTEN_FLESH",
        "BONE",
        "STRING",
        "SPIDER_EYE",
        "SULPHUR",
        "ENDER_PEARL",
        "GHAST_TEAR",
        "SLIME_BALL",
        "BLAZE_ROD",
        "MAGMA_CREAM",
        "CHILI_PEPPER",
        "RAW_FISH",
        "RAW_FISH:1",
        "RAW_FISH:2",
        "RAW_FISH:3",
        "PRISMARINE_SHARD",
        "PRISMARINE_CRYSTALS",
        "CLAY_BALL",
        "WATER_LILY",
        "INK_SACK",
        "SPONGE",
        "MAGMA_FISH",
        "AGARICUS_CAP",
        "CADUCOUS_STEM",
        "HALF_EATEN_CARROT",
        "HEMOVIBE",
        "METAL_HEART",
        "WILTED_BERBERIS",
        "TIMITE",
    ),
    COLLECTION_CATEGORY(
        "FARMING",
        "MINING",
        "COMBAT",
        "FORAGING",
        "FISHING",
        "RIFT",
    ),
    SLAYER(
        "zombie",
        "spider",
        "wolf",
        "enderman",
        "blaze",
        "vampire",
    ),
    ESSENCE(
        "WITHER",
        "SPIDER",
        "UNDEAD",
        "DRAGON",
        "GOLD",
        "DIAMOND",
        "ICE",
        "CRIMSON",
    ),
    RARITY(
        "DIVINE",
        "MYTHIC",
        "LEGENDARY",
        "EPIC",
        "RARE",
        "UNCOMMON",
        "COMMON",
    ),
    ;

    val list = entries.toList()

    fun sortToSkyBlockOrder(input: List<String>) = input.sortedBy { list.indexOf(it) }
    fun <T> sortToSkyBlockOrder(input: Map<String, T>) = input.toList().sortedBy { list.indexOf(it.first) }.toMap()

    companion object {
        fun <T> Map<String, T>.sortToSkillsOrder() = SKILLS.sortToSkyBlockOrder(this)
        fun <T> Map<String, T>.sortToCollectionsOrder() = COLLECTIONS.sortToSkyBlockOrder(this)
        fun List<String>.sortToCollectionCategoryOrder() = COLLECTION_CATEGORY.sortToSkyBlockOrder(this)
        fun <T> Map<String, T>.sortToSlayerOrder() = SLAYER.sortToSkyBlockOrder(this)
        fun <T> Map<String, T>.sortToEssenceOrder() = ESSENCE.sortToSkyBlockOrder(this)

        fun <T> Map<SkyBlockRarity, T>.sortToRarityOrder() = this.toList().sortedBy { SkyBlockRarity.entries.indexOf(it.first) }.toMap()
    }
}
