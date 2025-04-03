package tech.thatgravyboat.skyblockpv.dfu.fixes

import it.unimi.dsi.fastutil.bytes.Byte2ObjectOpenHashMap
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.component.TooltipDisplay
import tech.thatgravyboat.skyblockpv.dfu.DataComponentFixer

object HideFlagsFixer : DataComponentFixer<TooltipDisplay> {
    private val cache: Byte2ObjectOpenHashMap<TooltipDisplay> = Byte2ObjectOpenHashMap()

    private const val TAG = "HideFlags"

    private const val ZERO: UByte = 0u
    private const val HIDE_ENCHANTMENTS_FLAG: UByte = 1u
    private const val HIDE_MODIFIERS_FLAG: UByte = 2u
    private const val HIDE_UNBREAKABLE_FLAG: UByte = 4u
    private const val HIDE_CAN_DESTROY_FLAG: UByte = 8u
    private const val HIDE_CAN_PLACE_FLAG: UByte = 16u
    private const val HIDE_ADDITIONAL_FLAG: UByte = 32u
    private const val HIDE_DYED_FLAG: UByte = 64u
    private const val HIDE_UPGRADE_FLAG: UByte = 128u

    override fun getComponentType(): DataComponentType<TooltipDisplay> = DataComponents.TOOLTIP_DISPLAY
    override fun getData(compoundTag: CompoundTag): TooltipDisplay? {
        val hideFlags = compoundTag.getAndRemoveByte(TAG)?.toUByte()?: return null

        compoundTag.remove(TAG)
        if (cache.containsKey(hideFlags.toByte())) {
            return cache[hideFlags.toByte()]
        }

        val tooltipDisplay = buildList<DataComponentType<*>> {
            if (hideFlags.and(HIDE_ENCHANTMENTS_FLAG) != ZERO) {
                add(DataComponents.ENCHANTMENTS)
            }
            if (hideFlags.and(HIDE_MODIFIERS_FLAG) != ZERO) {
                add(DataComponents.ATTRIBUTE_MODIFIERS)
            }
            if (hideFlags.and(HIDE_UNBREAKABLE_FLAG) != ZERO) {
                add(DataComponents.UNBREAKABLE)
            }
            if (hideFlags.and(HIDE_CAN_DESTROY_FLAG) != ZERO) {
                add(DataComponents.CAN_BREAK)
            }
            if (hideFlags.and(HIDE_CAN_PLACE_FLAG) != ZERO) {
                add(DataComponents.CAN_PLACE_ON)
            }
            if (hideFlags.and(HIDE_ADDITIONAL_FLAG) != ZERO) {
                addAll(
                    listOf(
                        DataComponents.TROPICAL_FISH_PATTERN,
                        DataComponents.INSTRUMENT,
                        DataComponents.MAP_ID,
                        DataComponents.BEES,
                        DataComponents.CONTAINER_LOOT,
                        DataComponents.CONTAINER,
                        DataComponents.BANNER_PATTERNS,
                        DataComponents.POT_DECORATIONS,
                        DataComponents.WRITTEN_BOOK_CONTENT,
                        DataComponents.CHARGED_PROJECTILES,
                        DataComponents.FIREWORKS,
                        DataComponents.FIREWORK_EXPLOSION,
                        DataComponents.POTION_CONTENTS,
                        DataComponents.JUKEBOX_PLAYABLE,
                        DataComponents.STORED_ENCHANTMENTS,
                        DataComponents.OMINOUS_BOTTLE_AMPLIFIER,
                        DataComponents.SUSPICIOUS_STEW_EFFECTS,
                        DataComponents.BLOCK_STATE,
                    ),
                )
            }
            if (hideFlags.and(HIDE_DYED_FLAG) != ZERO) {
                add(DataComponents.DYED_COLOR)
            }
            if (hideFlags.and(HIDE_UPGRADE_FLAG) != ZERO) {
                add(DataComponents.TRIM)
            }
        }.let { TooltipDisplay(false, LinkedHashSet(it)) }

        cache[hideFlags.toByte()] = tooltipDisplay
        return tooltipDisplay
    }


}
