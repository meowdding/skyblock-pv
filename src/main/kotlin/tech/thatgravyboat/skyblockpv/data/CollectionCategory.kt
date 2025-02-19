package tech.thatgravyboat.skyblockpv.data

import net.minecraft.world.item.ItemStack

data class CollectionItem(
    val category: CollectionCategory,
    val itemId: String,
    val itemStack: ItemStack?,
    val amount: Long,
)

enum class CollectionCategory(val displayName: String, vararg val collections: String) {
    FARMING(
        "Farming",
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
    ),
    MINING(
        "Mining",
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
    ),
    COMBAT(
        "Combat",
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
    ),
    FORAGING(
        "Foraging",
        "LOG",
        "LOG:1",
        "LOG:2",
        "LOG_2:1",
        "LOG_2",
        "LOG:3",
    ),
    FISHING(
        "Fishing",
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
    ),
    RIFT(
        "Rift",
        "AGARICUS_CAP",
        "CADUCOUS_STEM",
        "HALF_EATEN_CARROT",
        "HEMOVIBE",
        "METAL_HEART",
        "WILTED_BERBERIS",
        "TIMITE",
    ),
    ;

    companion object {
        fun getCategoryByItemName(name: String) = entries.find { name in it.collections }
    }
}

