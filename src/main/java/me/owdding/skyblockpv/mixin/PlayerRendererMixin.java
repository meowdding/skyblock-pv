package me.owdding.skyblockpv.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import kotlin.Unit;
import me.owdding.skyblockpv.utils.FakePlayer;
import me.owdding.skyblockpv.utils.PlayerRenderStateAccessor;
import me.owdding.skyblockpv.utils.render.CatOnShoulderLayer;
import me.owdding.skyblockpv.utils.render.RenderUtils;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin extends LivingEntityRenderer<AbstractClientPlayer, PlayerRenderState, PlayerModel> {

    public PlayerRendererMixin(EntityRendererProvider.Context context, PlayerModel model, float shadowRadius) {
        super(context, model, shadowRadius);
    }

    @Inject(method = "extractRenderState(Lnet/minecraft/client/player/AbstractClientPlayer;Lnet/minecraft/client/renderer/entity/state/PlayerRenderState;F)V", at = @At("TAIL"))
    public void extractRenderState(
        CallbackInfo ci,
        @Local(argsOnly = true) AbstractClientPlayer player,
        @Local(argsOnly = true) PlayerRenderState renderState,
        @Local(argsOnly = true) float partial
    ) {
        ((PlayerRenderStateAccessor) renderState).setSkyblockpv$catOnShoulder(null);
        ((PlayerRenderStateAccessor) renderState).setSkyblockpv$scoreShader(null);
        ((PlayerRenderStateAccessor) renderState).setSkyblockpv$nameShader(null);
        if (player instanceof FakePlayer fakePlayer) {
            fakePlayer.setupRenderState(renderState, partial);
        }
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    public void addCatRenderer(CallbackInfo ci, @Local(argsOnly = true) EntityRendererProvider.Context context) {
        this.addLayer(new CatOnShoulderLayer(this, context.getModelSet()));
    }

    @WrapOperation(method = "renderNameTag(Lnet/minecraft/client/renderer/entity/state/PlayerRenderState;Lnet/minecraft/network/chat/Component;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;renderNameTag(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lnet/minecraft/network/chat/Component;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", ordinal = 0))
    public void scoreShader(PlayerRenderer instance, EntityRenderState entityRenderState, Component component, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, Operation<Void> original) {
        RenderUtils.INSTANCE.pushPopTextShader(((PlayerRenderStateAccessor) entityRenderState).getSkyblockpv$scoreShader(), () -> {
            original.call(instance, entityRenderState, component, poseStack, multiBufferSource, i);
            return Unit.INSTANCE;
        });
    }

    @WrapOperation(method = "renderNameTag(Lnet/minecraft/client/renderer/entity/state/PlayerRenderState;Lnet/minecraft/network/chat/Component;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;renderNameTag(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lnet/minecraft/network/chat/Component;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", ordinal = 1))
    public void nameShader(PlayerRenderer instance, EntityRenderState entityRenderState, Component component, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, Operation<Void> original) {
        RenderUtils.INSTANCE.pushPopTextShader(((PlayerRenderStateAccessor) entityRenderState).getSkyblockpv$nameShader(), () -> {
            original.call(instance, entityRenderState, component, poseStack, multiBufferSource, i);
            return Unit.INSTANCE;
        });
    }

}
