package me.owdding.skyblockpv.utils.theme

import com.google.gson.JsonElement
import me.owdding.ktmodules.Module
import me.owdding.skyblockpv.SkyBlockPv.RESOURCE_PATH
import me.owdding.skyblockpv.SkyBlockPv.id
import me.owdding.skyblockpv.generated.SkyBlockPVCodecs
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.minecraft.resources.FileToIdConverter
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.resources.PreparableReloadListener
import net.minecraft.server.packs.resources.ResourceManager
import tech.thatgravyboat.skyblockapi.utils.json.Json.readJson
import tech.thatgravyboat.skyblockapi.utils.json.Json.toData
import java.nio.charset.StandardCharsets
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor

@Module
object ThemeLoader : IdentifiableResourceReloadListener {
    override fun getFabricId() = id("themes")
    val location: FileToIdConverter = FileToIdConverter.json("themes")
    val themes: MutableMap<ResourceLocation, PvTheme> = mutableMapOf()

    override fun reload(
        barrier: PreparableReloadListener.PreparationBarrier,
        manager: ResourceManager,
        backgroundExecutor: Executor,
        gameExecutor: Executor,
    ): CompletableFuture<Void> = CompletableFuture.runAsync {
        themes.clear()

        location.listMatchingResources(manager).filter { (key) -> key.namespace.equals(RESOURCE_PATH, true) }.forEach { (id, resource) ->
            val theme = resource.open().readAllBytes().toString(StandardCharsets.UTF_8).readJson<JsonElement>().toData(SkyBlockPVCodecs.PvThemeCodec.codec())
                ?: return@forEach

            themes[location.fileToId(id)] = theme
        }
    }.thenCompose { barrier.wait(it) }


    init {
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(this)
    }

}
