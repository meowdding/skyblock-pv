package me.owdding.skyblockpv.utils

import me.owdding.lib.rendering.text.TextShader
import java.net.URI

interface PlayerRenderStateAccessor {
    var `skyblockpv$catOnShoulder`: URI?
    var `skyblockpv$nameShader`: TextShader?
    var `skyblockpv$scoreShader`: TextShader?
}
