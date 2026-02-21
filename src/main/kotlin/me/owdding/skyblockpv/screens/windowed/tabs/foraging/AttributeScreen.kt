package me.owdding.skyblockpv.screens.windowed.tabs.foraging

import com.mojang.authlib.GameProfile
import me.owdding.lib.displays.Display
import me.owdding.lib.displays.DisplayWidget
import me.owdding.lib.displays.Displays
import me.owdding.lib.displays.withTooltip
import me.owdding.lib.extensions.ListMerger
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.data.api.Attribute
import me.owdding.skyblockpv.data.repo.AttributesData
import me.owdding.skyblockpv.screens.windowed.tabs.base.GroupedScreen
import me.owdding.skyblockpv.utils.Utils.skipUntil
import me.owdding.skyblockpv.utils.Utils.toDateTime
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

class AttributeScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : BaseForagingScreen(gameProfile, profile),
    GroupedScreen<AttributeScreen.Filter, SkyBlockRarity, Pair<AttributesAPI.Attribute?, Attribute?>> {
    override val type: ForagingCategory = ForagingCategory.ATTRIBUTES
    override var query: String? = null
    override var filter: Filter = Filter.ALL
    override val noMatchFoundText: String = "No Attribute matches the input!"
    override val Pair<AttributesAPI.Attribute?, Attribute?>.group: SkyBlockRarity? get() {
        return runCatching { SkyBlockRarity.valueOf(first?.rarity!!) }.getOrNull()
    }

    override fun filterEntries(): Collection<Filter> = Filter.entries
    override fun Filter.display(): String = this.display
    override fun SkyBlockRarity.compareTo(other: SkyBlockRarity): Int = this.ordinal - other.ordinal

    override fun getLayout(bg: DisplayWidget): Layout = createLayout(bg)

    override fun getData(): List<Pair<AttributesAPI.Attribute?, Attribute?>> {

        val attributes = profile.attributeData.data.associateBy { it.id.lowercase() }

        val repoData = RepoAPI.attributes().attributes().values.associateBy { it.id().lowercase() }

        return buildList {
            addAll(repoData.values.map { it to attributes[it.id().lowercase()] })
            addAll(attributes.filterNot { (key) -> repoData.containsKey(key.lowercase()) }.map { (_, value) -> null to value })
        }
    }

    override fun Filter.doesDisplay(data: Pair<AttributesAPI.Attribute?, Attribute?>): Boolean {
        val (repo, api) = data
        return when (filter) {
            Filter.ALL -> true
            Filter.MAXED -> (repo?.let(::getMax) ?: 0) <= (api?.syphoned ?: 0)
            Filter.UNLOCKED -> api != null
            Filter.LOCKED -> api == null
        }
    }

    override fun matchesSearch(data: Pair<AttributesAPI.Attribute?, Attribute?>): Boolean {
        val (repo, _) = data
        val query = query
        if (query == null || repo == null) return true
        return buildList {
            add(repo.name)
            add(repo.shardId)
            add(repo.id)
            add(repo.shardName)
            add(repo.attributeId)
            add(repo.rarity)
            addAll(repo.lore)
        }.map { it.stripColor() }.any { it.contains(query, ignoreCase = true) }
    }

    fun getMax(repo: AttributesAPI.Attribute) = when (repo.id.lowercase()) {
        "reptiloid" -> 0
        else -> repo.max
    }

    override fun getColor(group: SkyBlockRarity?, data: Pair<AttributesAPI.Attribute?, Attribute?>): Int {
        val (repo, api) = data
        val baseColor = group?.color ?: -1

        if (api == null) return ARGB.scaleRGB(baseColor, 0.65f)

        if (repo == null || api.syphoned.absoluteValue >= getMax(repo)) return baseColor

        return ARGB.scaleRGB(baseColor, 0.75f + (0.25f * (api.syphoned / repo.max.toFloat())))
    }

    override fun toDisplay(group: SkyBlockRarity?, data: Pair<AttributesAPI.Attribute?, Attribute?>): Display {
        val (repo, api) = data
        val item = repo?.id()?.let { RepoAttributeAPI.getAttributeByIdOrNull(it) }
        return Displays.item(
            when {
                repo?.let(::getMax) != 0 && (api == null || api.syphoned <= 0) && filter != Filter.LOCKED -> Items.GRAY_DYE.defaultInstance
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

                    color = group?.color ?: -1
                }
                ListMerger(repo.lore).apply {
                    addUntil { it.stripColor().startsWith("You can Syphon this shard") }
                    skipUntil { it.endsWith("SHARD") }

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
                else if group == null -> add {
                    color = TextColor.GRAY
                    append("Syphoned: ")
                    append(syphoned.toString(), TextColor.YELLOW)
                    append("/")
                    append("?", TextColor.RED)
                }

                else -> {
                    val level = group.getLevel(syphoned)
                    val max = group.getMax()
                    add {
                        color = TextColor.GRAY
                        append("Syphoned: ")
                        append(
                            syphoned.toString(),
                            when (syphoned) {
                                0 -> TextColor.RED
                                max -> TextColor.GREEN
                                else -> TextColor.YELLOW
                            },
                        )
                        append("/")
                        append(max.toString(), TextColor.GREEN)
                    }
                    add {
                        color = TextColor.GRAY
                        append("Level: ")
                        append(
                            level.toString(),
                            when (level) {
                                0 -> TextColor.RED
                                10 -> TextColor.GREEN
                                else -> TextColor.YELLOW
                            },
                        )
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

    enum class Filter(val display: String) {
        ALL("All"),
        UNLOCKED("Unlocked"),
        LOCKED("Locked"),
        MAXED("Maxed"),
        ;
    }
}
