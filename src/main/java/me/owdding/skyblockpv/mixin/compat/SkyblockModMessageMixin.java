package me.owdding.skyblockpv.mixin.compat;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;


@Pseudo
@IfModLoaded("skyblockmod")
@Mixin(targets = "com.kevinthegreat.skyblockmod.util.Message", remap = false)
public class SkyblockModMessageMixin {
    @Shadow(remap = false)
    @Final
    public Map<String, String> commands;

    @Inject(method = "Lcom/kevinthegreat/skyblockmod/util/Message;<init>()V", at = @At(value = "INVOKE", target = "Ljava/util/SortedMap;putAll(Ljava/util/Map;)V"))
    private void init(CallbackInfo callbackInfo) {
        commands.remove("/pv");
    }

}
