package me.owdding.skyblockpv.feature.debug

import com.google.gson.GsonBuilder
import me.owdding.ktmodules.Module
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.config.DevConfig
import me.owdding.skyblockpv.utils.ChatUtils.sendWithPrefix
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.screen.ContainerCloseEvent
import tech.thatgravyboat.skyblockapi.api.events.screen.InventoryChangeEvent
import tech.thatgravyboat.skyblockapi.api.remote.RepoItemsAPI
import tech.thatgravyboat.skyblockapi.impl.tagkey.ItemTag
import tech.thatgravyboat.skyblockapi.utils.regex.RegexUtils.match
import tech.thatgravyboat.skyblockapi.utils.text.Text
import java.nio.file.Files

@Module
object SacksParser {
    private var data: MutableMap<String, Sack> = mutableMapOf()

    private var titleRegex = "(?<name>.*) Sack".toRegex()

    @Subscription
    fun onInv(event: InventoryChangeEvent) {
        if (!shouldParse()) return
        if (event.item in ItemTag.GLASS_PANES) return
        if (event.isInBottomRow) return
        if (event.isInPlayerInventory) return

        val id = event.item.getData(DataTypes.ID) ?: return
        titleRegex.match(event.title, "name") { (name) ->
            val title = event.title.replace("Gemstones", "Gemstone").replace("Dragons", "Dragon")
            val parsedIds = when (name) {
                "Gemstones" -> {
                    listOf("ROUGH", "FLAWED", "FINE").map {
                        id.replace("ROUGH", it)
                    }
                }

                else -> listOf(id)
            }

            // Get the Sack Item ID, some sacks don't have multiple scales, some do
            val sackId = listOf("", "LARGE_", "MEDIUM_", "SMALL_").firstNotNullOfOrNull {
                RepoItemsAPI.getItem("$it${title.replace(" ", "_").uppercase()}").takeUnless { it.item == Items.BARRIER }?.getData(DataTypes.ID)
            } ?: title

            data.computeIfAbsent(name) { Sack(sackId, mutableSetOf()) }.add(parsedIds)
        }
    }

    @Subscription(event = [ContainerCloseEvent::class])
    fun onInvGone() {
        if (!shouldParse()) return
        if (data.isEmpty()) return
        Text.of("Storing Sacks Data in config").sendWithPrefix()

        val file = FabricLoader.getInstance().configDir
            .resolve("skyblockpv")
            .resolve("sacks.json")

        Files.createDirectories(file.parent)
        Files.writeString(file, GsonBuilder().create().toJson(data.values))
    }

    fun shouldParse() = SkyBlockPv.isDevMode && DevConfig.sacksParser

    private data class Sack(
        val sack: String,
        val items: MutableSet<String>,
    ) {
        fun add(list: List<String>) {
            items.addAll(list)
        }
    }
}
