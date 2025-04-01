package tech.thatgravyboat.skyblockpv.data

import net.minecraft.nbt.Tag
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockpv.utils.legacyStack

interface LegacyItemStack {
    val hasFinishedLoading: Boolean
    suspend fun load()
    val itemStack: ItemStack

    companion object {
        fun fromTag(item: Tag): LegacyItemStack {
            return LazyLegacyItemStack(item)
        }

        fun wrap(defaultInstance: ItemStack): LegacyItemStack = WrappedLegacyItemStack(defaultInstance)
    }
}

class LazyLegacyItemStack(val tag: Tag) : LegacyItemStack {
    private var stack: ItemStack = Items.BARRIER.defaultInstance
    private var done: Boolean = false

    override val hasFinishedLoading: Boolean get() = done

    override suspend fun load() {
        if (done) return
        stack = tag.legacyStack()
        done = true
    }

    override val itemStack: ItemStack
        get() = stack
}

class WrappedLegacyItemStack(val stack: ItemStack) : LegacyItemStack {
    override val hasFinishedLoading: Boolean
        get() = true

    override suspend fun load() {}

    override val itemStack: ItemStack
        get() = stack
}
