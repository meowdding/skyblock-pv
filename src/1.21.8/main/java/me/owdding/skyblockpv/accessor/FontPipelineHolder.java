package me.owdding.skyblockpv.accessor;

import com.mojang.blaze3d.pipeline.RenderPipeline;

public interface FontPipelineHolder {

    RenderPipeline skyblockpv$getPipeline();

    void skyblockpv$setPipeline(RenderPipeline pipeline);

    static FontPipelineHolder getHolder(Object instance) {
        if (instance instanceof FontPipelineHolder holder) {
            return holder;
        }
        return null;
    }
}
