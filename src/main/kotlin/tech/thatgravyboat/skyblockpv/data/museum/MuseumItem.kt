package tech.thatgravyboat.skyblockpv.data.museum

import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import java.util.*
import kotlin.jvm.optionals.getOrNull

data class MuseumItem(override val id: String, override val parentId: String?, val mappedIds: List<String>) : MuseumRepoEntry

private val COMPACT_MUSEUM_ITEM_CODEC = Codec.STRING.xmap(
    { MuseumItem(it, null, emptyList()) },
    { it.id },
)

private val DEFAULT_MUSEUM_ITEM_CODEC = RecordCodecBuilder.create {
    it.group(
        Codec.STRING.fieldOf("id").forGetter(MuseumItem::id),
        Codec.STRING.optionalFieldOf("parent").forGetter { Optional.empty() },
        Codec.STRING.listOf().optionalFieldOf("mapped_item_ids", emptyList()).forGetter(MuseumItem::mappedIds),
    ).apply(it, ::initItem)
}

val MUSEUM_ITEM_CODEC = Codec.either(
    COMPACT_MUSEUM_ITEM_CODEC,
    DEFAULT_MUSEUM_ITEM_CODEC,
).xmap(
    { Either.unwrap(it) },
    { if (it.parentId == null && it.mappedIds.isEmpty()) Either.left(it) else Either.right(it) },
)

private fun initItem(id: String, parent: Optional<String>, mappedIds: List<String>): MuseumItem {
    return MuseumItem(id, parent.getOrNull(), mappedIds)
}
