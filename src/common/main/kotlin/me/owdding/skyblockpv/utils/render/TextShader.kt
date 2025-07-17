@file:Suppress("unused")

package me.owdding.skyblockpv.utils.render

import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.systems.RenderPass

interface TextShader {

    val pipeline: RenderPipeline
    fun pass(renderPass: RenderPass)

    val useWhite: Boolean get() = true
    val hasShadow: Boolean? get() = null

    companion object {

    }
}
