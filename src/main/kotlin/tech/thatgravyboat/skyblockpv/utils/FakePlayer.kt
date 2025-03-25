package tech.thatgravyboat.skyblockpv.utils

import com.mojang.authlib.GameProfile
import net.minecraft.client.Minecraft
import net.minecraft.client.player.RemotePlayer
import net.minecraft.client.renderer.entity.state.PlayerRenderState
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.PlayerModelPart
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.Vec3
import net.minecraft.world.scores.Scoreboard
import tech.thatgravyboat.skyblockapi.helpers.McClient

class FakePlayer(gameProfile: GameProfile, val customDisplayName: Component, val armor: List<ItemStack>) : RemotePlayer(McClient.self.level, gameProfile) {
    init {
        for (i in 0 until 4) {
            inventory.armor[i] = armor[i]
        }
    }

    fun setupRenderState(renderState: PlayerRenderState, partialTick: Float) {
        ContributorHandler.contributors[gameProfile.id]?.let {
            renderState.scoreText = it.title?.let { Component.literal(it) }
            renderState.parrotOnLeftShoulder = it.parrotLeft
            renderState.parrotOnRightShoulder = it.parrotRight
            renderState.isFullyFrozen = it.shaking


            renderState.ageInTicks = (Minecraft.getInstance().player?.tickCount?.toFloat()?: 0.0f).plus(partialTick)
        }
    }

    override fun isSpectator() = false
    override fun isCreative() = false

    override fun getSkin() = Minecraft.getInstance().skinManager.lookupInsecure(gameProfile).get()

    override fun getScoreboard() = object : Scoreboard() {}

    override fun getDisplayName(): Component? = customDisplayName

    override fun isModelPartShown(part: PlayerModelPart) = part != PlayerModelPart.CAPE

    override fun position(): Vec3? = Minecraft.getInstance().cameraEntity?.position()
}
