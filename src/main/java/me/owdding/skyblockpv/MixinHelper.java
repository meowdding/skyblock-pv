package me.owdding.skyblockpv;

import com.mojang.blaze3d.pipeline.RenderPipeline;

public class MixinHelper {

    public static final ThreadLocal<RenderPipeline> FONT_PIPELINE = ThreadLocal.withInitial(() -> null);
    public static boolean skipTextShader = false;

}
