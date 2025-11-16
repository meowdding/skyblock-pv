package me.owdding.skyblockpv.dfu

import net.minecraft.core.component.DataComponentPatch
import net.minecraft.core.component.DataComponentType
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.Tag
import net.minecraft.world.item.Item
import kotlin.jvm.optionals.getOrNull

interface DataComponentFixer<T> {

    val type: DataComponentType<T>
    fun getData(tag: CompoundTag): T?

    fun canApply(item: Item): Boolean {
        return true
    }

    fun apply(components: DataComponentPatch.Builder, tag: CompoundTag) {
        getData(tag)?.let { data -> components.set(this.type, data) }
    }

    // Helpers

    fun CompoundTag.removeIfEmpty(path: String) {
        if (this.getCompoundOrEmpty(path).isEmpty) {
            this.remove(path)
        }
    }

    fun CompoundTag.getAndRemove(path: String): Tag? {
        val tag = this.get(path)
        this.remove(path)
        return tag
    }

    fun CompoundTag.getAndRemoveCompound(key: String): CompoundTag? = this.getAndRemove(key)?.asCompound()?.getOrNull()
    fun CompoundTag.getAndRemoveIntArray(key: String): IntArray? = this.getAndRemove(key)?.asIntArray()?.getOrNull()
    fun CompoundTag.getAndRemoveBoolean(key: String): Boolean? = this.getAndRemove(key)?.asBoolean()?.getOrNull()
    fun CompoundTag.getAndRemoveString(key: String): String? = this.getAndRemove(key)?.asString()?.getOrNull()
    fun CompoundTag.getAndRemoveList(key: String): ListTag? = this.getAndRemove(key)?.asList()?.getOrNull()
    fun CompoundTag.getAndRemoveByte(key: String): Byte? = this.getAndRemove(key)?.asByte()?.getOrNull()
    fun CompoundTag.getAndRemoveInt(key: String): Int? = this.getAndRemove(key)?.asInt()?.getOrNull()
}
