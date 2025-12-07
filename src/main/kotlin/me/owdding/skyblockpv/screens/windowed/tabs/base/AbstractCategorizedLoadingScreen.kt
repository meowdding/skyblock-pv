package me.owdding.skyblockpv.screens.windowed.tabs.base

import com.mojang.authlib.GameProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.api.CachedApi
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.utils.theme.PvColors
import net.minecraft.network.chat.Component
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.Scheduling
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import kotlin.time.Duration.Companion.seconds

abstract class AbstractCategorizedLoadingScreen<V>(name: String, gameProfile: GameProfile, profile: SkyBlockProfile? = null) :
    AbstractCategorizedScreen(name, gameProfile, profile) {

    abstract val api: CachedApi<SkyBlockProfile, V, *>

    var data: Result<V>? = null
    private var isDoneLoading = false
    private var initiatedWithData = false

    init {
        profile?.let { onProfileSwitch(it) }
    }

    override fun init() {
        if (data != null) {
            initiatedWithData = true
        }
        super.init()
    }

    override fun onProfileSwitch(profile: SkyBlockProfile) {
        data = null
        initiatedWithData = false
        isDoneLoading = false
        val cachedData = api.getCached(profile)
        if (cachedData != null) {
            data = Result.success(cachedData)
            isDoneLoading = true
            initiatedWithData = true
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            this@AbstractCategorizedLoadingScreen.data = api.getData(profile, "screen").onFailure { exception ->
                SkyBlockPv.error("Failed to get data for ${gameProfile.name} on profile (${profile.id.name}) for ${api::class.java.simpleName}", exception)
            }

            isDoneLoading = true
            if (!initiatedWithData) {
                McClient.runNextTick { safelyRebuild() }
            }
        }

        Scheduling.schedule(10.seconds) {
            if (data == null) {
                McClient.runNextTick { safelyRebuild() }
            }
        }
    }

    protected fun loading(
        onSuccess: (V) -> Unit,
        loadingValue: () -> Unit,
        errorValue: () -> Unit,
    ) {
        val data = this.data
        return when {
            data == null -> loadingValue()
            data.isFailure -> errorValue()
            else -> onSuccess(data.getOrThrow())
        }
    }

    protected fun <T> loaded(
        whileLoading: T,
        onError: T,
        onSuccess: (V) -> T,
    ): T {
        val data = this.data
        return when {
            data == null -> whileLoading
            data.isFailure -> onError
            else -> onSuccess(data.getOrThrow())
        }
    }

    protected fun <T> loadingValue(
        successValue: T,
        loadingValue: T,
        errorValue: T,
    ): T {
        val data = data
        return when {
            data == null -> loadingValue
            data.isFailure -> errorValue
            else -> successValue
        }
    }

    protected val loadingMessage by lazy { Text.of("Loading...") { this.color = PvColors.RED } }
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
        successMessage: (V) -> Component,
    ): Component {
        return loaded(loadingMessage, errorMessage, successMessage)
    }
}
