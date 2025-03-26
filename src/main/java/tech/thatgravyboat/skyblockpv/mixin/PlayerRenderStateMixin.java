package tech.thatgravyboat.skyblockpv.mixin;

import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import tech.thatgravyboat.skyblockpv.utils.CatOnShoulder;
import tech.thatgravyboat.skyblockpv.utils.PlayerRenderStateAccessor;

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
