package me.owdding.skyblockpv.api.data.shared

import com.google.gson.JsonObject
import me.owdding.skyblockpv.api.data.shared.types.HotmData
import me.owdding.skyblockpv.generated.SkyBlockPvCodecs
import me.owdding.skyblockpv.utils.Utils
import me.owdding.skyblockpv.utils.Utils.toDataOrThrow
import me.owdding.skyblockpv.utils.json.getAs
import java.util.*
import java.util.concurrent.CompletableFuture

interface SharedProfileData {
    val id: UUID get() = backingProfile.id
    val hotm: HotmData? get() = backingProfile.hotm.getNowOrElse(null)

    val backingProfile: BackingSharedProfile
    val isEmpty: Boolean

    val dataFuture: CompletableFuture<Void> get() = backingProfile.dataFuture


    companion object {
        fun fromJson(json: JsonObject, user: UUID) = BackingSharedProfile.fromJson(json, user)?.let(::CompletableSharedProfileData)
    }
}


private fun <T> CompletableFuture<T>.getNowOrElse(defaultValue: T) = if (this.isCompletedExceptionally) defaultValue else this.getNow(defaultValue)

data class CompletableSharedProfileData(override val backingProfile: BackingSharedProfile) : SharedProfileData {
    override val isEmpty: Boolean get() = false
}

data class BackingSharedProfile(
    val id: UUID,
    val hotm: CompletableFuture<HotmData?> = emptyFuture()
) {
    val dataFuture: CompletableFuture<Void> = CompletableFuture.allOf(
        hotm
    )

    companion object {

        private fun <T> emptyFuture(): CompletableFuture<T> = CompletableFuture()

        private fun <T> future(supplier: () -> T): CompletableFuture<T> = CompletableFuture.supplyAsync(supplier, Utils.executorPool)
        fun fromJson(json: JsonObject, profileId: UUID): BackingSharedProfile? {
            return BackingSharedProfile(
                id = profileId,
                hotm = future { json.getAs<JsonObject>("hotm").toDataOrThrow(SkyBlockPvCodecs.HotmDataCodec) }
            )
        }
    }
}
