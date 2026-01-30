package me.owdding.skyblockpv.api

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.owdding.ktmodules.AutoCollect
import me.owdding.ktmodules.Module
import me.owdding.skyblockpv.api.data.shared.SharedDataProvider
import me.owdding.skyblockpv.generated.SkyBlockPvSharedData
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.base.predicates.TimePassed
import tech.thatgravyboat.skyblockapi.api.events.misc.RegisterCommandsEvent
import tech.thatgravyboat.skyblockapi.api.events.profile.ProfileChangeEvent
import tech.thatgravyboat.skyblockapi.api.events.time.TickEvent
import tech.thatgravyboat.skyblockapi.api.profile.profile.ProfileAPI
import tech.thatgravyboat.skyblockapi.utils.json.Json.toJson
import java.util.UUID

@Module
object SharedDataManager {

    val lastData = mutableMapOf<SharedDataProvider<*>, Any>()

    @Subscription
    fun registerCommand(event: RegisterCommandsEvent) {
        event.registerWithCallback("sbpv sync") {
            sync()
        }
    }

    @Subscription(TickEvent::class)
    @TimePassed("10m")
    fun sync() {
        val profileId = ProfileAPI.profileId ?: return
        SkyBlockPvSharedData.collected.forEach { provider ->
            handleProvider(provider, profileId)
        }
    }

    private fun <Type : Any> handleProvider(provider: SharedDataProvider<Type>, profileId: UUID) {
        val data = provider.create()
        val lastData = getLastData(provider)
        if (data == lastData) return
        scheduleSync(provider, data, profileId)
    }

    private fun <Type : Any> scheduleSync(provider: SharedDataProvider<Type>, data: Type, uuid: UUID) {
        CoroutineScope(Dispatchers.IO).launch {
            PvAPI.put("/shared_data/$uuid/${provider.endpoint}", "update", data.toJson(provider.codec) ?: return@launch)
        }
    }

    private fun <T : Any> getLastData(provider: SharedDataProvider<T>): T? = lastData[provider] as? T

    @Subscription
    fun onProfileSwitch(event: ProfileChangeEvent) {
        lastData.clear()
    }

}

@AutoCollect
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class SharedData
