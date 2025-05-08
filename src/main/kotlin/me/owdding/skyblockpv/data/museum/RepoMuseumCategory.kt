package me.owdding.skyblockpv.data.museum

import me.owdding.ktcodecs.FieldName
import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktcodecs.NamedCodec
import net.minecraft.world.item.ItemStack

@GenerateCodec
data class RepoMuseumCategory(
    val name: String,
    @NamedCodec("lazy_item_ref") @FieldName("display") val item: Lazy<ItemStack>,
    val categories: List<String>,
    val items: List<String> = emptyList(),
    val priority: Int = 0,
)
