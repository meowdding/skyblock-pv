package me.owdding.skyblockpv.dfu.fixes

import it.unimi.dsi.fastutil.bytes.Byte2ObjectOpenHashMap
import me.owdding.skyblockpv.dfu.DataComponentFixer
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.component.TooltipDisplay

object HideFlagsFixer : DataComponentFixer<TooltipDisplay> {
    private val cache: Byte2ObjectOpenHashMap<TooltipDisplay> = Byte2ObjectOpenHashMap()

    private const val TAG = "HideFlags"

    private const val ZERO: UByte = 0u
    private const val HIDE_ENCHANTMENTS_FLAG: UByte = 1u
    private const val HIDE_ATTRIBUTES_FLAG: UByte = 2u
    private const val HIDE_UNBREAKABLE_FLAG: UByte = 4u
    private const val HIDE_CAN_DESTROY_FLAG: UByte = 8u
    private const val HIDE_CAN_PLACE_FLAG: UByte = 16u
    private const val HIDE_ADDITIONAL_FLAG: UByte = 32u
    private const val HIDE_DYED_FLAG: UByte = 64u
    private const val HIDE_UPGRADE_FLAG: UByte = 128u

    private val additionalTooltipComponents = setOf(
        DataComponents.BANNER_PATTERNS,
        DataComponents.BEES,
        DataComponents.BLOCK_ENTITY_DATA,
        DataComponents.BLOCK_STATE,
        DataComponents.BUNDLE_CONTENTS,
        DataComponents.CHARGED_PROJECTILES,
        DataComponents.CONTAINER,
        DataComponents.CONTAINER_LOOT,
        DataComponents.FIREWORK_EXPLOSION,
        DataComponents.FIREWORKS,
        DataComponents.INSTRUMENT,
        DataComponents.MAP_ID,
        DataComponents.PAINTING_VARIANT,
        DataComponents.POT_DECORATIONS,
        DataComponents.POTION_CONTENTS,
        DataComponents.TROPICAL_FISH_PATTERN,
        DataComponents.WRITTEN_BOOK_CONTENT,
        DataComponents.STORED_ENCHANTMENTS,
    )

    override val type: DataComponentType<TooltipDisplay> = DataComponents.TOOLTIP_DISPLAY

    override fun getData(tag: CompoundTag): TooltipDisplay? {
        val hideFlags = tag.getAndRemoveByte(TAG)?.toUByte() ?: return null

        return cache.getOrPut(hideFlags.toByte()) {
            val components = LinkedHashSet<DataComponentType<*>>()
            if (hideFlags.and(HIDE_ENCHANTMENTS_FLAG) != ZERO) {
                components.add(DataComponents.ENCHANTMENTS)
            }
            if (hideFlags.and(HIDE_ATTRIBUTES_FLAG) != ZERO) {
                components.add(DataComponents.ATTRIBUTE_MODIFIERS)
            }
            if (hideFlags.and(HIDE_UNBREAKABLE_FLAG) != ZERO) {
                components.add(DataComponents.UNBREAKABLE)
            }
            if (hideFlags.and(HIDE_CAN_DESTROY_FLAG) != ZERO) {
                components.add(DataComponents.CAN_BREAK)
            }
            if (hideFlags.and(HIDE_CAN_PLACE_FLAG) != ZERO) {
                components.add(DataComponents.CAN_PLACE_ON)
            }
            if (hideFlags.and(HIDE_ADDITIONAL_FLAG) != ZERO) {
                components.addAll(additionalTooltipComponents)
            }
            if (hideFlags.and(HIDE_DYED_FLAG) != ZERO) {
                components.add(DataComponents.DYED_COLOR)
            }
            if (hideFlags.and(HIDE_UPGRADE_FLAG) != ZERO) {
                components.add(DataComponents.TRIM)
            }

            TooltipDisplay(false, components)
        }
    }


}
