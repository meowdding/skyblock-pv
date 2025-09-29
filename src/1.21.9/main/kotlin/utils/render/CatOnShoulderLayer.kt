package me.owdding.skyblockpv.utils.render

import com.mojang.blaze3d.vertex.PoseStack
import me.owdding.skyblockpv.utils.PlayerRenderStateAccessor
import net.minecraft.client.model.CatModel
import net.minecraft.client.model.PlayerModel
import net.minecraft.client.model.geom.EntityModelSet
import net.minecraft.client.model.geom.ModelLayers
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.entity.RenderLayerParent
import net.minecraft.client.renderer.entity.layers.RenderLayer
import net.minecraft.client.renderer.entity.state.AvatarRenderState
import net.minecraft.client.renderer.entity.state.CatRenderState
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.resources.ResourceLocation

class CatOnShoulderLayer(renderer: RenderLayerParent<AvatarRenderState, PlayerModel>, modelSet: EntityModelSet) :
    RenderLayer<AvatarRenderState, PlayerModel>(renderer) {
    private val catRenderState: CatRenderState = CatRenderState()
    private val model = CatModel(modelSet.bakeLayer(ModelLayers.CAT))

    init {
        this.catRenderState.isSitting = true
    }

    override fun submit(
        poseStack: PoseStack,
        collector: SubmitNodeCollector,
        packedLight: Int,
        renderState: AvatarRenderState?,
        yRot: Float,
        xRot: Float,
    ) {
        (renderState as PlayerRenderStateAccessor).`skyblockpv$catOnShoulder`?.let {
            submitOnShoulder(poseStack, collector, packedLight, renderState, it.asset.id(), yRot, xRot, it.leftSide)
        }
    }

    private fun submitOnShoulder(
        poseStack: PoseStack,
        collector: SubmitNodeCollector,
        packedLight: Int,
        renderState: AvatarRenderState,
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
        collector.submitModel(this.model, this.catRenderState, poseStack, this.model.renderType(variant), packedLight, OverlayTexture.NO_OVERLAY, -1, null)
        poseStack.popPose()
    }
}
