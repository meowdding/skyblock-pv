package me.owdding.skyblockpv.utils.codecs

import me.owdding.ktmodules.AutoCollect

interface ExtraData {
    fun load()
}

@AutoCollect("ExtraData")
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class LoadData
