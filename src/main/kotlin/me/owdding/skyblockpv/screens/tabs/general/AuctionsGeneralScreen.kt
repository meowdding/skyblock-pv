package me.owdding.skyblockpv.screens.tabs.general

import com.mojang.authlib.GameProfile
import com.teamresourceful.resourcefullib.common.utils.TriState
import earth.terrarium.olympus.client.components.Widgets
import kotlinx.datetime.Instant
import me.owdding.lib.builder.LayoutFactory
import me.owdding.lib.displays.*
import me.owdding.lib.extensions.toReadableTime
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.api.AuctionAPI
import me.owdding.skyblockpv.api.CachedApi
import me.owdding.skyblockpv.api.PlayerDbAPI
import me.owdding.skyblockpv.api.data.SkyBlockProfile
import me.owdding.skyblockpv.data.auctions.AuctionData
import me.owdding.skyblockpv.data.auctions.AuctionEntry
import me.owdding.skyblockpv.utils.Utils.append
import me.owdding.skyblockpv.utils.components.PvWidgets
import me.owdding.skyblockpv.utils.displays.ExtraDisplays
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.world.item.Item
import net.minecraft.world.item.TooltipFlag
import tech.thatgravyboat.skyblockapi.helpers.McLevel
import tech.thatgravyboat.skyblockapi.utils.builders.TooltipBuilder
import tech.thatgravyboat.skyblockapi.utils.extentions.toFormattedString
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.strikethrough
import tech.thatgravyboat.skyblockapi.utils.time.until
import java.util.concurrent.CompletableFuture

private const val CONTENT_PADDING = 5
private const val PADDING = 10

class AuctionsGeneralScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) :
    BaseGeneralLoadingScreen<AuctionData>(gameProfile, profile) {

    override val api: CachedApi<SkyBlockProfile, AuctionData, *> get() = AuctionAPI

    private var currentAuction: AuctionEntry? = null
    private var currentAuctionDisplay: Display? = null

    override fun getLayout(bg: DisplayWidget): Layout {

        return loaded(
            LayoutFactory.vertical {},
            LayoutFactory.vertical {},
        ) {
            val side = bg.width / 3
            val middle = bg.width - side - PADDING * 3
            val height = bg.height - PADDING * 3

            LayoutFactory.horizontal(PADDING) {
                this.widget(
                    PvWidgets.label(
                        "Auctions",
                        Widgets.list { list ->
                            list.withSize(side - PADDING * 2, height - 34)
                            list.withContentMargin(CONTENT_PADDING)
                            list.withScrollableY(TriState.UNDEFINED)
                            list.withContents { contents ->
                                contents.withGap(CONTENT_PADDING)

                                val hasScrollbar = (it.auctions.size * 20) + (it.auctions.size - 1 * CONTENT_PADDING) > height - 14

                                it.auctions.forEach { entry ->
                                    val row = Displays.padding(
                                        2,
                                        Displays.fixed(
                                            side - PADDING * 2 - CONTENT_PADDING * 2 - 4 - if (hasScrollbar) 20 else 0,
                                            16,
                                            Displays.row(
                                                Displays.item(entry.item), Displays.text(entry.item.hoverName),
                                                alignment = Alignment.CENTER,
                                                spacing = CONTENT_PADDING,
                                            ),
                                        ),
                                    )
                                    contents.withChild(
                                        Displays.supplied {
                                            Displays.background(
                                                SkyBlockPv.id("box/box"),
                                                row,
                                                if (entry == currentAuction) TextColor.GREEN else TextColor.WHITE
                                            )
                                        }.withTooltip { this.add(entry) }.asButton { button ->
                                            button.withCallback {
                                                currentAuction = entry
                                                currentAuctionDisplay = createAuctionsDisplay(entry)
                                            }
                                        },
                                    )
                                }
                            }
                        },
                        0,
                    ),
                )

                this.vertical(PADDING) {

                    val middleHeight = height * 0.7 - PADDING

                    this.display(
                        Displays.background(
                            SkyBlockPv.id("box/box"),
                            Displays.center(
                                width = middle,
                                height = middleHeight.toInt(),
                                Displays.supplied {
                                    currentAuctionDisplay?.let { display ->
                                        Displays.fixed(middle - CONTENT_PADDING * 2, middleHeight.toInt() - CONTENT_PADDING * 2, display)
                                    } ?: Displays.text("No auction selected")
                                },
                            ),
                        ),
                    )

                    this.display(
                        Displays.background(SkyBlockPv.id("box/box"), middle, height - middleHeight.toInt() - PADDING),
                    )

                }
            }
        }


    }

    private fun createAuctionsDisplay(auction: AuctionEntry): Display {
        val auctioneer = auction.auctioneer?.let { PlayerDbAPI.getProfileAsync(it) } ?: CompletableFuture.failedFuture(IllegalStateException())
        return Displays.column(
            ExtraDisplays.scaledText(auction.item.hoverName, 1.5f, true),
            Displays.text({ "by ${auctioneer.takeIf { it.isDone }?.get()?.name ?: "Loading..."}" }),
        )
    }

    private fun TooltipBuilder.add(auction: AuctionEntry): TooltipBuilder {
        auction.item.getTooltipLines(Item.TooltipContext.of(McLevel.self), null, TooltipFlag.NORMAL).forEach(this::add)

        this.add("                    ") {
            this.strikethrough = true
            this.color = TextColor.DARK_GRAY
        }

        if (auction.bin) {
            this.add("Buy it now: ") {
                this.color = TextColor.GRAY
                this.append("${auction.startingBid.toFormattedString()} coins") { this.color = TextColor.GOLD }
            }
        } else if (auction.bid != 0L) {
            this.add("Top bid: ") {
                this.color = TextColor.GRAY
                this.append("${auction.bid.toFormattedString()} coins") { this.color = TextColor.GOLD }
            }
        } else {
            this.add("Starting bid: ") {
                this.color = TextColor.GRAY
                this.append("${auction.startingBid.toFormattedString()} coins") { this.color = TextColor.GOLD }
            }
        }

        this.space()
        this.add("Ends in: ") {
            this.color = TextColor.GRAY

            this.append(Instant.fromEpochMilliseconds(auction.end).until().toReadableTime()) {
                this.color = TextColor.YELLOW
            }
        }
        this.space()
        this.add("Click to inspect!") { this.color = TextColor.YELLOW }

        return this
    }

}

