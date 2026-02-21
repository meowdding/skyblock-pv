package me.owdding.skyblockpv.data.museum

import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktcodecs.NamedCodec
import me.owdding.skyblockpv.utils.Utils
import me.owdding.skyblockpv.utils.codecs.CodecUtils
import net.minecraft.world.item.Items
import tech.thatgravyboat.skyblockapi.api.data.SkyBlockCategory
import tech.thatgravyboat.skyblockapi.api.datatype.DataTypes
import tech.thatgravyboat.skyblockapi.api.datatype.getData
import tech.thatgravyboat.skyblockapi.api.remote.RepoItemsAPI

object RepoMuseumData {

    val armor: List<MuseumArmor>
    val weapons: List<MuseumItem>
    val rarities: List<MuseumItem>
    val special: List<String>

    val museumCategories: List<RepoMuseumCategory>
    val museumCategoryMap: MutableMap<RepoMuseumCategory, List<MuseumItem>> = mutableMapOf()

    @GenerateCodec
    @NamedCodec("MuseumData")
    data class Data(
        val armor: List<MuseumArmor>,
        @NamedCodec("museum§item") val weapons: List<MuseumItem>,
        @NamedCodec("museum§item") val rarities: List<MuseumItem>,
        val special: List<String>,
    )

    init {
        Utils.loadRepoData<Data>("museum_data").let {
            this.armor = it.armor
            this.weapons = it.weapons
            this.rarities = it.rarities
            this.special = it.special
        }

        this.museumCategories = Utils.loadRepoData("museum_categories", CodecUtils.list())

        evaluateItemCategoryMap()
    }

    private fun convertToId(data: SkyBlockCategory?) = data?.toString()?.lowercase()?.replace(" ", "_")

    private fun evaluateItemCategoryMap() {
        val ids = listOf(rarities, weapons).flatten()

        val sortedBy = museumCategories.sortedByDescending { it.priority }
        museumCategoryMap.putAll(
            ids.groupBy { museumItem ->
                val item = RepoItemsAPI.getItem(museumItem.id)
                val data = convertToId(item.getData(DataTypes.CATEGORY)) ?: return@groupBy null

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

    val entries = listOf(armor, rarities, weapons).flatten().toSet()
    fun getById(id: String): MuseumRepoEntry? = entries.firstOrNull { it.id == id }

}

