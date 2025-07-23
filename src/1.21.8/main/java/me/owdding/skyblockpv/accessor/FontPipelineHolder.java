package me.owdding.skyblockpv.accessor;

import com.mojang.blaze3d.pipeline.RenderPipeline;

public interface FontPipelineHolder {

    ThreadLocal<RenderPipeline> GLOBAL_PIPELINE = ThreadLocal.withInitial(() -> null);

    RenderPipeline sbpv$getPipeline();

    void sbpv$setPipeline(RenderPipeline pipeline);
}
