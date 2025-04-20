package me.owdding.skyblockpv.utils.codecs

import com.google.common.collect.ImmutableMap
import com.mojang.datafixers.util.Pair
import com.mojang.datafixers.util.Unit
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import com.mojang.serialization.Lifecycle
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap
import it.unimi.dsi.fastutil.objects.Object2ObjectMap
import java.util.stream.Stream
import kotlin.jvm.optionals.getOrNull

class ReservedUnboundMapCodec<K, V>(
    private val keyCodec: Codec<K>,
    private val elementCodec: Codec<V>,
    private val excludedKeys: Set<K>,
) : Codec<Map<K, V>> {

    constructor(
        keyCodec: Codec<K>,
        elementCodec: Codec<V>,
        vararg excludedKeys: K
    ) : this(keyCodec, elementCodec, excludedKeys.toSet())

    override fun <T : Any> decode(
        ops: DynamicOps<T>,
        input: T,
    ): DataResult<Pair<Map<K, V>, T>> = ops.getMap(input).setLifecycle(Lifecycle.stable()).flatMap { data ->
        val outputs: Object2ObjectMap<K, V> = Object2ObjectArrayMap()
        val failed: Stream.Builder<Pair<T, T>> = Stream.builder()

        val result: DataResult<Unit> = data.entries().reduce(
            DataResult.success(Unit.INSTANCE, Lifecycle.stable()),
            { result, entry ->
                val key = keyCodec.parse(ops, entry.first)

                if (!key.result().map { it in excludedKeys }.orElse(false)) {
                    val entryResult: DataResult<Pair<K, V>> = key.apply2stable(
                        Pair<K, V>::of,
                        elementCodec.parse(ops, entry.second),
                    )

                    entryResult.resultOrPartial().getOrNull()?.let { pair ->
                        val existingValue = outputs.put(pair.first, pair.second)
                        if (existingValue != null) {
                            failed.add(entry)

                            return@reduce result.apply2stable<Unit, Unit>(
                                { it, _ -> it },
                                DataResult.error {
                                    "Duplicate entry for key: '${pair.first}'"
                                }
                            )
                        }
                    }

                    if (entryResult.isError) {
                        failed.add(entry)
                    }

                    return@reduce result.apply2stable({ it, _ -> it }, entryResult)
                }

                return@reduce result
            },
            { result1, result2 -> result1.apply2stable({ it, _ -> it }, result2) },
        )

        val elements = ImmutableMap.copyOf(outputs)
        val errors = ops.createMap(failed.build())

        return@flatMap result.map { _ -> elements }.setPartial(elements).mapError { error -> "$error missed input: $errors"}
    }.map { Pair.of(it, input) }

    override fun <T : Any> encode(
        input: Map<K, V>,
        ops: DynamicOps<T>,
        prefix: T,
    ): DataResult<T> {
        val map = ops.mapBuilder()
        input.forEach { key, value ->
            map.add(
                keyCodec.encodeStart(ops, key),
                elementCodec.encodeStart(ops, value),
            )
        }
        return map.build(prefix)
    }

}
