package me.owdding.skyblockpv.screens.windowed.tabs.base

import com.mojang.blaze3d.platform.InputConstants
import earth.terrarium.olympus.client.components.Widgets
import earth.terrarium.olympus.client.components.renderers.WidgetRenderers
import earth.terrarium.olympus.client.constants.MinecraftColors
import earth.terrarium.olympus.client.utils.ListenableState
import me.owdding.lib.displays.Alignment
import me.owdding.lib.displays.Display
import me.owdding.lib.displays.DisplayWidget
import me.owdding.lib.displays.Displays
import me.owdding.lib.displays.asWidget
import me.owdding.lib.displays.toColumn
import me.owdding.lib.displays.toRow
import me.owdding.lib.layouts.withPadding
import me.owdding.skyblockpv.screens.windowed.elements.ExtraConstants
import me.owdding.skyblockpv.utils.GroupedPageState
import me.owdding.skyblockpv.utils.LayoutUtils.asScrollable
import me.owdding.skyblockpv.utils.PvPageState
import me.owdding.skyblockpv.utils.Utils
import me.owdding.skyblockpv.utils.components.PvLayouts
import me.owdding.skyblockpv.utils.displays.ExtraDisplays
import me.owdding.skyblockpv.utils.theme.PvColors
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.network.chat.CommonComponents
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

interface GroupedScreen<FilterType : Any, GroupType : Any, DataType : Any> {

    var query: String?
    var filter: FilterType
    val noMatchFoundText: String

    fun FilterType.doesDisplay(data: DataType): Boolean
    fun filterEntries(): Collection<FilterType>
    fun FilterType.display(): String

    fun matchesSearch(data: DataType): Boolean
    val DataType.group: GroupType?
    fun GroupType.compareTo(other: GroupType): Int
    fun getData(): List<DataType>
    val comparator: Comparator<DataType> get() = Comparator { _, _ -> 0 }

    fun getColor(group: GroupType?, data: DataType): Int
    fun toDisplay(group: GroupType?, data: DataType): Display

    fun toTabState(): PvPageState

    fun createLayout(bg: DisplayWidget): Layout {
        val uiWidth = bg.width
        val uiHeight = bg.height

        val map = getData()

        val frame = Widgets.frame().withSize(uiWidth, uiHeight - 50)
        fun update() {
            frame.withContents {
                it.children.clear()
                val display = map.filter { data ->
                    matchesSearch(data) && filter.doesDisplay(data)
                }.takeUnless { it.isEmpty() }?.groupBy { runCatching { it.group }.getOrNull() }
                    ?.toSortedMap { o1, o2 ->
                        when {
                            o1 == null && o2 == null -> 0
                            o1 == null -> 1
                            o2 == null -> -1
                            else -> o1.compareTo(o2)
                        }
                    }?.map { (group, attributes) ->
                        attributes.sortedWith(comparator).chunked(uiWidth / 24).map {
                            it.map { data ->
                                ExtraDisplays.inventorySlot(
                                    Displays.padding(2, toDisplay(group, data)),
                                    getColor(group, data),
                                )
                            }.toRow(0, Alignment.CENTER)
                        }.toColumn(0, Alignment.CENTER)
                    }?.toColumn(4, Alignment.CENTER)
                    ?.let { display ->
                        PvLayouts.frame(uiWidth, 0) {
                            widget(display.asWidget()) {
                                alignHorizontallyCenter()
                                alignVerticallyMiddle()
                            }
                        }.asScrollable(uiWidth, uiHeight - 50)
                    }

                if (display != null) {
                    it.addChild(display)
                } else {
                    it.addChild(PvLayouts.frame(uiWidth, uiHeight - 50) {
                        widget(Widgets.text(noMatchFoundText).withColor(MinecraftColors.RED)) {
                            alignHorizontallyCenter()
                            alignVerticallyMiddle()
                        }
                    })
                }
            }
            Utils.lastTab = GroupedPageState(
                toTabState(),
                filter,
                query
            )
        }

        update()

        return PvLayouts.frame(uiWidth, uiHeight) {
            PvLayouts.horizontal(spacing = 5) {
                val input = Widgets.textInput(ListenableState.of(query ?: "")) { box ->
                    box.withChangeCallback {
                        query = it.takeUnless(String::isEmpty)
                        update()
                    }
                }
                input.withPlaceholder("Search")
                input.withSize(100, 20)
                input.withTexture(ExtraConstants.TEXTBOX)
                widget(input)

                button {

                    fun updateTooltip() {
                        withTooltip(
                            Text.multiline(
                                Text.of("Click to cycle through the filters!", TextColor.GRAY),
                                Text.of("Left Click / Right Click", TextColor.DARK_GRAY),
                                CommonComponents.EMPTY,
                                filterEntries().map {
                                    Text.of {
                                        if (filter == it) {
                                            append("> ", TextColor.GRAY)
                                        } else {
                                            append(" ")
                                        }

                                        append(it.display(), if (filter == it) TextColor.GREEN else TextColor.RED)
                                    }
                                },
                            ),
                        )
                    }

                    fun advance(amount: Int): () -> Unit = {
                        val entries = filterEntries().toList()
                        val max = entries.size
                        filter = entries[(entries.indexOf(filter) + max + amount) % max]
                        updateTooltip()
                        update()
                    }
                    updateTooltip()
                    setTooltipDelay((-1).seconds.toJavaDuration())
                    withTexture(ExtraConstants.BUTTON_DARK)
                    withRenderer(WidgetRenderers.text(Text.of("Filter", PvColors.WHITE)))

                    withCallback(InputConstants.MOUSE_BUTTON_LEFT, advance(1))
                    withCallback(InputConstants.MOUSE_BUTTON_RIGHT, advance(-1))
                    width = 100
                    height = 20
                }
            }.withPadding(paddingTop = 20, 0, 0, 0).add {
                alignVerticallyTop()
                alignHorizontallyCenter()
            }

            widget(frame) {
                alignVerticallyBottom()
                alignHorizontallyCenter()
                paddingBottom(5)
            }
        }
    }
}
