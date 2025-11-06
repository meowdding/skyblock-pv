package me.owdding.skyblockpv.utils.render

import com.mojang.blaze3d.vertex.PoseStack
import me.owdding.lib.cosmetics.CosmeticManager
import me.owdding.skyblockpv.utils.PlayerRenderStateAccessor
import me.owdding.skyblockpv.utils.codecs.clientAssetConverter
import net.minecraft.client.model.CatModel
import net.minecraft.client.model.PlayerModel
import net.minecraft.client.model.geom.EntityModelSet
import net.minecraft.client.model.geom.ModelLayers
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.entity.RenderLayerParent
import net.minecraft.client.renderer.entity.layers.RenderLayer
import net.minecraft.client.renderer.entity.state.AvatarRenderState
import net.minecraft.client.renderer.entity.state.CatRenderState
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.core.ClientAsset
import net.minecraft.resources.ResourceLocation

class CatOnShoulderLayer(renderer: RenderLayerParent<AvatarRenderState, PlayerModel>, modelSet: EntityModelSet) :
    RenderLayer<AvatarRenderState, PlayerModel>(renderer) {
    private val model = CatModel(modelSet.bakeLayer(ModelLayers.CAT))

    override fun submit(
        poseStack: PoseStack,
        collector: SubmitNodeCollector,
        packedLight: Int,
        renderState: AvatarRenderState,
        yRot: Float,
        xRot: Float,
    ) {
        (renderState as PlayerRenderStateAccessor).`skyblockpv$catOnShoulder`?.let {
            val catResource = CosmeticManager.imageProvider.get(it)
            if (catResource.equals(MissingTextureAtlasSprite.getLocation())) return
            val asset = clientAssetConverter()(catResource)
            val leftShoulder = renderState.parrotOnLeftShoulder == null
            submitOnShoulder(poseStack, collector, packedLight, renderState, (asset as ClientAsset.Texture).id(), yRot, xRot, leftShoulder)
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
        val catRenderState: CatRenderState = CatRenderState()
        catRenderState.isSitting = true
        catRenderState.ageInTicks = renderState.ageInTicks
        catRenderState.walkAnimationPos = renderState.walkAnimationPos
        catRenderState.walkAnimationSpeed = renderState.walkAnimationSpeed
        catRenderState.yRot = yRot
        catRenderState.xRot = xRot
        collector.submitModel(this.model, catRenderState, poseStack, this.model.renderType(variant), packedLight, OverlayTexture.NO_OVERLAY, 0, null)
        poseStack.popPose()
    }
}
