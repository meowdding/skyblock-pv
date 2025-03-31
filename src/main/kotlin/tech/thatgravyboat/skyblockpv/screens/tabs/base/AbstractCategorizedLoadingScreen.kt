package tech.thatgravyboat.skyblockpv.screens.tabs.base

import com.mojang.authlib.GameProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.minecraft.network.chat.Component
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.Scheduling
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockpv.api.CachedApi
import tech.thatgravyboat.skyblockpv.api.data.SkyBlockProfile
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
        CoroutineScope(Dispatchers.IO).launch {
            this@AbstractCategorizedLoadingScreen.data = api.getData(profile)

            isDoneLoading = true
            if (!initiatedWithData) {
                McClient.tell { rebuildWidgets() }
            }
        }

        Scheduling.schedule(10.seconds) {
            if (data == null) {
                McClient.tell { rebuildWidgets() }
            }
        }
    }


    protected fun loading(
        data: Result<V>? = this.data,
        onSuccess: (V) -> Unit,
        loadingValue: () -> Unit,
        errorValue: () -> Unit,
    ) {
        return when {
            data == null -> loadingValue()
            data.isFailure -> errorValue()
            else -> onSuccess(data.getOrThrow())
        }
    }

    protected fun <T> loaded(
        data: Result<V>? = this.data,
        whileLoading: T,
        onError: T,
        onSuccess: (V) -> T,
    ): T {
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
    protected val loadingMessage by lazy {  Text.of("Loading...") { this.color = TextColor.RED } }
    protected val errorMessage by lazy {  Text.of("Error...") { this.color = TextColor.RED } }


    protected fun loadingComponent(
        successMessage: Component,
        loadingMessage: Component = this.loadingMessage,
        errorMessage: Component = this.errorMessage,
    ): Component {
        return loadingValue(successMessage, loadingMessage, errorMessage)
    }

    protected fun loadingComponent(
        data: Result<V>? = this.data,
        loadingMessage: Component = this.loadingMessage,
        errorMessage: Component = this.errorMessage,
        successMessage: (V) -> Component,
    ): Component {
        return loaded(data, loadingMessage, errorMessage, successMessage)
    }
}
