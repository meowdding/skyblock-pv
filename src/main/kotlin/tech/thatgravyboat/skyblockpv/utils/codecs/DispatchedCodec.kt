package tech.thatgravyboat.skyblockpv.utils.codecs

import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import com.mojang.serialization.MapCodec
import com.mojang.serialization.MapLike
import com.mojang.serialization.RecordBuilder
import java.util.stream.Stream

class DispatchedCodec<K, V>(
    private val keyCodec: MapCodec<K>,
    private val keyGetter: (V) -> K,
    private val valueCodec: (K) -> DataResult<MapCodec<V>>,
): MapCodec<V>() {

    override fun <T : Any> decode(
        ops: DynamicOps<T>,
        input: MapLike<T>,
    ): DataResult<V> {
        if (ops.compressMaps()) error("DispatchedCodec does not support compressed maps")

        return keyCodec.decode(ops, input).flatMap { key ->
            valueCodec.invoke(key).flatMap { codec ->
                codec.decode(ops, input)
            }
        }
    }

    override fun <T : Any> encode(
        input: V,
        ops: DynamicOps<T>,
        prefix: RecordBuilder<T>,
    ): RecordBuilder<T> {
        if (ops.compressMaps()) error("DispatchedCodec does not support compressed maps")

        val key = keyGetter.invoke(input)
        val result = valueCodec.invoke(key)

        if (result.isError) {
            return prefix.withErrorsFrom(result)
        }

        return result.orThrow.encode(input, ops, keyCodec.encode(key, ops, prefix))
    }

    override fun <T : Any> keys(ops: DynamicOps<T>): Stream<T> = keyCodec.keys(ops)
}
