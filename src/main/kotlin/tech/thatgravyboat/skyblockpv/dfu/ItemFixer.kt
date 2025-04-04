package tech.thatgravyboat.skyblockpv.dfu

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.Tag
import net.minecraft.world.item.ItemStack
import kotlin.jvm.optionals.getOrNull

interface ItemFixer {

    fun fix(stack: ItemStack, tag: CompoundTag)
    fun shouldApply(stack: ItemStack): Boolean = true

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

    fun CompoundTag.getAndRemoveString(key: String): String? = this.getAndRemove(key)?.asString()?.getOrNull()
    fun CompoundTag.getAndRemoveList(key: String): ListTag? = this.getAndRemove(key)?.asList()?.getOrNull()
    fun CompoundTag.getAndRemoveShort(key: String): Short? = this.getAndRemove(key)?.asShort()?.getOrNull()
    fun CompoundTag.getAndRemoveByte(key: String): Byte? = this.getAndRemove(key)?.asByte()?.getOrNull()
    fun CompoundTag.getAndRemoveInt(key: String): Int? = this.getAndRemove(key)?.asInt()?.getOrNull()
    fun CompoundTag.getAndRemoveLong(key: String): Long? = this.getAndRemove(key)?.asLong()?.getOrNull()
    fun CompoundTag.getAndRemoveFloat(key: String): Float? = this.getAndRemove(key)?.asFloat()?.getOrNull()
    fun CompoundTag.getAndRemoveDouble(key: String): Double? = this.getAndRemove(key)?.asDouble()?.getOrNull()
    fun CompoundTag.getAndRemoveBoolean(key: String): Boolean? = this.getAndRemove(key)?.asBoolean()?.getOrNull()
    fun CompoundTag.getAndRemoveCompound(key: String): CompoundTag? = this.getAndRemove(key)?.asCompound()?.getOrNull()
}
