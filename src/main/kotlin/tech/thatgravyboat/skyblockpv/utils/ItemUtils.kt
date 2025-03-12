package tech.thatgravyboat.skyblockpv.utils

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import com.mojang.serialization.Dynamic
import net.azureaaron.legacyitemdfu.LegacyItemStackFixer
import net.azureaaron.legacyitemdfu.TypeReferences
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.Tag
import net.minecraft.network.chat.Component
import net.minecraft.resources.RegistryOps
import net.minecraft.util.Unit
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.ItemAttributeModifiers
import net.minecraft.world.item.component.ResolvableProfile
import tech.thatgravyboat.skyblockapi.helpers.McClient
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

fun ItemStack.getLore(): List<Component> = this[DataComponents.LORE]?.lines ?: emptyList()

fun Tag.legacyStack(): ItemStack {
    val ops: RegistryOps<Tag>? = McClient.self.connection?.registryAccess()?.createSerializationContext(NbtOps.INSTANCE)
    if (ops == null) return ItemStack.EMPTY
    val fixed: Dynamic<Tag> = LegacyItemStackFixer.getFixer().update(
        TypeReferences.LEGACY_ITEM_STACK, Dynamic<Tag>(ops, this),
        LegacyItemStackFixer.getFirstVersion(),
        LegacyItemStackFixer.getLatestVersion(),
    ).map {
        // move data out of extra attributes so it's equal to the modern hypixel format
        if (it is CompoundTag) {
            val customData = it.getCompound("components").getCompound("minecraft:custom_data")
            val extraAttributes = customData?.getCompound("ExtraAttributes")
            extraAttributes?.allKeys?.associate { it to extraAttributes.get(it) }?.filterValues { it != null }?.forEach { (key, tag) ->
                customData.put(key, tag!!) // can't be null because of previous filter
                if (key == "enchantments") {
                    // TODO remove when skyblockApi #55 is merged
                    customData.put("enchantment", tag)
                }
            }
        }
        return@map it
    }
    val stack = ItemStack.CODEC.parse<Tag>(fixed)
        .setPartial(ItemStack.EMPTY)
        .resultOrPartial()
        .get()

    stack.set(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE)
    stack.set(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers(listOf(), false))

    return stack
}

fun JsonObject.itemStack(): ItemStack {
    return this.getNbt().legacyStack()
}

fun JsonElement.itemStack(): ItemStack {
    return this.getNbt().legacyStack()
}
