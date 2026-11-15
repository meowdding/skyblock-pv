package me.owdding.skyblockpv.utils

import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.mojang.serialization.JsonOps
import me.owdding.lib.builder.ComponentFactory
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.utils.Utils.append
import me.owdding.skyblockpv.utils.codecs.CodecUtils
import net.minecraft.network.chat.Component
import tech.thatgravyboat.skyblockapi.api.profile.profile.ProfileAPI
import tech.thatgravyboat.skyblockapi.utils.json.Json.readJson
import tech.thatgravyboat.skyblockapi.utils.json.Json.toData
import tech.thatgravyboat.skyblockapi.utils.text.Text
import tech.thatgravyboat.skyblockapi.utils.text.TextColor
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.color
import tech.thatgravyboat.skyblockapi.utils.text.TextStyle.hover
import java.nio.file.Files
import java.util.concurrent.ConcurrentHashMap
import kotlin.jvm.optionals.getOrNull

object PlayerLevelCache {
    private val cache = ConcurrentHashMap<String, Int>()
    private val file = SkyBlockPv.configDir.resolve("player_levels.json")

    private val CODEC = CodecUtils.map<String, Int>()

    init {
        load()
    }

    private fun load() {
        if (!Files.exists(file)) return
        cache.putAll(Files.readString(file).readJson<JsonElement>().toData(CODEC) ?: emptyMap())
    }

    private fun save() {
        Files.createDirectories(file.parent)

        val data = CODEC.encodeStart(JsonOps.INSTANCE, cache.toMap()).resultOrPartial {
            SkyBlockPv.error("Failed to save player levels: $it")
        }?.getOrNull() ?: return

        Files.writeString(file, GsonBuilder().create().toJson(data))
    }

    fun update(username: String, level: Int) {
        cache[username.lowercase()] = level
        save()
    }

    fun get(username: String): Int? = cache[username.lowercase()]
    fun getComponent(username: String): Component? {
        val level = get(username) ?: return null
        return levelPrefixComponent(level)
    }

    private fun levelPrefixComponent(level: Int) = Text.of {
        append("[") { color = TextColor.DARK_GRAY }
        append(level.toString()) { color = ProfileAPI.getLevelColor(level) }
        append("] ") { color = TextColor.DARK_GRAY }

        hover = ComponentFactory.multiline {
            string("Added by SkyBlockPv") { color = TextColor.GRAY }
            string(" Requires opening their viewer to update") { color = TextColor.GRAY }
            string(" Will not work with different profiles") { color = TextColor.GRAY }
        }
    }
}
