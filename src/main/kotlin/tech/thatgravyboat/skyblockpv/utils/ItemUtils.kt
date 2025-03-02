package tech.thatgravyboat.skyblockpv.utils

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.mojang.serialization.Dynamic
import net.azureaaron.legacyitemdfu.LegacyItemStackFixer
import net.azureaaron.legacyitemdfu.TypeReferences
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.Tag
import net.minecraft.resources.RegistryOps
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.helpers.McClient
import kotlin.io.encoding.ExperimentalEncodingApi

fun Tag.legacyStack(): ItemStack {
    val ops: RegistryOps<Tag>? = McClient.self.connection?.registryAccess()?.createSerializationContext(NbtOps.INSTANCE)
    if (ops == null) return ItemStack.EMPTY
    val fixed: Dynamic<Tag> = LegacyItemStackFixer.getFixer().update(
        TypeReferences.LEGACY_ITEM_STACK, Dynamic<Tag>(ops, this),
        LegacyItemStackFixer.getFirstVersion(),
        LegacyItemStackFixer.getLatestVersion()
    )
    val stack = ItemStack.CODEC.parse<Tag>(fixed)
        .setPartial(ItemStack.EMPTY)
        .resultOrPartial()
        .get()

    return stack
}

@OptIn(ExperimentalEncodingApi::class)
fun JsonObject.itemStack(): ItemStack {
    return this.getNbt().legacyStack()
}

fun JsonElement.itemStack(): ItemStack {
    return this.getNbt().legacyStack()
}
