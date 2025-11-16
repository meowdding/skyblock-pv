package me.owdding.skyblockpv.utils

import com.mojang.authlib.GameProfile
import me.owdding.lib.rendering.text.TextShader
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import java.net.URI

interface PlayerRenderStateAccessor {
    var `skyblockpv$catOnShoulder`: URI?
    var `skyblockpv$nameShader`: TextShader?
    var `skyblockpv$scoreShader`: TextShader?
}

fun FakePlayer(gameProfile: GameProfile, customDisplayName: Component, armor: List<ItemStack> = emptyList()): LivingEntity =
    FakePlayer(gameProfile, armor, customDisplayName)
