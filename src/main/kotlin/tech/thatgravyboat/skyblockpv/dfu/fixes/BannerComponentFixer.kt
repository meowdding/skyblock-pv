package tech.thatgravyboat.skyblockpv.dfu.fixes

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.minecraft.core.Holder
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.ItemTags
import net.minecraft.util.datafix.ExtraDataFixUtils
import net.minecraft.world.item.DyeColor
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.entity.BannerPattern
import net.minecraft.world.level.block.entity.BannerPatternLayers
import net.minecraft.world.level.block.entity.BannerPatternLayers.Layer
import tech.thatgravyboat.skyblockpv.dfu.DataComponentFixer
import kotlin.jvm.optionals.getOrNull

private val PATTERN_MAP = Object2ObjectOpenHashMap<String, Holder<BannerPattern>>().apply {
    fun createPattern(parse: ResourceLocation) = Holder.direct(BannerPattern(parse, "block.minecraft.banner.${parse.toShortLanguageKey()}"))

    fun putPattern(old: String, location: String) {
        val parse = ResourceLocation.parse(location)
        put(old, createPattern(parse))
    }

    putPattern("b", "minecraft:base")
    putPattern("bl", "minecraft:square_bottom_left")
    putPattern("br", "minecraft:square_bottom_right")
    putPattern("tl", "minecraft:square_top_left")
    putPattern("tr", "minecraft:square_top_right")
    putPattern("bs", "minecraft:stripe_bottom")
    putPattern("ts", "minecraft:stripe_top")
    putPattern("ls", "minecraft:stripe_left")
    putPattern("rs", "minecraft:stripe_right")
    putPattern("cs", "minecraft:stripe_center")
    putPattern("ms", "minecraft:stripe_middle")
    putPattern("drs", "minecraft:stripe_downright")
    putPattern("dls", "minecraft:stripe_downleft")
    putPattern("ss", "minecraft:small_stripes")
    putPattern("cr", "minecraft:cross")
    putPattern("sc", "minecraft:straight_cross")
    putPattern("bt", "minecraft:triangle_bottom")
    putPattern("tt", "minecraft:triangle_top")
    putPattern("bts", "minecraft:triangles_bottom")
    putPattern("tts", "minecraft:triangles_top")
    putPattern("ld", "minecraft:diagonal_left")
    putPattern("rd", "minecraft:diagonal_up_right")
    putPattern("lud", "minecraft:diagonal_up_left")
    putPattern("rud", "minecraft:diagonal_right")
    putPattern("mc", "minecraft:circle")
    putPattern("mr", "minecraft:rhombus")
    putPattern("vh", "minecraft:half_vertical")
    putPattern("hh", "minecraft:half_horizontal")
    putPattern("vhr", "minecraft:half_vertical_right")
    putPattern("hhb", "minecraft:half_horizontal_bottom")
    putPattern("bo", "minecraft:border")
    putPattern("cbo", "minecraft:curly_border")
    putPattern("gra", "minecraft:gradient")
    putPattern("gru", "minecraft:gradient_up")
    putPattern("bri", "minecraft:bricks")
    putPattern("glb", "minecraft:globe")
    putPattern("cre", "minecraft:creeper")
    putPattern("sku", "minecraft:skull")
    putPattern("flo", "minecraft:flower")
    putPattern("moj", "minecraft:mojang")
    putPattern("pig", "minecraft:piglin")
    defaultReturnValue(createPattern(ResourceLocation.withDefaultNamespace("base")))
}

const val TAG = "BlockEntityTag"

object BannerItemFixer : DataComponentFixer<BannerPatternLayers> {

    override fun getComponentType(): DataComponentType<BannerPatternLayers> = DataComponents.BANNER_PATTERNS

    override fun getData(compoundTag: CompoundTag): BannerPatternLayers? {
        val tag = compoundTag.getAndRemoveCompound(TAG) ?: return null
        val base = tag.getAndRemoveInt("Base") ?: 0

        val listTag = tag.getAndRemoveList("Patterns") ?: return null

        val layers = listTag.mapNotNull { it.asCompound().getOrNull() }.mapNotNull {
            val andRemoveString = it.getAndRemoveString("Pattern") ?: return@mapNotNull null
            val color = it.getAndRemoveInt("Color") ?: return@mapNotNull null
            Layer(PATTERN_MAP[andRemoveString]!!, fixBannerColor(color))
        }.toMutableList()
        layers.addFirst(Layer(PATTERN_MAP["b"]!!, fixBannerColor(base)))
        return BannerPatternLayers(layers)
    }

    private fun fixBannerColor(id: Int): DyeColor {
        return DyeColor.byName(ExtraDataFixUtils.dyeColorIdToName(15 - id), null)?: DyeColor.WHITE
    }

    override fun shouldApply(itemStack: ItemStack) = itemStack.`is`(ItemTags.BANNERS)
}
