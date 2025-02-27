package tech.thatgravyboat.skyblockpv.utils

import com.mojang.authlib.GameProfile
import net.minecraft.client.Minecraft
import net.minecraft.client.player.RemotePlayer
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.world.entity.player.PlayerModelPart
import net.minecraft.world.scores.PlayerTeam
import net.minecraft.world.scores.Scoreboard
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.text.Text

class FakePlayer(gameProfile: GameProfile) : RemotePlayer(McClient.self.level, gameProfile) {
    override fun isSpectator() = false
    override fun isCreative() = false

    override fun getSkin() = Minecraft.getInstance().skinManager.lookupInsecure(gameProfile).get()

    override fun getScoreboard() = object : Scoreboard(){}

    override fun getDisplayName(): Component? = Text.join(gameProfile.name, " is gay, ew")

    override fun isModelPartShown(part: PlayerModelPart) = part != PlayerModelPart.CAPE
}
