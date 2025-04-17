package tech.thatgravyboat.skyblockpv.data.museum

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockpv.utils.codecs.CodecUtils

data class RepoMuseumCategory(
    val name: String,
    val item: Lazy<ItemStack>,
    val categories: List<String>,
    val items: List<String>,
    val priority: Int,
)

val MUSEUM_CATEGORY_CODEC = RecordCodecBuilder.create {
    it.group(
        Codec.STRING.fieldOf("name").forGetter(RepoMuseumCategory::name),
        CodecUtils.ITEM_REFERENCE.fieldOf("display").forGetter(RepoMuseumCategory::item),
        Codec.STRING.listOf().fieldOf("categories").forGetter(RepoMuseumCategory::categories),
        Codec.STRING.listOf().optionalFieldOf("items", emptyList()).forGetter(RepoMuseumCategory::categories),
        Codec.INT.optionalFieldOf("priority", 0).forGetter(RepoMuseumCategory::priority),
    ).apply(it) { name, itemReference, categories, items, priority ->
        RepoMuseumCategory(
            name,
            itemReference,
            categories,
            items,
            priority,
        )
    }
}
