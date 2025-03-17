package tech.thatgravyboat.skyblockpv.api

import com.mojang.serialization.JsonOps
import net.minecraft.world.item.ItemStack
import tech.thatgravyboat.repolib.api.RepoAPI

object ItemAPI {

    private val cache: MutableMap<String, ItemStack?> = mutableMapOf()

    init {
        RepoAPI.setup()
    }

    fun get(id: String): ItemStack {
        if (cache.containsKey(id)) {
            return cache[id] ?: ItemStack.EMPTY
        }
        val data = RepoAPI.items().getItem(id)
        cache[id] = ItemStack.CODEC.parse(JsonOps.INSTANCE, data).result().orElse(null)
        return cache[id] ?: ItemStack.EMPTY
    }
}
