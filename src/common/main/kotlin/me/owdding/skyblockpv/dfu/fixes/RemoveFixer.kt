package me.owdding.skyblockpv.dfu.fixes

import me.owdding.skyblockpv.dfu.DataComponentFixer
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.Unit

class RemoveFixer(val key: String) : DataComponentFixer<Unit> {
    override val type: DataComponentType<Unit> = DataComponents.UNBREAKABLE // just a nonsense key, doesn't get used since we return null
    override fun getData(tag: CompoundTag): Unit? {
        tag.remove(key)
        return null
    }
}
