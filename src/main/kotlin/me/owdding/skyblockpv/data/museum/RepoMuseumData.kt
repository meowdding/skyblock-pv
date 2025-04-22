package me.owdding.skyblockpv.data.museum

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import com.mojang.serialization.codecs.RecordCodecBuilder
import me.owdding.skyblockpv.utils.Utils
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.api.data.SkyBlockCategory
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import tech.thatgravyboat.skyblockapi.api.remote.RepoItemsAPI

object RepoMuseumData {

    var armor: List<MuseumArmor> = emptyList()
    var weapons: List<MuseumItem> = emptyList()
    var rarities: List<MuseumItem> = emptyList()
    var special: List<String> = emptyList()

    val museumCategories: MutableList<RepoMuseumCategory> = mutableListOf()
    val museumCategoryMap: MutableMap<RepoMuseumCategory, List<MuseumItem>> = mutableMapOf()

    private val CODEC = RecordCodecBuilder.create {
        it.group(
            MUSEUM_ARMOR_CODEC.listOf().fieldOf("armor").forGetter { armor },
            MUSEUM_ITEM_CODEC.listOf().fieldOf("weapons").forGetter { weapons },
            MUSEUM_ITEM_CODEC.listOf().fieldOf("rarities").forGetter { rarities },
            Codec.STRING.listOf().fieldOf("special").forGetter { special },
        ).apply(it) { armor, weapons, rarities, special ->
            RepoMuseumData.armor = armor.sortedWith(
                Comparator.comparingInt<MuseumArmor>(
                    {
                        it.armorIds.map { RepoItemsAPI.getItem(it) }.maxOf { it.getData(DataTypes.RARITY)?.ordinal ?: 0 }
                    },
                ).then(Comparator.comparing { it.id }),
            )
            //.sortedBy { ItemAPI.getItem(it.armorIds.first()).getData(DataTypes.RARITY)?.ordinal ?: 0 }
            RepoMuseumData.weapons = weapons
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

        val museumCategories = Utils.loadFromRepo<JsonArray>("museum_categories") ?: JsonArray()
        MUSEUM_CATEGORY_CODEC.listOf().parse(JsonOps.INSTANCE, museumCategories).ifSuccess {
            this.museumCategories.addAll(it)
        }.ifError {
            throw RuntimeException(it.error().get().message())
        }

        evaluateItemCategoryMap()
    }

    private fun convertToId(data: SkyBlockCategory?) = data?.toString()?.lowercase()?.replace(" ", "_")

    private fun evaluateItemCategoryMap() {
        val ids = listOf(rarities, weapons).flatten()

        val sortedBy = museumCategories.sortedByDescending { it.priority }
        museumCategoryMap.putAll(
            ids.groupBy { museumItem ->
                val item = RepoItemsAPI.getItem(museumItem.id)
                val data = convertToId(item.getData(DataTypes.CATEGORY))

                sortedBy.find { it.categories.contains(data) || it.items.contains(museumItem.id) || it.categories.contains("*") }
            }.mapKeys {
                it.key ?: RepoMuseumCategory(
                    "unknown",
                    lazy { Items.BARRIER.defaultInstance },
                    emptyList(),
                    emptyList(),
                    0,
                )
            }.toSortedMap(Comparator.comparingInt<RepoMuseumCategory> { it.priority }.reversed()),
        )
    }

    fun getById(id: String): MuseumRepoEntry? = listOf(armor, rarities, weapons).flatten().firstOrNull { it.id == id }

}

