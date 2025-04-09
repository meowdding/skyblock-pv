package tech.thatgravyboat.skyblockpv.feature.debug

import com.google.gson.GsonBuilder
import net.fabricmc.loader.api.FabricLoader
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.screen.ContainerChangeEvent
import tech.thatgravyboat.skyblockapi.api.events.screen.ContainerCloseEvent
import tech.thatgravyboat.skyblockapi.utils.regex.RegexUtils.match
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.Text.send
import tech.thatgravyboat.skyblockpv.SkyBlockPv
import tech.thatgravyboat.skyblockpv.config.DevConfig
import java.nio.file.Files

object SacksParser {
    private var data: MutableMap<String, MutableSet<String>> = mutableMapOf()

    private var titleRegex = "(?<name>.*) Sack".toRegex()

    @Subscription
    fun onInv(event: ContainerChangeEvent) {
        if (!shouldParse()) return
        if (event.slot !in 9..44) return

        val id = event.item.getData(DataTypes.ID) ?: return
        titleRegex.match(event.title, "name") { (name) ->
            val parsedIds = when (name) {
                "Gemstone" -> {
                    listOf("ROUGH", "FLAWED", "FINE").map {
                        id.replace("ROUGH", it)
                    }
                }

                else -> listOf(id)
            }

            data.computeIfAbsent(name) { mutableSetOf() }.addAll(parsedIds)
        }
    }

    @Subscription
    fun onInvGone(event: ContainerCloseEvent) {
        if (!shouldParse()) return
        if (data.isEmpty()) return
        Text.of("Storing Sacks Data in config").send()

        val file = FabricLoader.getInstance().configDir
            .resolve("skyblockpv")
            .resolve("sacks.json")

        Files.createDirectories(file.parent)
        Files.writeString(file, GsonBuilder().create().toJson(data))

        data.clear()
    }

    fun shouldParse() = SkyBlockPv.isDevMode && DevConfig.sacksParser
}
