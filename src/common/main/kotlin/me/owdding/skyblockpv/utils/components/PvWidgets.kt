package me.owdding.skyblockpv.utils.components

import com.mojang.blaze3d.platform.InputConstants
import earth.terrarium.olympus.client.components.Widgets
import earth.terrarium.olympus.client.components.base.renderer.WidgetRenderer
import earth.terrarium.olympus.client.components.buttons.Button
import earth.terrarium.olympus.client.components.renderers.WidgetRenderers
import earth.terrarium.olympus.client.components.string.TextWidget
import earth.terrarium.olympus.client.utils.Orientation
import kotlinx.coroutines.runBlocking
import me.owdding.lib.displays.*
import me.owdding.lib.extensions.floor
import me.owdding.lib.extensions.rightPad
import me.owdding.lib.utils.keys
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.api.StatusAPI
import me.owdding.skyblockpv.api.data.PlayerStatus
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.api.predicates.ItemPredicateHelper
import me.owdding.skyblockpv.api.predicates.ItemPredicates
import me.owdding.skyblockpv.screens.BasePvScreen
import me.owdding.skyblockpv.screens.windowed.elements.ExtraConstants
import me.owdding.skyblockpv.utils.FakePlayer
import me.owdding.skyblockpv.utils.LayoutUtils.centerHorizontally
import me.owdding.skyblockpv.utils.Utils.plus
import me.owdding.skyblockpv.utils.Utils.unaryPlus
import me.owdding.skyblockpv.utils.displays.ExtraDisplays
import me.owdding.skyblockpv.utils.displays.ExtraDisplays.asTable
import me.owdding.skyblockpv.utils.theme.PvColors
import me.owdding.skyblockpv.utils.theme.ThemeSupport
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.layouts.FrameLayout
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.client.gui.layouts.LayoutElement
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.ItemLike
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import tech.thatgravyboat.skyblockapi.api.location.SkyBlockIsland
import tech.thatgravyboat.skyblockapi.platform.id
import tech.thatgravyboat.skyblockapi.platform.name
import tech.thatgravyboat.skyblockapi.platform.pushPop
import tech.thatgravyboat.skyblockapi.utils.text.Text

object PvWidgets {


    fun iconNumberElement(icon: ItemStack, text: Component) = listOf(
        Displays.item(icon, 12, 12),
        Displays.padding(0, 0, 2, 0, ExtraDisplays.text(text, shadow = false)),
    ).toRow(1).let { Displays.background(ThemeSupport.texture(SkyBlockPv.id("box/rounded_box_thin")), Displays.padding(2, it)) }

    fun iconNumberElement(icon: ItemLike, text: Component) = iconNumberElement(icon.asItem().defaultInstance, text)
    fun label(title: String, element: LayoutElement, padding: Int = 0, width: Int = element.width + padding + 20, icon: ResourceLocation? = null) =
        label(Text.of(title), element, padding, width, icon)

    fun label(title: Component, element: LayoutElement, padding: Int = 0, width: Int = element.width + padding + 20, icon: ResourceLocation? = null) =
        PvLayouts.vertical {
            widget(getTitleWidget(title, width, icon))
            widget(getMainContentWidget(element, width))
        }

    fun tools(
        profile: SkyBlockProfile,
        score: (ItemStack) -> Int,
        tools: List<String>,
        emptyIcon: String,
        maxAmount: Int = 4,
    ): LayoutElement {
        val toolsToDisplay = (ItemPredicateHelper.getItemsMatching(
            profile,
            ItemPredicates.AnySkyblockID(tools),
        )?.sortedByDescending(score) ?: emptyList()).take(maxAmount).toMutableList()

        while (toolsToDisplay.size < maxAmount) {
            toolsToDisplay.add(Items.AIR.defaultInstance)
        }

        val column = toolsToDisplay.map { tool ->
            Displays.item(tool, showTooltip = true).let { display ->
                // TODO: hover over empty slot
                display.takeUnless { tool.isEmpty } ?: Displays.background(ThemeSupport.texture(SkyBlockPv.id(emptyIcon)), display)
            }.let { Displays.padding(2, it) }
        }.toColumn()

        return ExtraDisplays.inventoryBackground(
            maxAmount, Orientation.VERTICAL,
            Displays.padding(2, column),
        ).asWidget()
    }

