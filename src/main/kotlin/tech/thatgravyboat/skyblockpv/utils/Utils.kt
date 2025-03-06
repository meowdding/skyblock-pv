package tech.thatgravyboat.skyblockpv.utils

import com.mojang.authlib.GameProfile
import com.mojang.blaze3d.vertex.PoseStack
import earth.terrarium.olympus.client.components.Widgets
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.layouts.FrameLayout
import net.minecraft.client.gui.layouts.LayoutElement
import net.minecraft.world.level.block.entity.SkullBlockEntity
import tech.thatgravyboat.skyblockpv.SkyBlockPv
import tech.thatgravyboat.skyblockpv.utils.displays.Displays
import tech.thatgravyboat.skyblockpv.utils.displays.asWidget
import java.text.DecimalFormat
import kotlin.jvm.optionals.getOrNull

object Utils {

    var isFetchingGameProfile = false
        private set

    fun PoseStack.translate(x: Int, y: Int, z: Int) {
        this.translate(x.toFloat(), y.toFloat(), z.toFloat())
    }

    inline fun GuiGraphics.scissor(x: Int, y: Int, width: Int, height: Int, action: () -> Unit) {
        this.enableScissor(x, y, x + width, y + height)
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
}
