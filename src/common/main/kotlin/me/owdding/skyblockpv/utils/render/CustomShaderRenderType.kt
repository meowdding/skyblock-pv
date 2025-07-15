package me.owdding.skyblockpv.utils.render

import com.mojang.blaze3d.systems.RenderPass
import net.minecraft.client.renderer.RenderType

class CustomShaderRenderType(val type: RenderType, val options: (RenderPass) -> Unit = {}) {
}
