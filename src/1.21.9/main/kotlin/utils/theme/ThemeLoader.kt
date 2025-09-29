package me.owdding.skyblockpv.utils.theme

import com.google.gson.JsonElement
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.utils.Utils.toData
import me.owdding.skyblockpv.utils.theme.ThemeHelper.location
import me.owdding.skyblockpv.utils.theme.ThemeHelper.themes
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.resources.PreparableReloadListener
import tech.thatgravyboat.skyblockapi.utils.json.Json.readJson
import java.nio.charset.StandardCharsets
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor

object ThemeLoader : IdentifiableResourceReloadListener {
    override fun getFabricId() = SkyBlockPv.id("themes")

    override fun reload(
        state: PreparableReloadListener.SharedState,
        backgroundExecutor: Executor,
        barrier: PreparableReloadListener.PreparationBarrier,
        gameExecutor: Executor,
    ): CompletableFuture<Void> = CompletableFuture.runAsync {
        themes.clear()

        location.listMatchingResources(state.resourceManager()).filter { (key) -> key.namespace.equals(SkyBlockPv.RESOURCE_PATH, true) }
            .forEach { (id, resource) ->
                val theme = resource.open().readAllBytes().toString(StandardCharsets.UTF_8).readJson<JsonElement>().toData(PvTheme.CODEC) ?: return@forEach

                themes[location.fileToId(id)] = theme
            }
    }.thenCompose { barrier.wait(it) }

    init {
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(this)
    }

}
