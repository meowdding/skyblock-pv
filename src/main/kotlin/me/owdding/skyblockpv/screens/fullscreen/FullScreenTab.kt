package me.owdding.skyblockpv.screens.fullscreen

import kotlinx.coroutines.runBlocking
import me.owdding.lib.layouts.setPos
import me.owdding.skyblockpv.api.CachedApi
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.utils.Utils
import me.owdding.skyblockpv.utils.theme.PvColors
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.network.chat.Component
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import java.util.concurrent.CompletableFuture
import kotlin.getOrThrow

interface FullScreenTab {
    context(profile: SkyBlockProfile)
    fun init() {
    }

    fun requiresRebuild(): Boolean = false
    fun consumeRebuild() {}

    context(screen: BaseFullScreenPvScreen, profile: SkyBlockProfile)
    fun create(x: Int, y: Int, width: Int, height: Int)

    context(screen: BaseFullScreenPvScreen)
    fun Layout.applyLayout(x: Int, y: Int) {
        this.setPos(x, y).visitWidgets(screen::addRenderableWidget)
    }
}

abstract class LoadingFullScreenTab<Data> : FullScreenTab {

    abstract val api: CachedApi<SkyBlockProfile, Data, *>
    private var _requiresRebuild = false
    override fun requiresRebuild(): Boolean = _requiresRebuild
    override fun consumeRebuild() {
        _requiresRebuild = false
    }

    context(profile: SkyBlockProfile)
    override fun init() {
        dataResult = CompletableFuture.supplyAsync(
            {
                runBlocking {
                    api.getData(profile, "screen")
                }
            },
            Utils.executorPool,
        ).thenComposeAsync {
            if (it.isSuccess) {
                CompletableFuture.completedStage(it.getOrThrow())
            } else {
                CompletableFuture.failedStage(it.exceptionOrNull()!!)
            }
        }
        dataResult.thenRun {
            _requiresRebuild = true
        }
    }

    var dataResult: CompletableFuture<Data> = CompletableFuture()

    protected fun loading(
        onSuccess: (Data) -> Unit,
        loadingValue: () -> Unit,
        errorValue: () -> Unit,
    ) {
        val data = this.dataResult
        return when {
            !data.isDone -> loadingValue()
            data.isCompletedExceptionally -> errorValue()
            else -> onSuccess(data.get())
        }
    }

    protected fun <T> loaded(
        whileLoading: T,
        onError: T,
        onSuccess: (Data) -> T,
    ): T {
        val data = this.dataResult
        return when {
            !data.isDone -> whileLoading
            data.isCompletedExceptionally -> onError
            else -> onSuccess(data.get())
        }
    }

    protected fun <T> loadingValue(
        successValue: T,
        loadingValue: T,
        errorValue: T,
    ): T {
        val data = dataResult
        return when {
            !data.isDone -> loadingValue
            data.isCompletedExceptionally -> errorValue
            else -> successValue
        }
    }

    protected val loadingMessage by lazy { Text.of("Loading...") { this.color = PvColors.YELLOW } }
    protected val errorMessage by lazy { Text.of("Error...") { this.color = PvColors.RED } }


    protected fun loadingComponent(
        successMessage: Component,
        loadingMessage: Component = this.loadingMessage,
        errorMessage: Component = this.errorMessage,
    ): Component {
        return loadingValue(successMessage, loadingMessage, errorMessage)
    }

    protected fun loadingComponent(
        loadingMessage: Component = this.loadingMessage,
        errorMessage: Component = this.errorMessage,
        successMessage: (Data) -> Component,
    ): Component {
        return loaded(loadingMessage, errorMessage, successMessage)
    }
}
