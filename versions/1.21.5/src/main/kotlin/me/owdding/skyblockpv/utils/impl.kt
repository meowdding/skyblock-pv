package me.owdding.skyblockpv.utils

import com.mojang.authlib.GameProfile
import earth.terrarium.olympus.client.pipelines.RoundedRectangle
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.player.RemotePlayer
import net.minecraft.client.renderer.entity.state.PlayerRenderState
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.player.PlayerModelPart
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.entity.SkullBlockEntity
import net.minecraft.world.phys.Vec3
import net.minecraft.world.scores.Scoreboard
import tech.thatgravyboat.skyblockapi.helpers.McClient
import java.util.*
import java.util.concurrent.CompletableFuture

fun GuiGraphics.drawRoundedRec(
    x: Int, y: Int, width: Int, height: Int,
    backgroundColor: Int, borderColor: Int = backgroundColor,
    borderSize: Int = 0, radius: Int = 0,
) {
    this.flush()

        RoundedRectangle.drawRelative(
            this@drawRoundedRec, x, y, width, height,
            backgroundColor, borderColor, width.coerceAtMost(height) * (radius / 100f), borderSize,
        )
}

internal fun fetchGameProfile(username: String): CompletableFuture<Optional<GameProfile>> = SkullBlockEntity.fetchGameProfile(username)
internal fun fetchGameProfile(uuid: UUID): CompletableFuture<Optional<GameProfile>> = SkullBlockEntity.fetchGameProfile(uuid)

class FakePlayer(gameProfile: GameProfile, val armor: List<ItemStack>, val customDisplayName: Component) : RemotePlayer(McClient.self.level!!, gameProfile) {

    init {
        equipment.set(EquipmentSlot.HEAD, armor[3])
        equipment.set(EquipmentSlot.CHEST, armor[2])
        equipment.set(EquipmentSlot.LEGS, armor[1])
        equipment.set(EquipmentSlot.FEET, armor[0])
    }

    fun setupRenderState(renderState: PlayerRenderState, partialTick: Float) {
        ContributorHandler.contributors[gameProfile.id]?.let {
            renderState.scoreText = it.title
            (renderState as PlayerRenderStateAccessor).`skyblockpv$scoreShader` = it.titleShader

            it.parrot?.let { parrot ->
                if (parrot.leftSide) {
                    renderState.parrotOnLeftShoulder = parrot.variant
                } else {
                    renderState.parrotOnRightShoulder = parrot.variant
                }
            }
            it.cat?.let { cat ->
                (renderState as PlayerRenderStateAccessor).`skyblockpv$catOnShoulder` = cat
            }
            renderState.isFullyFrozen = it.shaking
            renderState.ageInTicks = (Minecraft.getInstance().player?.tickCount?.toFloat() ?: 0.0f).plus(partialTick)
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
