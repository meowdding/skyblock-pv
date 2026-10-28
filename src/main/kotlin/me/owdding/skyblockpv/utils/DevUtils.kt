package me.owdding.skyblockpv.utils

import me.owdding.ktmodules.Module
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.SkyBlockPv.id
import me.owdding.skyblockpv.utils.ChatUtils.sendWithPrefix
import net.minecraft.network.chat.MutableComponent
import tech.thatgravyboat.skyblockapi.helpers.McClient
import tech.thatgravyboat.skyblockapi.utils.devutils.DevUtils
import tech.thatgravyboat.skyblockapi.utils.devutils.entries.DebugSelect
import tech.thatgravyboat.skyblockapi.utils.devutils.entries.DebugToggle
import tech.thatgravyboat.skyblockapi.utils.extentions.parseFormattedInt
import java.nio.file.Path
import kotlin.io.path.Path

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
internal data object SkyblockPvDevUtils : DevUtils() {
    override val commandName: String = "sbpv toggle"
    override val namespace: String = SkyBlockPv.MOD_ID
    override val path: Path = loadPath()
    val propertiesMap: Map<String, String> = loadFromProperties(path)

    override fun send(component: MutableComponent) = component.sendWithPrefix()

    fun getInt(key: String, default: Int = 0): Int {
        return propertiesMap[key].parseFormattedInt(default)
    }

    fun getBoolean(key: String): Boolean {
        return propertiesMap[key] == "true"
    }

    private fun loadPath(): Path {
        val specifiedPath = System.getenv("sbpv.property_path") ?: System.getProperty("sbpv.property_path")
        return if (specifiedPath != null) Path(specifiedPath)
        else McClient.config.resolve("sbpv.properties")
    }
}
