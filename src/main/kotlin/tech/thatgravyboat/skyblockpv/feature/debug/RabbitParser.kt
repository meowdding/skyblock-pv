package tech.thatgravyboat.skyblockpv.feature.debug

import com.google.gson.GsonBuilder
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.screen.ContainerChangeEvent
import tech.thatgravyboat.skyblockapi.api.events.screen.ContainerCloseEvent
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.Text.send
import tech.thatgravyboat.skyblockapi.utils.text.TextProperties.stripped
import tech.thatgravyboat.skyblockpv.SkyBlockPv
import tech.thatgravyboat.skyblockpv.config.DevConfig
import java.nio.file.Files

object RabbitParser {
    private var data: MutableMap<String, MutableSet<String>> = mutableMapOf()

    private val incorrectApiNames = mapOf(
        "fish_the_rabbit" to "fish",
    )

    @Subscription
    fun onInv(event: ContainerChangeEvent) {
        if (!shouldParse()) return
        if (!event.title.contains("Hoppity's Collection")) return
        if (event.slot !in 9..44) return
        if (!listOf(Items.PLAYER_HEAD, Items.GRAY_DYE).any { event.item.`is`(it) }) return

        val rarity = event.item.getData(DataTypes.RARITY) ?: return
        val name = event.item.hoverName.stripped
        val probablyApiName = name.split(" ", "-").joinToString("_").lowercase().let {
            incorrectApiNames[it] ?: it
        }
        data.computeIfAbsent(rarity.name) { mutableSetOf() }.add(probablyApiName)
    }

    @Subscription
    fun onInvGone(event: ContainerCloseEvent) {
        if (!shouldParse()) return
        if (data.isEmpty()) return
        Text.of("Storing Rabbit Data in config").send()

        val file = FabricLoader.getInstance().configDir
            .resolve("skyblockpv")
            .resolve("rabbits.json")

        Files.createDirectories(file.parent)
        Files.writeString(file, GsonBuilder().create().toJson(data))

        data.clear()
    }

    fun shouldParse() = SkyBlockPv.isDevMode && DevConfig.hoppityParser
}
