package me.owdding.skyblockpv.utils.codecs

import com.mojang.serialization.Codec
import eu.pb4.placeholders.api.ParserContext
import eu.pb4.placeholders.api.parsers.TagParser
import me.owdding.ktcodecs.IncludedCodec
import me.owdding.skyblockpv.generated.SkyBlockPvCodecs
import me.owdding.skyblockpv.utils.theme.PvColors
import net.minecraft.core.ClientAsset
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.util.ExtraCodecs
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import org.joml.Vector2i
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import tech.thatgravyboat.skyblockapi.api.remote.RepoItemsAPI
import tech.thatgravyboat.skyblockapi.api.remote.api.SkyBlockId
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color

object CodecUtils {
    internal fun clientAssetConverter(): (Identifier) -> ClientAsset =
        //? if >= 1.21.9 {
        ClientAsset::ResourceTexture
        //?} else
    /*::ClientAsset*/

    internal inline fun <reified K, reified V> map(): Codec<Map<K, V>> =
        Codec.unboundedMap(SkyBlockPvCodecs.getCodec<K>(), SkyBlockPvCodecs.getCodec<V>())

    internal inline fun <reified K, reified V> mutableMap(): Codec<MutableMap<K, V>> =
        Codec.unboundedMap(SkyBlockPvCodecs.getCodec<K>(), SkyBlockPvCodecs.getCodec<V>())
            .xmap({ it.toMutableMap() }, { it })

    internal inline fun <reified T> list(): Codec<List<T>> {
        return SkyBlockPvCodecs.getCodec<T>().listOf()
    }

    @IncludedCodec
    val CLIENT_ASSET: Codec<ClientAsset> = Identifier.CODEC.xmap(clientAssetConverter()) { it.id() }

    @IncludedCodec(named = "cum_int_list_alt")
    val CUMULATIVE_INT_LIST_ALT: Codec<List<Int>> =
        Codec.INT.listOf().xmap(
            { it.runningFold(0, Int::plus).drop(1) },
            { it.reversed().runningFold(0, Int::minus).reversed() },
        )

    @IncludedCodec(named = "int_long_map")
    val INT_LONG_MAP: Codec<Map<Int, Long>> = Codec.unboundedMap(Codec.STRING, Codec.LONG).xmap(
        { it.mapKeys { entry -> entry.key.toInt() } },
        { it.mapKeys { entry -> entry.key.toString() } },
    )

    @IncludedCodec(named = "cum_int_list")
    val CUMULATIVE_INT_LIST: Codec<List<Int>> =
        Codec.INT.listOf().xmap(
            { it.runningFold(0, Int::plus).distinct() },
            { it.reversed().runningFold(0, Int::minus).reversed() },
        )

    @IncludedCodec(named = "cum_long_list")
    val CUMULATIVE_LONG_LIST: Codec<List<Long>> =
        Codec.LONG.listOf().xmap(
            { it.runningFold(0, Long::plus).distinct() },
            { it.reversed().runningFold(0, Long::minus).reversed() },
        )

    @IncludedCodec(named = "vec_2i")
    val VECTOR_2I: Codec<Vector2i> = Codec.INT.listOf(2, 2).xmap(
        { Vector2i(it[0], it[1]) },
        { listOf(it.x, it.y) },
    )

    @IncludedCodec(named = "component_tag")
    val COMPONENT_TAG: Codec<Component> = Codec.STRING.xmap(
        { TagParser.QUICK_TEXT_SAFE.parseText(it, ParserContext.of()) },
        { it.string },
    )

    @IncludedCodec
    val TEXT_COLOR: Codec<net.minecraft.network.chat.TextColor> = net.minecraft.network.chat.TextColor.CODEC

    @IncludedCodec(named = "cum_string_int_map")
    val CUMULATIVE_STRING_INT_MAP: Codec<List<Map<String, Int>>> = Codec.unboundedMap(Codec.STRING, Codec.INT).listOf().xmap(
        {
            it.runningFold(
                mapOf(),
            ) { acc: Map<String, Int>, mutableMap: MutableMap<String, Int>? ->
                val acc = LinkedHashMap(acc)
                mutableMap?.forEach {
                    acc[it.key] = it.value + (acc[it.key] ?: 0)
                }
                acc
            }.drop(1)
        },
        { it },
    )

    @IncludedCodec(named = "lazy_item_ref")
    val ITEM_REFERENCE: Codec<Lazy<ItemStack>> = Identifier.CODEC.xmap(
        {
            lazy {
                if (it.namespace.equals("skyblock")) {
                    RepoItemsAPI.getItem(it.path.uppercase())
                } else {
                    BuiltInRegistries.ITEM.get(it).map { it.value().defaultInstance }
                        .orElseGet {
                            val defaultInstance = Items.BARRIER.defaultInstance
                            defaultInstance.set(DataComponents.ITEM_NAME, Text.of(it.toString()) { this.color = PvColors.RED })
                            defaultInstance
                        }
                }
            }
        },
        {
            val value = it.value
            val id = value.getData(DataTypes.ID)
            if (id != null) {
                Identifier.fromNamespaceAndPath("skyblock", id.lowercase())
            } else {
                if (value.`is`(Items.BARRIER) && !value.componentsPatch.isEmpty) {
                    Identifier.parse(value.get(DataComponents.ITEM_NAME)?.stripped ?: "barrier")
                } else {
                    BuiltInRegistries.ITEM.getKey(value.item)
                }
            }
        },
    )

    @IncludedCodec(named = "item")
    val ITEM: Codec<Item> = BuiltInRegistries.ITEM.byNameCodec()

    @IncludedCodec
    val SKYBLOCK_ID: Codec<SkyBlockId> = SkyBlockId.UNKNOWN_CODEC

    @IncludedCodec(named = "compact_string_list")
    val COMPACT_STRING_LIST: Codec<List<String>> = ExtraCodecs.compactListCodec(Codec.STRING)

    @IncludedCodec(named = "identifier_map")
    val RESOURCE_MAP: Codec<Map<Identifier, Identifier>> = Codec.unboundedMap(Identifier.CODEC, Identifier.CODEC)
}
