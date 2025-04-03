package tech.thatgravyboat.skyblockpv.dfu

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.misc.RegisterCommandsEvent
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.Text.send
import tech.thatgravyboat.skyblockpv.dfu.fixes.*
import tech.thatgravyboat.skyblockpv.dfu.fixes.display.ColorFixer
import tech.thatgravyboat.skyblockpv.dfu.fixes.display.LoreFixer
import tech.thatgravyboat.skyblockpv.dfu.fixes.display.NameFixer

object LegacyDataFixer {

    val fixers = listOf(
        ItemCountFixer,
        HideFlagsFixer,
        SkullTextureFixer,
        LoreFixer,
        NameFixer,
        ColorFixer,
        UnbreakableFixer,
        EnchantGlintFixer,
        WrittenBookFixer,
    )

    val balls = mutableSetOf<String>()

    fun fromTag(tag: Tag): ItemStack? {
        if (tag !is CompoundTag) {
            return ItemStack.EMPTY
        }

        if (tag.isEmpty) return null

        val item: Item
        try {
            item = FlatteningFixer.getItem(tag)
        } catch (e: NullPointerException) {
            return ItemStack.EMPTY
        }

        val defaultInstance = item.defaultInstance
        fixers.forEach {
            if (it.shouldApply(defaultInstance)) {
                it.fixItem(defaultInstance, tag)
            }
        }

        tag.getCompoundOrEmpty("tag").get("BlockEntityTag")?.let {
            println("meow")
        }

        tag.entrySet().distinctBy { it.key }.forEach {
            balls.add(it.key)
        }
        tag.getCompoundOrEmpty("tag").getCompoundOrEmpty("display").entrySet().distinctBy { it.key }.forEach {
            balls.add(it.key)
        }
        tag.getCompoundOrEmpty("tag").entrySet().distinctBy { it.key }.forEach {
            balls.add(it.key)
        }


        return ItemStack.EMPTY
    }

    @Subscription
    fun command(event: RegisterCommandsEvent) {
        event.register("meow") {
            callback {
                balls.forEach {
                    println(it)
                    Text.of(it).send()
                }
            }
        }
    }

}
