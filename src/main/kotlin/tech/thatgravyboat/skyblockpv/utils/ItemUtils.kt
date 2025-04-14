package tech.thatgravyboat.skyblockpv.utils

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import it.unimi.dsi.fastutil.objects.ObjectSortedSets
import net.minecraft.core.Holder
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.Tag
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.ItemLore
import net.minecraft.world.item.component.ResolvableProfile
import net.minecraft.world.item.component.TooltipDisplay
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.italic
import tech.thatgravyboat.skyblockpv.dfu.LegacyDataFixer
import tech.thatgravyboat.skyblockpv.utils.displays.TooltipBuilder
import java.util.*

fun createSkull(textureBase64: String): ItemStack {
    val profile = GameProfile(UUID.randomUUID(), "a")
    profile.properties.put("textures", Property("textures", textureBase64))
    return createSkull(profile)
}

fun createSkull(profile: GameProfile): ItemStack {
    val stack = ItemStack(Items.PLAYER_HEAD)
    stack.set(DataComponents.PROFILE, ResolvableProfile(profile))
    return stack
}

val Item.holder: Holder<Item> get() = this.builtInRegistryHolder()

fun Tag.legacyStack() = LegacyDataFixer.fromTag(this.copy()) ?: Items.BARRIER.defaultInstance

fun JsonObject.itemStack(): ItemStack = this.getNbt().legacyStack()
fun JsonElement.itemStack(): ItemStack = this.getNbt().legacyStack()

fun ItemStack.withoutTooltip(): ItemStack = this.withTooltip()

fun ItemStack.withTooltip(init: TooltipBuilder.() -> Unit = {}): ItemStack {
    val builder = TooltipBuilder().apply(init).lines().filterIsInstance<Component>()
    when {
        builder.isEmpty() -> this.set(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay(true, ObjectSortedSets.emptySet()))
        builder.size == 1 -> this.set(DataComponents.CUSTOM_NAME, builder.first().copy().apply {
            this.italic = false
        })
        else -> {
            this.set(DataComponents.CUSTOM_NAME, builder.first().copy().apply {
                this.italic = false
            })
            val lore = builder.drop(1)
            this.set(DataComponents.LORE, ItemLore(lore, lore))
        }
    }
    return this
}
