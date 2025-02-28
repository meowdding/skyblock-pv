package tech.thatgravyboat.skyblockpv.utils

import com.mojang.authlib.GameProfile
import net.minecraft.client.Minecraft
import net.minecraft.client.player.RemotePlayer
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.PlayerModelPart
import net.minecraft.world.scores.Scoreboard
import tech.thatgravyboat.skyblockapi.helpers.McClient

class FakePlayer(gameProfile: GameProfile, val customDisplayName: Component) : RemotePlayer(McClient.self.level, gameProfile) {
    override fun isSpectator() = false
    override fun isCreative() = false

    override fun getSkin() = Minecraft.getInstance().skinManager.lookupInsecure(gameProfile).get()

    override fun getScoreboard() = object : Scoreboard() {}

    override fun getDisplayName(): Component? = customDisplayName

    override fun isModelPartShown(part: PlayerModelPart) = part != PlayerModelPart.CAPE
}
