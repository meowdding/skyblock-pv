package me.owdding.skyblockpv.utils

import com.google.gson.JsonElement
import me.owdding.skyblockpv.dfu.LegacyDataFixer
import net.minecraft.nbt.Tag
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

fun Tag.legacyStack() = LegacyDataFixer.fromTag(this.copy()) ?: Items.BARRIER.defaultInstance
fun JsonElement.apiItemStacks(): List<ItemStack> = this.getNbt().getListOrEmpty("i").map(Tag::legacyStack)
