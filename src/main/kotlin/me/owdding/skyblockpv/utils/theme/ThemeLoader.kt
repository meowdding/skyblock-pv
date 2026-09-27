package me.owdding.skyblockpv.utils.theme

import com.google.gson.JsonElement
import me.owdding.ktmodules.Module
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.config.Config
import me.owdding.skyblockpv.utils.Utils.toData
import me.owdding.skyblockpv.utils.theme.ThemeHelper.location
import me.owdding.skyblockpv.utils.theme.ThemeHelper.themes
import net.minecraft.server.packs.resources.PreparableReloadListener
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.json.Json.readJson
import java.nio.charset.StandardCharsets
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor

@Module
object ThemeLoader : PreparableReloadListener {
    override fun reload(
        state: PreparableReloadListener.SharedState,
        backgroundExecutor: Executor,
        barrier: PreparableReloadListener.PreparationBarrier,
        gameExecutor: Executor,
    ): CompletableFuture<Void> = CompletableFuture.runAsync {
        themes.clear()

        val resources = location.listMatchingResources(state.resourceManager())
            .filter { (key) ->
                key.namespace.equals(SkyBlockPv.RESOURCE_PATH, true)
            }

        var configChanged = false
        var defaultAppliedThisReload = false

        resources.forEach { (id, resource) ->
            val theme = resource.open().readAllBytes().toString(StandardCharsets.UTF_8).readJson<JsonElement>().toData(PvTheme.CODEC) ?: return@forEach
            val themeId = location.fileToId(id)
            themes[themeId] = theme

            val themeIdString = themeId.toString()

            if (theme.autoDefault && themeIdString !in Config.seenAutoThemes) {
                Config.seenAutoThemes = (Config.seenAutoThemes.toList() + themeIdString).toTypedArray()
                configChanged = true

                if (!defaultAppliedThisReload) {
                    Config.theme = themeId
                    defaultAppliedThisReload = true
                }
            }
        }

        if (configChanged) {
            SkyBlockPv.config.save()
        }
    }.thenCompose { barrier.wait(it) }

    init {
        McClient.registerClientReloadListener(SkyBlockPv.id("themes"), this)
    }
}
