package tech.thatgravyboat.skyblockpv.utils

import com.mojang.authlib.GameProfile
import com.mojang.blaze3d.vertex.PoseStack
import earth.terrarium.olympus.client.components.Widgets
import earth.terrarium.olympus.client.shader.builtin.RoundedRectShader
import kotlinx.coroutines.runBlocking
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.layouts.FrameLayout
import net.minecraft.client.gui.layouts.LayoutElement
import net.minecraft.world.level.block.entity.SkullBlockEntity
import tech.thatgravyboat.skyblockapi.utils.json.Json.readJson
import tech.thatgravyboat.skyblockpv.SkyBlockPv
import tech.thatgravyboat.skyblockpv.utils.displays.Displays
import tech.thatgravyboat.skyblockpv.utils.displays.asWidget
import java.text.DecimalFormat
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

    fun LayoutElement.centerVertically(height: Int) : LayoutElement {
        return FrameLayout(0, height).also { it.addChild(this) }
    }

    fun LayoutElement.centerHorizontally(width: Int) : LayoutElement {
        return FrameLayout(width, 0).also { it.addChild(this) }
    }

    fun LayoutElement.center(width: Int, height: Int): LayoutElement {
        return FrameLayout(width, height).also { it.addChild(this) }
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

    fun getTitleWidget(title: String, width: Int) = Widgets.frame { compoundWidget ->
        compoundWidget.withContents { contents ->
            contents.addChild(Displays.background(SkyBlockPv.id("box/title"), width - 10, 20).asWidget())
            contents.addChild(Widgets.text(title).centerHorizontally(width))
        }
        compoundWidget.withStretchToContentSize()
    }

    fun getMainContentWidget(content: LayoutElement, width: Int) = Widgets.frame { compoundWidget ->
        val contentWithSpacer = LayoutBuild.vertical {
            spacer(height = 7)
            widget(content)
            spacer(height = 7)
        }
        compoundWidget.withContents { contents ->
            contents.addChild(Displays.background(SkyBlockPv.id("box/box"), width - 10, contentWithSpacer.height).asWidget())
            contents.addChild(contentWithSpacer.centerHorizontally(width))
        }
        compoundWidget.withStretchToContentSize()
    }

    fun fetchGameProfile(username: String, callback: (GameProfile?) -> Unit) {
        if (isFetchingGameProfile) return
        isFetchingGameProfile = true
        SkullBlockEntity.fetchGameProfile(username)
            .thenAccept { profile ->
                callback(profile.getOrNull())
                isFetchingGameProfile = false
            }
    }

    fun String.toTitleCase() = lowercase().split("_").joinToString(" ") { it.replaceFirstChar { it.uppercase() } }

    fun Number.shorten(): String {
        val number = this.toLong()
        return when {
            number >= 1_000_000_000_000 -> String.format("%.1ft", number / 1_000_000_000_000.0)
            number >= 1_000_000_000 -> String.format("%.1fb", number / 1_000_000_000.0)
            number >= 1_000_000 -> String.format("%.1fm", number / 1_000_000.0)
            number >= 1_000 -> String.format("%.1fk", number / 1_000.0)
            else -> this.toString()
        }
    }

    fun Duration.formatReadableTime(biggestUnit: DurationUnit, maxUnits: Int = -1): String {
        val units = listOf(
            DurationUnit.DAYS to this.inWholeDays,
            DurationUnit.HOURS to this.inWholeHours % 24,
            DurationUnit.MINUTES to this.inWholeMinutes % 60,
            DurationUnit.SECONDS to this.inWholeSeconds % 60,
            DurationUnit.MILLISECONDS to this.inWholeMilliseconds % 1000,
        )

        val unitNames = mapOf(
            DurationUnit.DAYS to "d",
            DurationUnit.HOURS to "h",
            DurationUnit.MINUTES to "min",
            DurationUnit.SECONDS to "s",
            DurationUnit.MILLISECONDS to "ms",
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
}
