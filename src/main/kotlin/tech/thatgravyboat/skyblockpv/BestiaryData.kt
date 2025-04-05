package tech.thatgravyboat.skyblockpv

import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import tech.thatgravyboat.skyblockpv.utils.codecs.ReservedUnboundMapCodec

typealias BestiaryIcon = Either<String, Pair<String, String>>
typealias BestiaryCategoriesEntry = Either<BestiaryCategoryEntry, Map<String, BestiaryCategoryEntry>>

object BestiaryData {

    private val ICON: MapCodec<BestiaryIcon> = Codec.mapEither(
        Codec.STRING.fieldOf("item"),
        RecordCodecBuilder.mapCodec<Pair<String, String>> {
            it.group(
                Codec.STRING.fieldOf("skullOwner").forGetter { it.first },
                Codec.STRING.fieldOf("texture").forGetter { it.second },
            ).apply(it, ::Pair)
        },
    )

    private val MOB_ENTRY_CODEC: Codec<BestiaryMobEntry> = RecordCodecBuilder.create {
        it.group(
            Codec.STRING.fieldOf("name").forGetter(BestiaryMobEntry::name),
            ICON.forGetter(BestiaryMobEntry::icon),
            Codec.INT.fieldOf("cap").forGetter(BestiaryMobEntry::cap),
            Codec.STRING.listOf().fieldOf("mobs").forGetter(BestiaryMobEntry::mobs),
            Codec.INT.fieldOf("bracket").forGetter(BestiaryMobEntry::bracket),
        ).apply(it, ::BestiaryMobEntry)
    }

    private val CATEGORY_ENTRY_CODEC: Codec<BestiaryCategoryEntry> = RecordCodecBuilder.create {
        it.group(
            Codec.STRING.fieldOf("name").forGetter(BestiaryCategoryEntry::name),
            ICON.fieldOf("icon").forGetter(BestiaryCategoryEntry::icon),
            MOB_ENTRY_CODEC.listOf().fieldOf("mobs").forGetter(BestiaryCategoryEntry::mobs),
        ).apply(it, ::BestiaryCategoryEntry)
    }
    private val SIMPLE_CATEGORY_CODEC: MapCodec<BestiaryCategoriesEntry> = MapCodec.assumeMapUnsafe(
        CATEGORY_ENTRY_CODEC.xmap(
            { Either.left(it) },
            { it.left().orElseThrow() },
        ),
    )
    private val COMPLEX_CATEGORY_CODEC: MapCodec<BestiaryCategoriesEntry> = MapCodec.assumeMapUnsafe(
        ReservedUnboundMapCodec(
            Codec.STRING,
            CATEGORY_ENTRY_CODEC,
            "name", "icon", "hasSubcategories"
        ).xmap(
            { Either.right(it) },
            { it.right().orElseThrow() },
        ),
    )
    private val CATEGORY_CODEC: Codec<BestiaryCategoriesEntry> = Codec.BOOL.dispatch(
        "hasSubcategories",
        { it.right().isPresent },
        { if (it) COMPLEX_CATEGORY_CODEC else SIMPLE_CATEGORY_CODEC },
    )

    private val BRACKETS_CODEC: Codec<Map<Int, List<Int>>> = Codec.unboundedMap(
        Codec.STRING.xmap({ it.toIntOrNull() ?: 0 }, { it.toString() }),
        Codec.INT.listOf(),
    )
    private val CATEGORIES_CODEC: MapCodec<Map<String, BestiaryCategoriesEntry>> = MapCodec.assumeMapUnsafe(
        ReservedUnboundMapCodec(
            Codec.STRING,
            CATEGORY_CODEC,
            "brackets",
        ),
    )

    val CODEC: Codec<BestiaryRepoData> = RecordCodecBuilder.create {
        it.group(
            BRACKETS_CODEC.fieldOf("brackets").forGetter(BestiaryRepoData::brackets),
            CATEGORIES_CODEC.forGetter(BestiaryRepoData::categories),
        ).apply(it, ::BestiaryRepoData)
    }

}

data class BestiaryRepoData(
    val brackets: Map<Int, List<Int>>,
    val categories: Map<String, BestiaryCategoriesEntry>,
)

data class BestiaryCategoryEntry(
    val name: String,
    val icon: BestiaryIcon,
    val mobs: List<BestiaryMobEntry>,
)

data class BestiaryMobEntry(
    val name: String,
    val icon: BestiaryIcon,
    val cap: Int,
    val mobs: List<String>,
    val bracket: Int,
)
