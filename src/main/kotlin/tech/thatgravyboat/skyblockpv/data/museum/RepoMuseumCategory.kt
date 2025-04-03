package tech.thatgravyboat.skyblockpv.data.museum

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockpv.api.ItemAPI

data class RepoMuseumCategory(
    val name: String,
    val location: ResourceLocation,
    val item: Lazy<ItemStack>,
    val categories: List<String>,
    val items: List<String>,
    val priority: Int,
)

val MUSEUM_CATEGORY_CODEC = RecordCodecBuilder.create {
    it.group(
        Codec.STRING.fieldOf("name").forGetter(RepoMuseumCategory::name),
        ResourceLocation.CODEC.fieldOf("display").forGetter(RepoMuseumCategory::location),
        Codec.STRING.listOf().fieldOf("categories").forGetter(RepoMuseumCategory::categories),
        Codec.STRING.listOf().optionalFieldOf("items", emptyList()).forGetter(RepoMuseumCategory::categories),
        Codec.INT.optionalFieldOf("priority", 0).forGetter(RepoMuseumCategory::priority),
    ).apply(it) { name, itemLocation, categories, items, priority ->
        RepoMuseumCategory(
            name,
            itemLocation,
            lazy {
                if (itemLocation.namespace.equals("minecraft")) {
                    return@lazy BuiltInRegistries.ITEM.get(itemLocation).get().value().defaultInstance
                } else if (itemLocation.namespace.equals("skyblock")) {
                    return@lazy ItemAPI.getItem(itemLocation.path.uppercase())
                }
                throw UnsupportedOperationException("Unsupported item location $itemLocation")
            },
            categories,
            items,
            priority,
        )
    }
}
