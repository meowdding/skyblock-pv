package me.owdding.skyblockpv.utils

import com.mojang.authlib.GameProfile
import earth.terrarium.olympus.client.pipelines.RoundedRectangle
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.ClientMannequin
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.entity.state.AvatarRenderState
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.PlayerModelPart
import net.minecraft.world.entity.player.PlayerSkin
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.ResolvableProfile
import net.minecraft.world.phys.Vec3
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.platform.pushPop
import java.util.*
import java.util.concurrent.CompletableFuture

fun GuiGraphics.drawRoundedRec(
    x: Int, y: Int, width: Int, height: Int,
    backgroundColor: Int, borderColor: Int = backgroundColor,
    borderSize: Int = 0, radius: Int = 0,
) {
    pushPop {
        RoundedRectangle.draw(
            this@drawRoundedRec, x, y, width, height,
            backgroundColor, borderColor, width.coerceAtMost(height) * (radius / 100f), borderSize,
        )
    }
}

internal fun fetchGameProfile(username: String): CompletableFuture<Optional<GameProfile>> =
    CompletableFuture.supplyAsync { McClient.self.services().profileResolver.fetchByName(username) }

class FakePlayer(val gameProfile: GameProfile, val armor: List<ItemStack>, val customDisplayName: Component) :
    ClientMannequin(McClient.self.level!!, McClient.self.playerSkinRenderCache()) {
    private val _profile = ResolvableProfile.createResolved(gameProfile).apply {
        this.resolveProfile(McClient.self.services().profileResolver)
    }
    private var _skin: PlayerSkin = DEFAULT_SKIN

    init {
        equipment.set(EquipmentSlot.HEAD, armor[3])
        equipment.set(EquipmentSlot.CHEST, armor[2])
        equipment.set(EquipmentSlot.LEGS, armor[1])
        equipment.set(EquipmentSlot.FEET, armor[0])
        McClient.self.playerSkinRenderCache().lookup(_profile).whenComplete { skin, _ ->
            skin.ifPresent { skin ->
                _skin = skin.playerSkin()
            }
        }
    }

    override fun shouldShowName() = true

    fun setupRenderState(renderState: AvatarRenderState, partialTick: Float) {
        renderState.nameTag = customDisplayName
        renderState.scoreText = null
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

    override fun getProfile(): ResolvableProfile = _profile
    override fun getDisplayName(): Component? = customDisplayName
    override fun getSkin() = _skin

    override fun isModelPartShown(part: PlayerModelPart) = part != PlayerModelPart.CAPE

    override fun position(): Vec3? = Minecraft.getInstance().cameraEntity?.position()

}
