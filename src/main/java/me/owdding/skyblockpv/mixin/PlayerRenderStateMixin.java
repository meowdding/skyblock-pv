package me.owdding.skyblockpv.mixin;

import me.owdding.skyblockpv.utils.CatOnShoulder;
import me.owdding.skyblockpv.utils.PlayerRenderStateAccessor;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PlayerRenderState.class)
public class PlayerRenderStateMixin implements PlayerRenderStateAccessor {

    @Unique
    private CatOnShoulder skyblockpv$catOnShould = null;

    @Override
    public @Nullable CatOnShoulder getSkyblockpv$catOnShoulder() {
        return skyblockpv$catOnShould;
    }

    @Override
    public void setSkyblockpv$catOnShoulder(@Nullable CatOnShoulder catOnShoulder) {
        skyblockpv$catOnShould = catOnShoulder;
    }
}
