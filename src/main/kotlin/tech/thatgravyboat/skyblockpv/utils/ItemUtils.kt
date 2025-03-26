package tech.thatgravyboat.skyblockpv.utils

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import com.mojang.serialization.Dynamic
import net.azureaaron.legacyitemdfu.LegacyItemStackFixer
import net.azureaaron.legacyitemdfu.TypeReferences
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.Tag
import net.minecraft.network.chat.Component
import net.minecraft.resources.RegistryOps
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.ItemAttributeModifiers
import net.minecraft.world.item.component.ResolvableProfile
import net.minecraft.world.item.component.TooltipDisplay
import tech.thatgravyboat.skyblockapi.helpers.McClient
import java.util.*


val tooltipDataComponents = LinkedHashSet<DataComponentType<*>>(
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
        DataComponents.TRIM,
        DataComponents.STORED_ENCHANTMENTS,
        DataComponents.ENCHANTMENTS,
        DataComponents.DYED_COLOR,
        DataComponents.OMINOUS_BOTTLE_AMPLIFIER,
        DataComponents.SUSPICIOUS_STEW_EFFECTS,
        DataComponents.BLOCK_STATE,
        DataComponents.ATTRIBUTE_MODIFIERS,
        DataComponents.UNBREAKABLE
    ),
)

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

fun ItemStack.getLore(): List<Component> = this[DataComponents.LORE]?.lines ?: emptyList()

fun Tag.legacyStack(): ItemStack {
    val ops: RegistryOps<Tag> = McClient.self.connection?.registryAccess()?.createSerializationContext(NbtOps.INSTANCE) ?: return ItemStack.EMPTY
    val fixed: Dynamic<Tag> = LegacyItemStackFixer.getFixer().update(
        TypeReferences.LEGACY_ITEM_STACK, Dynamic(ops, this),
        LegacyItemStackFixer.getFirstVersion(),
        LegacyItemStackFixer.getLatestVersion(),
    ).map {
        // move data out of extra attributes so it's equal to the modern hypixel format
        if (it is CompoundTag) {
            val customData = it.getCompoundOrEmpty("components")?.getCompoundOrEmpty("minecraft:custom_data")
            val extraAttributes = customData?.getCompoundOrEmpty("ExtraAttributes")
            extraAttributes?.keySet()?.associate { it to extraAttributes.get(it) }?.filterValues { it != null }?.forEach { (key, tag) ->
                customData.put(key, tag!!) // can't be null because of previous filter
            }
        }
        return@map it
    }
    val stack = ItemStack.CODEC.parse(fixed).setPartial(ItemStack.EMPTY).resultOrPartial().get()

    stack.set(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay(false, tooltipDataComponents))
    stack.set(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers(listOf()))

    return stack
}

fun JsonObject.itemStack(): ItemStack {
    return this.getNbt().legacyStack()
}

fun JsonElement.itemStack(): ItemStack {
    return this.getNbt().legacyStack()
}
