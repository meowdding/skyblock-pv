package me.owdding.skyblockpv.utils.render

import com.mojang.blaze3d.systems.RenderPass
import com.mojang.blaze3d.vertex.MeshData
import com.mojang.blaze3d.vertex.VertexFormat
import net.minecraft.client.renderer.RenderType

class CustomShaderRenderType(val type: RenderType, val options: (RenderPass) -> Unit = {}) :
    RenderType(type.name, type.bufferSize(), type.affectsCrumbling(), type.sortOnUpload(), { type.setupRenderState() }, { type.clearRenderState() }) {
    override fun draw(meshData: MeshData) {
        //PipelineRenderer.draw(renderPipeline, meshData, options)
    }

    override fun format(): VertexFormat = type.format()

    override fun mode(): VertexFormat.Mode = type.mode()
}
