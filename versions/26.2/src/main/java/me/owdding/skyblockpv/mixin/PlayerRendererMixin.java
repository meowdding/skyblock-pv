package me.owdding.skyblockpv.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import kotlin.Unit;
import me.owdding.skyblockpv.utils.FakePlayer;
import me.owdding.skyblockpv.utils.PlayerRenderStateAccessor;
import me.owdding.skyblockpv.utils.render.CatOnShoulderLayer;
import me.owdding.skyblockpv.utils.render.RenderUtils;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Avatar;
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
        ((PlayerRenderStateAccessor) renderState).setSkyblockpv$isCatBaby(null);
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

    @WrapMethod(method = "submitNameDisplay(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V")
    public void scoreShader(
        AvatarRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera, Operation<Void> original
    ) {
        RenderUtils.INSTANCE.pushPopTextShader(((PlayerRenderStateAccessor) state).getSkyblockpv$scoreShader(), () -> {
            original.call(state, poseStack, submitNodeCollector, camera);
            return Unit.INSTANCE;
        });
    }
}
