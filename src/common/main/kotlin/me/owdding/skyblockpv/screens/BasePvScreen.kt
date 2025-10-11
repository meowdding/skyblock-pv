package me.owdding.skyblockpv.screens

import com.google.gson.*
import com.mojang.authlib.GameProfile
import com.mojang.serialization.JsonOps
import com.teamresourceful.resourcefullib.client.screens.BaseCursorScreen
import earth.terrarium.olympus.client.components.Widgets
import earth.terrarium.olympus.client.components.renderers.WidgetRenderers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.owdding.lib.displays.Alignment
import me.owdding.lib.displays.DisplayWidget
import me.owdding.lib.displays.asWidget
import me.owdding.lib.layouts.setPos
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.api.PlayerAPI
import me.owdding.skyblockpv.api.ProfileAPI
import me.owdding.skyblockpv.api.data.profile.EmptySkyBlockProfile
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.utils.ChatUtils
import me.owdding.skyblockpv.utils.Utils.multiLineDisplay
import me.owdding.skyblockpv.utils.Utils.unaryPlus
import me.owdding.skyblockpv.utils.components.PvLayouts
import me.owdding.skyblockpv.utils.displays.ExtraDisplays
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.Util
import net.minecraft.client.gui.layouts.FrameLayout
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.client.gui.layouts.LayoutElement
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.skyblockapi.helpers.McPlayer
import tech.thatgravyboat.skyblockapi.platform.id
import tech.thatgravyboat.skyblockapi.utils.text.CommonText
import java.lang.reflect.Type
import java.nio.file.Files
import java.util.concurrent.CompletableFuture

abstract class BasePvScreen(val name: String, val gameProfile: GameProfile, initProfile: SkyBlockProfile?) : BaseCursorScreen(CommonText.EMPTY) {

    val starttime = System.currentTimeMillis()
    var profiles: List<SkyBlockProfile> = emptyList()

    abstract val uiWidth: Int
    abstract val uiHeight: Int

    var initedWithProfile = false
    private var requireRebuild = false

    open val tabTitle: Component get() = +"tab.${name.lowercase()}"

    var profile: SkyBlockProfile = EmptySkyBlockProfile(gameProfile.id, EmptySkyBlockProfile.Reason.LOADING)

    init {
        CoroutineScope(Dispatchers.IO).launch { PlayerAPI.getPlayer(gameProfile) /*Load player data in the background*/ }
        CoroutineScope(Dispatchers.IO).launch {
            profiles = ProfileAPI.getProfiles(gameProfile)
            if (profiles.isEmpty()) {
                profile = EmptySkyBlockProfile(gameProfile.id, EmptySkyBlockProfile.Reason.NO_PROFILES)
                requireRebuild = true
                return@launch
            }
            if (initedWithProfile) return@launch
            val selected = initProfile ?: profiles.find { it.selected } ?: return@launch
            onProfileSwitch(selected)
            profile = selected
            requireRebuild = true
            selected.dataFuture.whenComplete { _, throwable ->
                if (throwable != null) {
                    profile = EmptySkyBlockProfile(gameProfile.id, EmptySkyBlockProfile.Reason.ERROR, throwable)
                }
                requireRebuild = true
            }
        }
    }

    override fun tick() {
        super.tick()
        if (requireRebuild) safelyRebuild()
    }

    fun isProfileInitialized() = profile !is EmptySkyBlockProfile
    fun isProfileOfUser() = gameProfile.id == McPlayer.uuid

    abstract fun create(bg: DisplayWidget)

    protected fun safelyRebuild() {
        if (this.minecraft == null) return
        if (isProfileInitialized()) onProfileSwitch(profile)
        requireRebuild = false
        rebuildWidgets()
    }

    fun LayoutElement.applyLayout() = this.visitWidgets(::addRenderableWidget)
    fun Layout.applyLayout(x: Int, y: Int) = this.setPos(x, y).applyLayout()

    protected fun addLoader() {
        if (isProfileInitialized()) return

        val loading = ExtraDisplays.loading().asWidget()
        FrameLayout.centerInRectangle(loading, 0, 0, this.width, this.height)

        if (starttime + 8000 > System.currentTimeMillis()) return loading.visitWidgets(this::addRenderableOnly)

        val errorWidget = PvLayouts.vertical(alignment = 0.5f) {
            widget(loading)
            spacer(height = 20)

            display((+"widgets.error.loading").multiLineDisplay(Alignment.CENTER))

            spacer(height = 20)

            widget(
                Widgets.button()
                    .withRenderer(WidgetRenderers.text(+"widgets.open_logs"))
                    .withSize(100, 20)
                    .withCallback {
                        Util.getPlatform().openPath(FabricLoader.getInstance().gameDir.resolve("logs"))
                    },
            )
        }

        FrameLayout.centerInRectangle(errorWidget, 0, 0, this.width, this.height)
        errorWidget.applyLayout()
    }

    protected fun saveProfiles() {
        val file = SkyBlockPv.configDir.resolve("profiles-${gameProfile.id}.json")

        Files.createDirectories(file.parent)
        Files.writeString(
            file,
            GsonBuilder()
                .registerTypeAdapter(
                    ItemStack::class.java,
                    object : JsonSerializer<ItemStack> {
                        override fun serialize(src: ItemStack?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
                            if (src == null) {
                                return JsonNull.INSTANCE
                            }

                            val encodeStart = ItemStack.CODEC.encodeStart(JsonOps.INSTANCE, src)
                            if (encodeStart.isError) {
                                return JsonPrimitive(encodeStart.error().get().messageSupplier().get())
                            }
                            return encodeStart.getOrThrow()
                        }
                    },
                )
                .registerTypeAdapter(
                    CompletableFuture::class.java,
                    object : JsonSerializer<CompletableFuture<*>> {
                        override fun serialize(src: CompletableFuture<*>?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
                            if (src == null) {
                                return JsonNull.INSTANCE
                            }

                            return JsonPrimitive("Future: ${src.get()}")
                        }
                    },
                )
                .create().toJson(profiles),
        )

        ChatUtils.chat("Profiles saved to .minecraft/config/skyblockpv/")
    }

    open fun onProfileSwitch(profile: SkyBlockProfile) {}
    override fun isPauseScreen() = false
}
