package tech.thatgravyboat.skyblockpv.data.museum

import com.google.gson.JsonObject
import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import com.mojang.serialization.codecs.RecordCodecBuilder
import tech.thatgravyboat.skyblockpv.utils.Utils

object RepoMuseumData {

    var armor: List<MuseumArmor> = emptyList()
    var items: List<MuseumItem> = emptyList()
    var rarities: List<MuseumItem> = emptyList()
    var special: List<String> = emptyList()

    private val CODEC = RecordCodecBuilder.create {
        it.group(
            MUSEUM_ARMOR_CODEC.listOf().fieldOf("armor").forGetter { armor },
            MUSEUM_ITEM_CODEC.listOf().fieldOf("items").forGetter { items },
            MUSEUM_ITEM_CODEC.listOf().fieldOf("rarities").forGetter { rarities },
            Codec.STRING.listOf().fieldOf("special").forGetter { special }
        ).apply(it) { armor, items, rarities, special ->
            RepoMuseumData.armor = armor
            RepoMuseumData.items = items
            RepoMuseumData.rarities = rarities
            RepoMuseumData.special = special
        }
    }

    init {
        val museumData = Utils.loadFromRepo<JsonObject>("museum_data") ?: JsonObject()

        CODEC.parse(JsonOps.INSTANCE, museumData).let {
            if (it.isError) {
                throw RuntimeException(it.error().get().message())
            }
        }
    }
}
