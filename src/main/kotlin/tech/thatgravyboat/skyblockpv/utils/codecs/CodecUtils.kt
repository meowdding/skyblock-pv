package tech.thatgravyboat.skyblockpv.utils.codecs

import com.mojang.serialization.Codec
import eu.pb4.placeholders.api.ParserContext
import eu.pb4.placeholders.api.parsers.TagParser
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Items
import org.joml.Vector2i
import tech.thatgravyboat.skyblockapi.api.data.SkyBlockRarity
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import tech.thatgravyboat.skyblockapi.api.remote.RepoItemsAPI
import tech.thatgravyboat.skyblockapi.utils.codecs.EnumCodec
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

object CodecUtils {

    val INT_LONG_MAP: Codec<Map<Int, Long>> = Codec.unboundedMap(Codec.STRING, Codec.LONG).xmap(
        { it.mapKeys { entry -> entry.key.toInt() } },
        { it.mapKeys { entry -> entry.key.toString() } },
    )

    val SKYBLOCK_RARITY_CODEC: Codec<SkyBlockRarity> = EnumCodec.Companion.of(SkyBlockRarity.entries.toTypedArray())

    val CUMULATIVE_INT_LIST: Codec<List<Int>> =
        Codec.INT.listOf().xmap(
            { it.runningFold(0, Int::plus).distinct() },
            { it.reversed().runningFold(0, Int::minus).reversed() },
        )

    val CUMULATIVE_LONG_LIST: Codec<List<Long>> =
        Codec.LONG.listOf().xmap(
            { it.runningFold(0, Long::plus).distinct() },
            { it.reversed().runningFold(0, Long::minus).reversed() },
        )

    val VECTOR_2I = Codec.INT.listOf(2, 2).xmap(
        { Vector2i(it[0], it[1]) },
        { listOf(it.x, it.y) },
    )

    val COMPONENT_TAG = Codec.STRING.xmap(
        { TagParser.QUICK_TEXT_SAFE.parseText(it, ParserContext.of()) },
        { it.string },
    )

    val CUMULATIVE_STRING_INT_MAP: Codec<List<Map<String, Int>>> = Codec.unboundedMap(Codec.STRING, Codec.INT).listOf().xmap(
        {
            it.runningFold(
                mutableMapOf(),
            ) { acc: MutableMap<String, Int>, mutableMap: MutableMap<String, Int>? ->
                LinkedHashMap(
                    acc.also {
                        mutableMap?.forEach {
                            acc[it.key] = it.value + (acc[it.key] ?: 0)
                        }
                    },
                )
            }.drop(1)
        },
        { it },
    )

    val ITEM_REFRENCE = ResourceLocation.CODEC.xmap(
        {
            lazy {
                if (it.namespace.equals("skyblock")) {
                    RepoItemsAPI.getItem(it.path.uppercase())
                } else {
                    BuiltInRegistries.ITEM.get(it).map { it.value().defaultInstance }
                        .orElseGet {
                            val defaultInstance = Items.BARRIER.defaultInstance
                            defaultInstance.set(DataComponents.ITEM_NAME, Text.of(it.toString()) { this.color = TextColor.RED })
                            defaultInstance
                        }
                }
            }
        },
        {
            val value = it.value
            val id = value.getData(DataTypes.ID)
            if (id != null) {
                ResourceLocation.fromNamespaceAndPath("skyblock", id.lowercase())
            } else {
                if (value.`is`(Items.BARRIER) && !value.componentsPatch.isEmpty) {
                    ResourceLocation.parse(value.get(DataComponents.ITEM_NAME)?.stripped ?: "barrier")
                } else {
                    BuiltInRegistries.ITEM.getKey(value.item)
                }
            }
        },
    )
}
