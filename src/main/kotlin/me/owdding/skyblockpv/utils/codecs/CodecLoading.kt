package me.owdding.skyblockpv.utils.codecs

import me.owdding.ktmodules.AutoCollect

interface ExtraData {
    suspend fun load()
    fun loadFallback(): Result<Unit>
}

interface DefaultedData : ExtraData {
    override fun loadFallback(): Result<Unit> = Result.success(Unit)
}

@AutoCollect("ExtraData")
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class LoadData
