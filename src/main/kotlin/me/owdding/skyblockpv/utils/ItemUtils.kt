package me.owdding.skyblockpv.utils

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import me.owdding.dfu.item.LegacyDataFixer
import net.minecraft.nbt.Tag
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

fun Tag.legacyStack(): ItemStack = LegacyDataFixer.fromTag(this.copy()) ?: Items.BARRIER.defaultInstance

fun JsonObject.itemStack(): ItemStack = this.getNbt().legacyStack()
fun JsonElement.itemStack(): ItemStack = this.getNbt().legacyStack()
