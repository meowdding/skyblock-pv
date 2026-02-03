package me.owdding.skyblockpv.api.data.shared

import com.mojang.serialization.Codec

interface SharedDataProvider<Data : Any> {

    val endpoint: String
    val codec: Codec<Data>
    fun create(): Data

}
