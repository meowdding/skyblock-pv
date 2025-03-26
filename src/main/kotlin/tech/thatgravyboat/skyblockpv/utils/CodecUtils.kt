package tech.thatgravyboat.skyblockpv.utils

import com.mojang.serialization.Codec
import eu.pb4.placeholders.api.ParserContext
import eu.pb4.placeholders.api.parsers.TagParser
import org.joml.Vector2i

object CodecUtils {

    val CUMULATIVE_INT_LIST: Codec<List<Int>> =
        Codec.INT.listOf().xmap(
            { it.runningFold(0, Int::plus).distinct() },
            { it.reversed().runningFold(0, Int::minus).reversed() }
        )

    val VECTOR_2I = Codec.INT.listOf(2, 2).xmap(
        { Vector2i(it[0], it[1]) },
        { listOf(it.x, it.y) },
    )

    val COMPONENT_TAG = Codec.STRING.xmap(
        { TagParser.QUICK_TEXT_SAFE.parseText(it, ParserContext.of()) },
        { it.string },
    )

    val CUMULATIVE_STRING_INT_MAP: Codec<List<Map<String, Int>>> = Codec.unboundedMap(Codec.STRING, Codec.INT).listOf()
        .xmap({
            it.runningFold(mutableMapOf(), { acc: MutableMap<String, Int>, mutableMap: MutableMap<String, Int>? ->
                LinkedHashMap(acc.also {
                    mutableMap?.forEach {
                        acc[it.key] = it.value +(acc[it.key] ?: 0)
                    }
                })
            }).drop(1)
        }, {it})
}
