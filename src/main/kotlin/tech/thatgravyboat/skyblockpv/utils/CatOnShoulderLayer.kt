package tech.thatgravyboat.skyblockpv.utils

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.model.CatModel
import net.minecraft.client.model.PlayerModel
import net.minecraft.client.model.geom.EntityModelSet
import net.minecraft.client.model.geom.ModelLayers
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.RenderLayerParent
import net.minecraft.client.renderer.entity.layers.RenderLayer
import net.minecraft.client.renderer.entity.state.CatRenderState
import net.minecraft.client.renderer.entity.state.PlayerRenderState
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.resources.ResourceLocation

class CatOnShoulderLayer(renderer: RenderLayerParent<PlayerRenderState, PlayerModel>, modelSet: EntityModelSet) :
    RenderLayer<PlayerRenderState, PlayerModel>(renderer) {
    private val catRenderState: CatRenderState = CatRenderState()
    private val model = CatModel(modelSet.bakeLayer(ModelLayers.CAT))

    init {
        this.catRenderState.isSitting = true
    }

    override fun render(poseStack: PoseStack, bufferSource: MultiBufferSource, packedLight: Int, renderState: PlayerRenderState, yRot: Float, xRot: Float) {
        (renderState as PlayerRenderStateAccessor).`skyblockpv$catOnShoulder`?.let {
            renderOnShoulder(poseStack, bufferSource, packedLight, renderState, it.type.texture, yRot, xRot, it.leftSide)
        }
    }

    private fun renderOnShoulder(
        poseStack: PoseStack,
        buffer: MultiBufferSource,
        packedLight: Int,
        renderState: PlayerRenderState,
        variant: ResourceLocation,
        yRot: Float,
        xRot: Float,
        leftShoulder: Boolean,
    ) {
        poseStack.pushPose()
        poseStack.translate(if (leftShoulder) 0.4f else -0.4f, if (renderState.isCrouching) -1.3f else -1.5f, 0.2f)
        this.catRenderState.ageInTicks = renderState.ageInTicks
        this.catRenderState.walkAnimationPos = renderState.walkAnimationPos
        this.catRenderState.walkAnimationSpeed = renderState.walkAnimationSpeed
        this.catRenderState.yRot = yRot
        this.catRenderState.xRot = xRot
        this.model.setupAnim(this.catRenderState)
        this.model.renderToBuffer(
            poseStack,
            buffer.getBuffer(this.model.renderType(variant)),
            packedLight,
            OverlayTexture.NO_OVERLAY,
        )
        poseStack.popPose()
    }
}
