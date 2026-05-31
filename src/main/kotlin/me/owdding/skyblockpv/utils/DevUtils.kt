package me.owdding.skyblockpv.utils

import me.owdding.ktmodules.Module
import me.owdding.skyblockpv.SkyBlockPv.id
import me.owdding.skyblockpv.utils.ChatUtils.sendWithPrefix
import net.minecraft.network.chat.MutableComponent
import tech.thatgravyboat.skyblockapi.api.SkyBlockAPI
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.api.events.misc.RegisterCommandsEvent
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.platform.Identifiers
import tech.thatgravyboat.skyblockapi.utils.DebugSelect
import tech.thatgravyboat.skyblockapi.utils.DebugToggle
import tech.thatgravyboat.skyblockapi.utils.DevUtils
import tech.thatgravyboat.skyblockapi.utils.extentions.parseFormattedInt
import java.util.Properties
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.io.path.Path
import kotlin.io.path.notExists
import kotlin.io.path.reader


internal fun debugToggle(path: String, description: String = path): DebugToggle {
    return DebugToggle(id(path), description, SkyblockPvDevUtils)
}


internal fun <T : Any> debugSelect(
    path: String,
    description: String = path,
    initialState: T?,
    states: List<T>,
    toString: (T) -> String = { it.toString() },
): DebugSelect<T> {
    return DebugSelect(id(path), description, SkyblockPvDevUtils, initialState, toString, states)
}

internal inline fun <reified T : Enum<T>> debugSelect(
    path: String,
    description: String = path,
    initialState: T? = null,
): DebugSelect<T> = debugSelect(path, description, initialState, T::class.java.enumConstants.toList())


@Module
internal object SkyblockPvDevUtils : DevUtils() {
    override val commandName: String = "sbpv toggle"
    override fun send(component: MutableComponent) = component.sendWithPrefix()
    val properties: Map<String, String> = loadFromProperties()

    fun getInt(key: String, default: Int = 0): Int {
        return properties[key].parseFormattedInt(default)
    }

    fun getBoolean(key: String): Boolean {
        return properties[key] == "true"
    }

    private fun loadFromProperties(): Map<String, String> {
        val properties = Properties()
        val path = System.getProperty("sbpv.property_path")?.let { Path(it) } ?: McClient.config.resolve("sbpv.properties")
        if (path.notExists()) return emptyMap()
        path.reader(Charsets.UTF_8).use {
            properties.load(it)
        }
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
