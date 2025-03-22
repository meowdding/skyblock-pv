package tech.thatgravyboat.skyblockpv.utils

import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import eu.pb4.placeholders.api.ParserContext
import eu.pb4.placeholders.api.parsers.TagParser
import org.joml.Vector2i

object CodecUtils {

    val VECTOR_2I = Codec.INT.listOf(2, 2).xmap(
        { Vector2i(it[0], it[1]) },
        { listOf(it.x, it.y) },
    )

    val COMPONENT_TAG = Codec.STRING.xmap(
        { TagParser.QUICK_TEXT_SAFE.parseText(it, ParserContext.of()) },
        { it.string }
    )

    fun <T> Codec<T>.eitherList(): Codec<List<T>> = Codec.either(
        this.xmap({listOf(it)}, {it[0]}),
        this.listOf()
    ).xmap({ Either.unwrap(it)},{ if (it.size == 1) Either.left(it) else Either.right(it) })

}
