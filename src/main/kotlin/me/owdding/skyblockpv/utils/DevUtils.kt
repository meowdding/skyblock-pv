package me.owdding.skyblockpv.utils

import me.owdding.ktmodules.Module
import me.owdding.skyblockpv.utils.ChatUtils.sendWithPrefix
import net.minecraft.network.chat.MutableComponent
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.misc.RegisterCommandsEvent
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.platform.Identifiers
import tech.thatgravyboat.skyblockapi.utils.DevUtils
import tech.thatgravyboat.skyblockapi.utils.extentions.parseFormattedInt
import java.nio.file.StandardOpenOption
import java.util.Properties
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.outputStream
import kotlin.io.path.reader


@Module
internal object SkyBlockPvDevUtils : DevUtils() {

    private val path = System.getProperty("skyblockpv.property_path")?.let { Path(it) } ?: McClient.config.resolve("skyblockpv.properties")
    override val commandName: String = "sbpv dev toggle"
    override fun send(component: MutableComponent) = component.sendWithPrefix()
    val properties: MutableMap<String, String> = loadFromProperties().toMutableMap()

    fun getString(key: String, default: String): String = properties[key] ?: default
    fun getString(key: String): String? = properties[key]

    fun getInt(key: String, default: Int = 0): Int {
        return properties[key].parseFormattedInt(default)
    }

    fun getBoolean(key: String): Boolean {
        return properties[key] == "true"
    }

    private fun loadProperties(): Properties {
        val properties = Properties()
        if (path.exists()) {
            path.reader(Charsets.UTF_8).use {
                properties.load(it)
            }
        }
        return properties
    }

    fun saveProperties() {
        val properties = Properties()
        properties.putAll(this.properties)
        properties.store(this.path.outputStream(StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING), null)
    }

    private fun loadFromProperties(): Map<String, String> {
        val properties = loadProperties()
        val map = mutableMapOf<String, String>()
        properties.forEach { (key, value) ->
            Identifiers.parseWithSeparator(key.toString(), '@')?.let {
                if (value.toString() == "true") {
                    states[it] = true
                }
            }
            map[key.toString()] = value.toString()
        }
        return map
    }

    @Subscription
    fun commandRegister(event: RegisterCommandsEvent) = super.onCommandRegister(event)
}
