package me.owdding.skyblockpv.dfu.fixes

import it.unimi.dsi.fastutil.ints.IntArrayList
import it.unimi.dsi.fastutil.ints.IntList
import it.unimi.dsi.fastutil.ints.IntLists
import me.owdding.skyblockpv.dfu.DataComponentFixer
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.component.FireworkExplosion

object FireworkExplosionFixer : DataComponentFixer<FireworkExplosion> {
    override val type: DataComponentType<FireworkExplosion> = DataComponents.FIREWORK_EXPLOSION

    override fun getData(tag: CompoundTag): FireworkExplosion? {
        val compoundTag = tag.getAndRemoveCompound("Explosion") ?: return null
        val explosionShape = getExplosionById(compoundTag.getAndRemoveInt("Type") ?: 0)
        val colors = compoundTag.getAndRemoveIntArray("Colors").toIntList()
        val fadeColors = compoundTag.getAndRemoveIntArray("FadeColors").toIntList()
        val hasTrail = compoundTag.getAndRemoveBoolean("Trail") ?: false
        val hasTwinkle = compoundTag.getAndRemoveBoolean("Flicker") ?: false

        return FireworkExplosion(explosionShape, colors, fadeColors, hasTrail, hasTwinkle)
    }

    private fun IntArray?.toIntList(): IntList {
        return this?.let { IntArrayList(it) } ?: IntLists.EMPTY_LIST
    }

    private fun getExplosionById(id: Int): FireworkExplosion.Shape = when (id) {
        1 -> FireworkExplosion.Shape.LARGE_BALL
        2 -> FireworkExplosion.Shape.STAR
        3 -> FireworkExplosion.Shape.CREEPER
        4 -> FireworkExplosion.Shape.BURST
        else -> FireworkExplosion.Shape.SMALL_BALL
    }
}
