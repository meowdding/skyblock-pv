package me.owdding.skyblockpv.data.repo

import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktcodecs.IncludedCodec
import me.owdding.ktcodecs.NamedCodec
import me.owdding.ktcodecs.Unnamed
import me.owdding.skyblockpv.generated.SkyBlockPVCodecs
import me.owdding.skyblockpv.utils.Utils
import me.owdding.skyblockpv.utils.codecs.DispatchedCodec
import me.owdding.skyblockpv.utils.codecs.ExtraData
import me.owdding.skyblockpv.utils.codecs.LoadData
import me.owdding.skyblockpv.utils.codecs.ReservedUnboundMapCodec
import kotlin.jvm.optionals.getOrNull

typealias BestiaryIcon = Either<String, Pair<String, String>>
typealias BestiaryCategoriesEntry = Either<BestiaryCategoryEntry, ComplexBestiaryCategoryEntry>

@LoadData
object BestiaryCodecs : ExtraData {

    lateinit var data: BestiaryRepoData
        private set

    val allMobs: List<String> by lazy {
        data.categories.values.flatMap { categoryEntry ->
            (categoryEntry.left().getOrNull()?.mobs
                ?: categoryEntry.right().getOrNull()?.subcategories?.values?.flatMap { subcategory -> subcategory.mobs }
                ?: emptyList()).flatMap { it.mobs }
        }
    }

    @IncludedCodec(named = "be§icon")
    val ICON: MapCodec<BestiaryIcon> = Codec.mapEither(
        Codec.STRING.fieldOf("item"),
        RecordCodecBuilder.mapCodec {
            it.group(
                Codec.STRING.fieldOf("skullOwner").forGetter { it.first },
                Codec.STRING.fieldOf("texture").forGetter { it.second },
            ).apply(it, ::Pair)
        },
    )

    private val SIMPLE_CATEGORY_CODEC: MapCodec<BestiaryCategoriesEntry> = MapCodec.assumeMapUnsafe(
        SkyBlockPVCodecs.getCodec<BestiaryCategoryEntry>().xmap(
            { Either.left(it) },
            { it.left().orElseThrow() },
        ),
    )
    private val COMPLEX_CATEGORY_CODEC: MapCodec<BestiaryCategoriesEntry> = RecordCodecBuilder.mapCodec {
        it.group(
            Codec.STRING.fieldOf("name").forGetter(ComplexBestiaryCategoryEntry::name),
            ICON.fieldOf("icon").forGetter(ComplexBestiaryCategoryEntry::icon),
            MapCodec.assumeMapUnsafe(
                ReservedUnboundMapCodec(
                    Codec.STRING,
                    SkyBlockPVCodecs.getCodec<BestiaryCategoryEntry>(),
                    "name", "icon", "hasSubcategories",
                ),
            ).forGetter(ComplexBestiaryCategoryEntry::subcategories),
        ).apply(it, ::ComplexBestiaryCategoryEntry)
    }.xmap(
        { Either.right(it) },
        { it.right().orElseThrow() },
    )

    private val CATEGORY_CODEC: Codec<BestiaryCategoriesEntry> = DispatchedCodec(
        Codec.BOOL.optionalFieldOf("hasSubcategories", false),
        { it.right().isPresent },
        { DataResult.success(if (it) COMPLEX_CATEGORY_CODEC else SIMPLE_CATEGORY_CODEC) },
    ).codec()

    @IncludedCodec(named = "be§brackets")
    val BRACKETS_CODEC: Codec<Map<Int, List<Int>>> = Codec.unboundedMap(
        Codec.STRING.xmap({ it.toIntOrNull() ?: 0 }, { it.toString() }),
        Codec.INT.listOf(),
    )

    @IncludedCodec(named = "be§categories")
    val CATEGORIES_CODEC: MapCodec<Map<String, BestiaryCategoriesEntry>> = MapCodec.assumeMapUnsafe(
        ReservedUnboundMapCodec(
            Codec.STRING,
            CATEGORY_CODEC,
            "brackets",
        ),
    )

    override suspend fun load() {
        data = Utils.loadRepoData<BestiaryRepoData>("bestiary")
    }
}

@GenerateCodec
data class BestiaryRepoData(
    @NamedCodec("be§brackets") val brackets: Map<Int, List<Int>>,
    @NamedCodec("be§categories") @Unnamed val categories: Map<String, BestiaryCategoriesEntry>,
)


@GenerateCodec
data class BestiaryCategoryEntry(
    val name: String,
    @NamedCodec("be§icon") val icon: BestiaryIcon,
    val mobs: List<BestiaryMobEntry>,
)

data class ComplexBestiaryCategoryEntry(
    val name: String,
    val icon: BestiaryIcon,
    val subcategories: Map<String, BestiaryCategoryEntry>,
)

@GenerateCodec
data class BestiaryMobEntry(
    val name: String,
    @NamedCodec("be§icon") @Unnamed val icon: BestiaryIcon,
    val cap: Int,
    val mobs: List<String>,
    val bracket: Int,
)
