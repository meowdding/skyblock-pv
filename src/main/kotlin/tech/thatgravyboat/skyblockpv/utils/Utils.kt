package tech.thatgravyboat.skyblockpv.utils

import com.mojang.authlib.GameProfile
import com.mojang.blaze3d.vertex.PoseStack
import earth.terrarium.olympus.client.components.base.ListWidget
import earth.terrarium.olympus.client.components.compound.LayoutWidget
import earth.terrarium.olympus.client.shader.builtin.RoundedRectShader
import kotlinx.coroutines.runBlocking
import net.minecraft.Util
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.layouts.FrameLayout
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.client.gui.layouts.LayoutElement
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.entity.SkullBlockEntity
import tech.thatgravyboat.skyblockapi.utils.json.Json.readJson
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockpv.api.PlayerDbAPI
import tech.thatgravyboat.skyblockpv.utils.components.PvWidgets
import java.text.DecimalFormat
import java.text.NumberFormat
import kotlin.jvm.optionals.getOrNull
import kotlin.time.Duration
import kotlin.time.DurationUnit

object Utils {

    var isFetchingGameProfile = false
        private set

    fun GuiGraphics.drawRoundedRec(
        x: Int, y: Int, width: Int, height: Int,
        backgroundColor: Int, borderColor: Int = backgroundColor,
        borderSize: Int = 0, radius: Int = 0,
    ) {
        this.flush()

        val xOffset = this.pose().last().pose().m30()
        val yOffset = this.pose().last().pose().m31()
        pushPop {
            translate(-xOffset, -yOffset, 0f)
            RoundedRectShader.fill(
                this@drawRoundedRec, (x + xOffset).toInt(), (y + yOffset).toInt(), width, height,
                backgroundColor, borderColor, width.coerceAtMost(height) * (radius / 100f), borderSize,
            )
        }
    }

    fun PoseStack.translate(x: Int, y: Int, z: Int) {
        this.translate(x.toFloat(), y.toFloat(), z.toFloat())
    }

    inline fun GuiGraphics.scissor(x: Int, y: Int, width: Int, height: Int, action: () -> Unit) {
        this.enableScissor(x, y, x + width, y + height)
        action()
        this.disableScissor()
    }

    inline fun GuiGraphics.scissorRange(x: IntRange, y: IntRange, action: () -> Unit) {
        this.enableScissor(x.start, y.start, x.endInclusive, y.endInclusive)
        action()
        this.disableScissor()
    }

    inline fun GuiGraphics.pushPop(action: PoseStack.() -> Unit) {
        this.pose().pushPop(action)
    }

    inline fun PoseStack.pushPop(action: PoseStack.() -> Unit) {
        this.pushPose()
        this.action()
        this.popPose()
    }

    fun Number.round(): String = DecimalFormat("#.##").format(this)

    fun LayoutElement.centerVertically(height: Int): LayoutElement {
        return FrameLayout(0, height).also { it.addChild(this) }
    }

    fun LayoutElement.centerHorizontally(width: Int): LayoutElement {
        return FrameLayout(width, 0).also { it.addChild(this) }
    }

    fun LayoutElement.center(width: Int, height: Int): LayoutElement {
        return FrameLayout(width, height).also { it.addChild(this) }
    }

    fun Layout.asScrollable(width: Int, height: Int, init: ListWidget.() -> Unit = {}): Layout {
        this.arrangeElements()
        val widget = LayoutWidget(this).also { it.visible = true }.withStretchToContentSize()
        val scrollable = ListWidget((widget.width + 20).coerceAtMost(width), widget.height.coerceAtMost(height - 20))

        scrollable.add(widget)
        scrollable.init()

        return LayoutBuild.frame(width - 20, height) {
            widget(scrollable)
        }
    }

    fun ListWidget.withScrollToBottom() {
        this.mouseScrolled(0.0, 0.0, 0.0, -this.height.toDouble())
    }

    fun <T> List<List<T>>.transpose(): List<List<T>> {
        val list = mutableListOf<MutableList<T>>()
        for (x in indices) {
            for (y in this[x].indices) {
                if (x == 0) {
                    list.add(mutableListOf())
                }
                list[y].add(this[x][y])
            }
        }
        return list
    }

    @Deprecated("Use PvWidgets.getTitleWidget instead", ReplaceWith("PvWidgets.getTitleWidget(title, width, icon)"))
    fun getTitleWidget(title: String, width: Int, icon: ResourceLocation? = null) = PvWidgets.getTitleWidget(title, width, icon)

    @Deprecated("Use PvWidgets.getMainContentWidget instead", ReplaceWith("PvWidgets.getMainContentWidget(content, width)"))
    fun getMainContentWidget(content: LayoutElement, width: Int) = PvWidgets.getMainContentWidget(content, width)


    fun fetchGameProfile(username: String, callback: (GameProfile?) -> Unit) {
        if (isFetchingGameProfile) return
        isFetchingGameProfile = true
        PlayerDbAPI.getUUID(username).takeIf { it != Util.NIL_UUID }?.let {
            callback(it)
            isFetchingGameProfile = false
        }
            ?: SkullBlockEntity.fetchGameProfile(username).thenAccept { profile ->
                callback(profile.getOrNull())
                isFetchingGameProfile = false
            }
    }

    fun String.toTitleCase() = lowercase().split("_").joinToString(" ") { it.replaceFirstChar { it.uppercase() } }

    private val formatter = NumberFormat.getCompactNumberInstance()
    fun Number.shorten(decimalDigits: Int = 1): String = formatter.apply { maximumFractionDigits = decimalDigits }.format(this)

    fun Duration.formatReadableTime(biggestUnit: DurationUnit, maxUnits: Int = -1): String {
        val units = listOf(
            DurationUnit.DAYS to this.inWholeDays,
            DurationUnit.HOURS to this.inWholeHours % 24,
            DurationUnit.MINUTES to this.inWholeMinutes % 60,
            DurationUnit.SECONDS to this.inWholeSeconds % 60,
            //DurationUnit.MILLISECONDS to this.inWholeMilliseconds % 1000,
        )

        val unitNames = mapOf(
            DurationUnit.DAYS to "d",
            DurationUnit.HOURS to "h",
            DurationUnit.MINUTES to "min",
            DurationUnit.SECONDS to "s",
            //DurationUnit.MILLISECONDS to "ms",
        )

        val filteredUnits = units.dropWhile { it.first != biggestUnit }
            .filter { it.second > 0 }
            .take(maxUnits)

        return filteredUnits.joinToString(", ") { (unit, value) ->
            "$value${unitNames[unit]}"
        }.ifEmpty { "0 seconds" }
    }

    inline fun <reified T : Any> loadFromRepo(file: String) = runBlocking {
        try {
            this.javaClass.getResourceAsStream("/repo/$file.json")?.readJson<T>() ?: return@runBlocking null
        } catch (e: Exception) {
            println(e)
            null
        }
    }

    fun text(
        text: String = "",
        color: UInt = 0x555555u,
        init: MutableComponent.() -> Unit = {},
    ): MutableComponent {
        return Text.of(text) {
            withColor(color.toInt())
            init(this)
        }
    }

    fun whiteText(text: String = "", init: MutableComponent.() -> Unit = {}) = text(text, 0xFFFFFFu, init)

    fun <T> MutableList<T>.rightPad(size: Int, element: T): MutableList<T> {
        while (this.size < size) {
            this.add(this.lastIndex + 1, element)
        }
        return this
    }
    fun MutableComponent.append(text: String, init: MutableComponent.() -> Unit): MutableComponent = this.append(Text.of(text, init))
}
