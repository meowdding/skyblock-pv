package tech.thatgravyboat.skyblockpv.dfu.fixes.display

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.component.DyedItemColor
import tech.thatgravyboat.skyblockpv.dfu.DataComponentFixer
import kotlin.jvm.optionals.getOrNull

object ColorFixer: DataComponentFixer<DyedItemColor> {

    private val cache = Int2ObjectOpenHashMap<DyedItemColor>()

    private const val DISPLAY_TAG = "display"
    private const val TAG = "color"

    override fun getComponentType(): DataComponentType<DyedItemColor> = DataComponents.DYED_COLOR

    override fun getData(compoundTag: CompoundTag): DyedItemColor? {
        val display = compoundTag.getCompound(DISPLAY_TAG).getOrNull() ?: return null
        val color = display.getAndRemoveInt(TAG) ?: return null
        compoundTag.removeIfEmpty(DISPLAY_TAG)

        if (cache.containsKey(color)) {
            return cache[color]
        }

        val dyedItemColor = DyedItemColor(color)
        cache[color] = dyedItemColor
        return dyedItemColor
    }
}
