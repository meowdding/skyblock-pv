package me.owdding.skyblockpv.utils

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtAccounter
import net.minecraft.nbt.NbtIo
import tech.thatgravyboat.skyblockapi.utils.json.Json.toJson
import java.io.ByteArrayInputStream
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

fun JsonObject.getNbt(): CompoundTag = this.asString.getNbt()
fun JsonElement.getNbt(): CompoundTag = this.asString.getNbt()

@OptIn(ExperimentalEncodingApi::class)
fun String.getNbt(): CompoundTag {
    return NbtIo.readCompressed(ByteArrayInputStream(Base64.decode(this)), NbtAccounter.unlimitedHeap())
}

fun JsonObject.getNbtJson(): JsonObject? {
    return this.getNbt().toJson(CompoundTag.CODEC)?.asJsonObject
}

fun JsonElement.getNbtJson(): JsonObject? {
    return this.getNbt().toJson(CompoundTag.CODEC)?.asJsonObject
}
