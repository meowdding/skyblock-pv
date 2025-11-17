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
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AvatarRenderer.class)
public abstract class PlayerRendererMixin extends LivingEntityRenderer<AbstractClientPlayer, AvatarRenderState, PlayerModel> {

    public PlayerRendererMixin(EntityRendererProvider.Context context, PlayerModel model, float shadowRadius) {
        super(context, model, shadowRadius);
    }

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V", at = @At("TAIL"))
    public void extractRenderState(
        CallbackInfo ci,
        @Local(argsOnly = true) Avatar player,
        @Local(argsOnly = true) AvatarRenderState renderState,
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

    @WrapOperation(method = "submitNameTag(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitNameTag(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/phys/Vec3;ILnet/minecraft/network/chat/Component;ZIDLnet/minecraft/client/renderer/state/CameraRenderState;)V", ordinal = 0))
    public void scoreShader(
        SubmitNodeCollector instance,
        PoseStack poseStack,
        Vec3 vec3,
        int i,
        Component component,
        boolean b,
        int i2,
        double v,
        CameraRenderState cameraRenderState,
        Operation<Void> original,
        @Local(argsOnly = true) AvatarRenderState renderState
    ) {
        RenderUtils.INSTANCE.pushPopTextShader(((PlayerRenderStateAccessor) renderState).getSkyblockpv$scoreShader(), () -> {
            original.call(instance, poseStack, vec3, i, component, b, i2, v, cameraRenderState);
            return Unit.INSTANCE;
        });
    }

    @WrapOperation(method = "submitNameTag(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitNameTag(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/phys/Vec3;ILnet/minecraft/network/chat/Component;ZIDLnet/minecraft/client/renderer/state/CameraRenderState;)V", ordinal = 1))
    public void nameShader(
        SubmitNodeCollector instance,
        PoseStack poseStack,
        Vec3 vec3,
        int i,
        Component component,
        boolean b,
        int i2,
        double v,
        CameraRenderState cameraRenderState,
        Operation<Void> original,
        @Local(argsOnly = true) AvatarRenderState renderState
    ) {
        RenderUtils.INSTANCE.pushPopTextShader(((PlayerRenderStateAccessor) renderState).getSkyblockpv$nameShader(), () -> {
            original.call(instance, poseStack, vec3, i, component, b, i2, v, cameraRenderState);
            return Unit.INSTANCE;
        });
    }

}
