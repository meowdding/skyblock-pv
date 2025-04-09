package tech.thatgravyboat.skyblockpv.utils

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import net.minecraft.core.Holder
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.Tag
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.ResolvableProfile
import tech.thatgravyboat.skyblockpv.dfu.LegacyDataFixer
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
