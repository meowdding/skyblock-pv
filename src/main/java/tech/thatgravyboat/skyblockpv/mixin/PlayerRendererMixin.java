package tech.thatgravyboat.skyblockpv.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tech.thatgravyboat.skyblockpv.utils.CatOnShoulderLayer;
import tech.thatgravyboat.skyblockpv.utils.FakePlayer;

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
        if (player instanceof FakePlayer fakePlayer) {
            fakePlayer.setupRenderState(renderState, partial);
        }
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    public void addCatRenderer(CallbackInfo ci, @Local(argsOnly = true) EntityRendererProvider.Context context) {
        this.addLayer(new CatOnShoulderLayer(this, context.getModelSet()));
    }

}