    fun armorAndEquipment(
        profile: SkyBlockProfile,
        score: (ItemStack) -> Int,
        necklaces: List<String>,
        cloaks: List<String>,
        belts: List<String>,
        gloves: List<String>,
        armor: List<String>,
    ): LayoutElement {
        val armorAndEquipment = ItemPredicateHelper.getItemsMatching(
            profile,
            ItemPredicates.AnySkyblockID(armor).or(ItemPredicates.AnySkyblockID(listOf(cloaks, necklaces, belts, gloves).flatten())),
        )?.sortedBy(score)?.reversed() ?: emptyList()

        val displayEquipment = buildList {
            fun addEquipment(list: List<String>, name: String) {
                val item = armorAndEquipment.firstOrNull { it.getData(DataTypes.ID)?.let { id -> list.contains(id) } == true } ?: ItemStack.EMPTY
                val display = if (item.isEmpty) {
                    Displays.background(ThemeSupport.texture(SkyBlockPv.id("icon/slot/${name.lowercase()}")), Displays.empty(16, 16))
                } else {
                    Displays.item(item, showTooltip = true)
                }

                add(Displays.padding(2, display))
            }

            addEquipment(necklaces, "necklace")
            addEquipment(cloaks, "cloak")
            addEquipment(belts, "belt")
            addEquipment(gloves, "glove")
        }

        val armorEquipment = listOf(
            armorDisplay(armorAndEquipment),
            displayEquipment.toColumn(),
        ).toRow()
        return ExtraDisplays.inventoryBackground(
            2, 4,
            Displays.padding(2, armorEquipment),
        ).asWidget()
    }

    private val ARMOR_BACKGROUND_IDS = listOf<ResourceLocation>(
        ResourceLocation.withDefaultNamespace("container/slot/helmet"),
        ResourceLocation.withDefaultNamespace("container/slot/chestplate"),
        ResourceLocation.withDefaultNamespace("container/slot/leggings"),
        ResourceLocation.withDefaultNamespace("container/slot/boots"),
    )

    fun orderedArmorDisplay(list: List<ItemStack>): Display {
        val armor = list.take(4).toMutableList().rightPad(4, ItemStack.EMPTY)

        return armor.mapIndexed { index, item ->
            if (item.isEmpty) {
                Displays.background(ThemeSupport.texture(ARMOR_BACKGROUND_IDS[index]), 16, 16)
            } else {
                Displays.item(item, showTooltip = true)
            }
        }.map { Displays.padding(2, it) }.toColumn()
    }

    private val EQUIPMENT_BACKGROUND_IDS = listOf(
        SkyBlockPv.id("icon/slot/necklace"),
        SkyBlockPv.id("icon/slot/cloak"),
        SkyBlockPv.id("icon/slot/belt"),
        SkyBlockPv.id("icon/slot/glove"),
    )

    fun orderedEquipmentDisplay(list: List<ItemStack>): Display {
        val armor = list.take(4).toMutableList().rightPad(4, ItemStack.EMPTY)

        return armor.mapIndexed { index, item ->
            if (item.isEmpty) {
                Displays.background(ThemeSupport.texture(EQUIPMENT_BACKGROUND_IDS[index]), 16, 16)
            } else {
                Displays.item(item, showTooltip = true)
            }
        }.map { Displays.padding(2, it) }.toColumn()
    }

    fun armorDisplay(list: List<ItemStack>) = Displays.column(
        *buildList {
            fun addArmor(type: String) {
                val itemStack = list.firstOrNull { it.getData(DataTypes.ID)?.contains(type) != false } ?: ItemStack.EMPTY
                Displays.padding(
                    2,
                    Displays.item(itemStack, showTooltip = true).let {
                        it.takeUnless { itemStack.isEmpty } ?: Displays.background(
                            ThemeSupport.texture(ResourceLocation.parse("container/slot/${type.lowercase()}")),
                            it,
                        )
                    },
                ).let { add(it) }
            }

            addArmor("HELMET")
            addArmor("CHESTPLATE")
            addArmor("LEGGINGS")
            addArmor("BOOTS")
        }.toTypedArray(),
    )

    fun text(text: Component): TextWidget = Widgets.text(text).withColor(PvColors.DARK_GRAY_COLOR)
    fun text(text: String): TextWidget = Widgets.text(text).withColor(PvColors.DARK_GRAY_COLOR)

    fun getTitleWidget(title: String, width: Int, icon: ResourceLocation? = null) = getTitleWidget(Text.of(title), width, icon)
    fun getTitleWidget(title: Component, width: Int, icon: ResourceLocation? = null): LayoutElement = Widgets.frame { compoundWidget ->
        compoundWidget.withContents { contents ->
            contents.addChild(Displays.background(ThemeSupport.texture(SkyBlockPv.id("box/title")), width - 10, 20).asWidget())
            if (icon != null) contents.addChild(Displays.padding(0, width - 30, 0, 0, Displays.sprite(ThemeSupport.texture(icon), 12, 12)).asWidget())
            contents.addChild(text(title).centerHorizontally(width))
        }
        compoundWidget.withStretchToContentSize()
    }

