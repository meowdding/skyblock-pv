package me.owdding.skyblockpv.screens.windowed.tabs.foraging

import com.mojang.authlib.GameProfile
import me.owdding.lib.displays.Alignment
import me.owdding.lib.displays.Display
import me.owdding.lib.displays.DisplayWidget
import me.owdding.lib.displays.Displays
import me.owdding.lib.displays.toColumn
import me.owdding.lib.displays.toRow
import me.owdding.lib.displays.withTooltip
import me.owdding.lib.extensions.ListMerger
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.data.api.Attribute
import me.owdding.skyblockpv.data.repo.AttributesData
import me.owdding.skyblockpv.utils.LayoutUtils.asScrollable
import me.owdding.skyblockpv.utils.Utils.toDateTime
import me.owdding.skyblockpv.utils.components.PvLayouts
import me.owdding.skyblockpv.utils.displays.ExtraDisplays
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.util.ARGB
import net.minecraft.world.item.Items
import tech.thatgravyboat.repolib.api.AttributesAPI
import tech.thatgravyboat.repolib.api.RepoAPI
import tech.thatgravyboat.skyblockapi.api.data.SkyBlockRarity
import tech.thatgravyboat.skyblockapi.api.remote.api.RepoAttributeAPI
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.extentions.stripColor
import tech.thatgravyboat.skyblockapi.utils.text.TextBuilder.append
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.strikethrough
import kotlin.math.absoluteValue
import kotlin.math.min

class AttributeScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseForagingScreen(gameProfile, profile) {
    override val type: ForagingCategory = ForagingCategory.ATTRIBUTES

    override fun getLayout(bg: DisplayWidget): Layout {
        val attributes = profile.attributeData.data.associateBy { it.id.lowercase() }

        val repoData = RepoAPI.attributes().attributes().values.associateBy { it.id().lowercase() }

        val map = buildList {
            addAll(repoData.values.map { it to attributes[it.id().lowercase()] })
            addAll(attributes.filterNot { (key) -> repoData.containsKey(key.lowercase()) }.map { (_, value) -> null to value })
        }

        return map
            .groupBy { runCatching { SkyBlockRarity.valueOf(it.first?.rarity!!) }.getOrNull() }
            .toSortedMap { o1, o2 ->
                when {
                    o1 == null && o2 == null -> 0
                    o1 == null -> 1
                    o2 == null -> -1
                    else -> o1.ordinal - o2.ordinal
                }
            }
            .map { (rarity, attributes) ->
                attributes.sortedWith(
                    Comparator.comparingInt { (repo, _) ->
                        repo?.attributeId?.filter { c -> c.isDigit() }?.toIntOrNull() ?: 1000
                    },
                ).chunked(uiWidth / 24).map {
                    it.map { (repo, api) ->
                        ExtraDisplays.inventorySlot(
                            Displays.padding(2, toDisplay(rarity, repo, api)),
                            getColor(rarity, repo, api),
                        )
                    }.toRow(0, Alignment.CENTER)
                }.toColumn(0, Alignment.CENTER)
            }.toColumn(4, Alignment.CENTER)
            .let { PvLayouts.frame { display(it) }.asScrollable(uiWidth, uiHeight) }

    }

    fun getMax(repo: AttributesAPI.Attribute) = when (repo.id.lowercase()) {
        "reptiloid" -> 0
        else -> repo.max
    }

    fun getColor(rarity: SkyBlockRarity?, repo: AttributesAPI.Attribute?, api: Attribute?): Int {
        val baseColor = rarity?.color ?: -1

        if (api == null) return ARGB.scaleRGB(baseColor, 0.65f)

        if (repo == null || api.syphoned.absoluteValue >= getMax(repo)) return baseColor

        return ARGB.scaleRGB(baseColor, 0.75f + (0.25f * (api.syphoned / repo.max.toFloat())))
    }

    private fun toDisplay(
        rarity: SkyBlockRarity?,
        repo: AttributesAPI.Attribute?,
        api: Attribute?,
    ): Display {
        val item = repo?.id()?.let { RepoAttributeAPI.getAttributeByIdOrNull(it) }
        return Displays.item(
            when {
                repo?.let(::getMax) != 0 && (api == null || api.syphoned <= 0) -> Items.GRAY_DYE.defaultInstance
                else -> item ?: Items.BARRIER.defaultInstance
            },
        ).withTooltip {
            val max = repo?.let(::getMax) ?: -1

            if (repo == null) {
                add("Unknown shard!")
            } else {
                add {
                    append(repo.name)
                    append(" (")
                    append(repo.shardName)
                    append(")")

                    color = rarity?.color ?: -1
                }
                ListMerger(repo.lore).apply {
                    addUntil { it.stripColor().startsWith("You can Syphon this shard") }
                    while (index + 1 < original.size && !peek().endsWith("SHARD")) read()

                    addRemaining()
                }.destination.forEach(::add)
            }

            val capturedAt = api?.capturedAt?.takeUnless { it == 0L }
            val owned = api?.owned ?: 0
            val syphoned = api?.syphoned ?: 0

            space()

            add {
                color = TextColor.GRAY
                append("Owned: ")
                append(owned.toString(), TextColor.YELLOW)
            }

            if (owned != 0) {
                add {
                    color = TextColor.GRAY
                    append("Last Captured At: ")
                    if (capturedAt == null) {
                        append("Unknown", TextColor.RED)
                    } else {
                        append(capturedAt.toDateTime(), TextColor.GREEN)
                    }
                }
            }

            when (max) {
                0 -> add("Can't be syphoned!", TextColor.RED)
                else if rarity == null -> add {
                    color = TextColor.GRAY
                    append("Syphoned: ")
                    append(syphoned.toString(), TextColor.YELLOW)
                    append("/")
                    append("?", TextColor.RED)
                }

                else -> {
                    val level = rarity.getLevel(syphoned)
                    val max = rarity.getMax()
                    add {
                        color = TextColor.GRAY
                        append("Syphoned: ")
                        append(syphoned.toString(), when (syphoned) {
                            0 -> TextColor.RED
                            max -> TextColor.GREEN
                            else -> TextColor.YELLOW
                        })
                        append("/")
                        append(max.toString(), TextColor.GREEN)
                    }
                    add {
                        color = TextColor.GRAY
                        append("Level: ")
                        append(level.toString(), when (level) {
                            0 -> TextColor.RED
                            10 -> TextColor.GREEN
                            else -> TextColor.YELLOW
                        })
                        append("/")
                        append("10", TextColor.GREEN)
                    }

                    val total = 40
                    val used = min(level * 4, total)
                    val unused = total - used
                    add {
                        append(" ".repeat(used), TextColor.GREEN)
                        append(" ".repeat(unused), TextColor.GRAY)
                        strikethrough = true
                    }
                }
            }

            if (McClient.options.advancedItemTooltips && repo != null) {
                space()
                add("${repo.id.lowercase()} - ${repo.attributeId} (${repo.shardId.lowercase()})", TextColor.DARK_GRAY)
            }
        }
    }

    private fun SkyBlockRarity.getLevel(syphoned: Int) = AttributesData[this].indexOfLast { it <= syphoned }
    private fun SkyBlockRarity.getMax() = AttributesData[this].max()
}
