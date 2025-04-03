package tech.thatgravyboat.skyblockpv.dfu.fixes.display

import net.minecraft.core.component.DataComponentType
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockpv.dfu.DataComponentFixer
import kotlin.jvm.optionals.getOrNull

object NameFixer : DataComponentFixer<Component> {
    private const val DISPLAY_TAG = "display"
    private const val TAG = "Name"

    override fun getComponentType(): DataComponentType<Component> = DataComponents.CUSTOM_NAME

    override fun getData(compoundTag: CompoundTag): Component? {
        val display = compoundTag.getCompound(DISPLAY_TAG).getOrNull() ?: return null
        val name = display.getAndRemoveString(TAG) ?: return null
        compoundTag.removeIfEmpty(DISPLAY_TAG)

        return Text.of(name)
    }
}
