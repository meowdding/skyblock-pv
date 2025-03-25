package tech.thatgravyboat.skyblockpv.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tech.thatgravyboat.skyblockpv.utils.FakePlayer;

@Mixin(PlayerRenderer.class)
public class PlayerRendererMixin {

    @Inject(method = "extractRenderState(Lnet/minecraft/client/player/AbstractClientPlayer;Lnet/minecraft/client/renderer/entity/state/PlayerRenderState;F)V", at = @At("TAIL"))
    public void extractRenderState(CallbackInfo ci, @Local(argsOnly = true) AbstractClientPlayer player, @Local(argsOnly = true) PlayerRenderState renderState, @Local(argsOnly = true) float partial) {
        if (player instanceof FakePlayer fakePlayer) {
            fakePlayer.setupRenderState(renderState, partial);
        }
    }

}
