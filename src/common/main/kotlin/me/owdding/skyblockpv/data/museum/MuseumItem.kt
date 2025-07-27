package me.owdding.skyblockpv.data.museum

import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import me.owdding.ktcodecs.FieldName
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktcodecs.IncludedCodec
import me.owdding.skyblockpv.generated.SkyBlockPVCodecs

@GenerateCodec
data class MuseumItem(
    override val id: String,
    @FieldName("parent") override val parentId: String?,
    @FieldName("mapped_item_ids") val mappedIds: List<String> = emptyList(),
) : MuseumRepoEntry {
    companion object {
        private val COMPACT_MUSEUM_ITEM_CODEC = Codec.STRING.xmap(
            { MuseumItem(it, null, emptyList()) },
            { it.id },
        )

        @IncludedCodec(named = "museum§item")
        val MUSEUM_ITEM_CODEC: Codec<List<MuseumItem>> = Codec.either(
            COMPACT_MUSEUM_ITEM_CODEC,
            SkyBlockPVCodecs.getCodec<MuseumItem>(),
        ).xmap(
            { Either.unwrap(it) },
            { if (it.parentId == null && it.mappedIds.isEmpty()) Either.left(it) else Either.right(it) },
        ).listOf()

    }
}