    fun getMainContentWidget(content: LayoutElement, width: Int): LayoutElement = Widgets.frame { compoundWidget ->
        val contentWithSpacer = PvLayouts.vertical {
            spacer(height = 7)
            widget(content)
            spacer(height = 7)
        }
        compoundWidget.withContents { contents ->
            contents.addChild(Displays.background(ThemeSupport.texture(SkyBlockPv.id("box/box")), width - 10, contentWithSpacer.height).asWidget())
            contents.addChild(contentWithSpacer.centerHorizontally(width))
        }
        compoundWidget.withStretchToContentSize()
    }

    fun createInventory(items: List<ItemStack>): Display {
        val itemDisplays = items.chunked(9).map { chunk ->
            val updatedChunk = chunk + List(9 - chunk.size) { ItemStack.EMPTY }
            updatedChunk.map { item ->
                Displays.padding(2, Displays.item(item, showTooltip = true, showStackSize = true))
            }
        }
        return ExtraDisplays.inventoryBackground(
            9, itemDisplays.size,
            Displays.padding(2, itemDisplays.asTable()),
        )
    }

    private val rightClick = keys {
        withButton(InputConstants.MOUSE_BUTTON_RIGHT)
    }
    private var cachedX = 0.0F
    private var cachedY = 0.0F

    fun BasePvScreen.getPlayerWidget(width: Int): AbstractWidget {
        val height = (width * 1.1).toInt()
        val armor = (if (isProfileInitialized()) profile.inventory?.armorItems?.inventory else null) ?: List(4) { ItemStack.EMPTY }
        val name = if (isProfileInitialized()) {
            val skyblockLvl = profile.skyBlockLevel.first
            val skyblockLvlColor = tech.thatgravyboat.skyblockapi.api.profile.profile.ProfileAPI.getLevelColor(skyblockLvl)
            Text.join("§8[", Text.of("$skyblockLvl").withColor(skyblockLvlColor), "§8] §f", gameProfile.name)
        } else Text.of(gameProfile.name)
        val fakePlayer = FakePlayer(gameProfile, name, armor)
        val nakedFakePlayer = FakePlayer(gameProfile, name)
        return Displays.background(ThemeSupport.texture(SkyBlockPv.id("buttons/disabled")), width, height).asWidget().withRenderer { gr, ctx, _ ->
            val isHovered = ctx.mouseX in ctx.x..(ctx.x + width) && ctx.mouseY in ctx.y..(ctx.y + height)
            val eyesX = (ctx.mouseX - ctx.x).toFloat().takeIf { ctx.mouseX >= 0 }?.also { cachedX = it } ?: cachedX
            val eyesY = (ctx.mouseY - ctx.y).toFloat().takeIf { ctx.mouseY >= 0 }?.also { cachedY = it } ?: cachedY
            gr.pushPop {
                Displays.entity(
                    if (rightClick.isDown() && isHovered) {
                        nakedFakePlayer
                    } else {
                        fakePlayer
                    },
                    width,
                    height,
                    (width / 3f).floor(),
                    eyesX, eyesY,
                ).render(gr, ctx.x, ctx.y + height / 10)
            }
        }
    }

    fun BasePvScreen.getStatusButton(width: Int): Button {
        val status = StatusAPI.getCached(gameProfile.id)

        fun getStatusDisplay(status: PlayerStatus): WidgetRenderer<Button> {
            val statusText = +"screens.main.status.${status.status.name.lowercase()}"
            val location = SkyBlockIsland.entries.find { it.id == status.location }?.toString() ?: status.location
            val locationText = location?.let { Text.of(it).withColor(PvColors.GREEN) } ?: +"screens.main.status.unknown"
            return WidgetRenderers.text(statusText + locationText)
        }

        return Button().also { button ->
            button.withTexture(ExtraConstants.BUTTON_DARK)
            button.withRenderer(WidgetRenderers.text(+"screens.main.status.load"))
            button.withCallback {
                button.withRenderer(WidgetRenderers.text(+"screens.main.status.loading"))
                runBlocking {
                    val status = StatusAPI.getData(gameProfile.id).getOrNull()
                    if (status == null) {
                        button.withRenderer(WidgetRenderers.text(+"screens.main.status.error"))
                    } else {
                        button.withRenderer(getStatusDisplay(status))
                        button.asDisabled()
                    }
                }
            }
            button.withSize(width, 20)

            if (status != null) {
                button.withRenderer(getStatusDisplay(status))
                button.asDisabled()
            }
        }
    }

    fun Layout.centerIn(x: Int, y: Int, width: Int, height: Int) = apply { FrameLayout.centerInRectangle(this, 0, 0, this.width, this.height) }
}
