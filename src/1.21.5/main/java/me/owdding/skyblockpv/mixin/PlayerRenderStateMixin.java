package me.owdding.skyblockpv.mixin;

import me.owdding.lib.rendering.text.TextShader;
import me.owdding.skyblockpv.utils.PlayerRenderStateAccessor;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.net.URI;

@Mixin(PlayerRenderState.class)
public class PlayerRenderStateMixin implements PlayerRenderStateAccessor {

    @Unique
    private URI skyblockpv$catOnShould = null;
    @Unique
    private TextShader skyblockpv$nameShader = null;
    @Unique
    private TextShader skyblockpv$scoreShader = null;

    @Override
    public @Nullable URI getSkyblockpv$catOnShoulder() {
        return skyblockpv$catOnShould;
    }

    @Override
    public void setSkyblockpv$catOnShoulder(@Nullable URI catOnShoulder) {
        skyblockpv$catOnShould = catOnShoulder;
    }

    @Override
    public @Nullable TextShader getSkyblockpv$nameShader() {
        return this.skyblockpv$nameShader;
    }

    @Override
    public void setSkyblockpv$nameShader(@Nullable TextShader textShader) {
        this.skyblockpv$nameShader = textShader;
    }

    @Override
    public @Nullable TextShader getSkyblockpv$scoreShader() {
        return this.skyblockpv$scoreShader;
    }

    @Override
    public void setSkyblockpv$scoreShader(@Nullable TextShader textShader) {
        this.skyblockpv$scoreShader = textShader;
    }
}
