package me.owdding.skyblockpv.dfu.fixes

import me.owdding.skyblockpv.dfu.DataComponentFixer
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation

object ItemModelFix : DataComponentFixer<ResourceLocation> {

    private val cache: MutableMap<String, ResourceLocation?> = mutableMapOf()
    override val type: DataComponentType<ResourceLocation> = DataComponents.ITEM_MODEL

    override fun getData(tag: CompoundTag) = tag.getAndRemoveString("ItemModel")?.let {
        cache.getOrPut(it) {
            ResourceLocation.tryParse(it)
        }
    }

}
