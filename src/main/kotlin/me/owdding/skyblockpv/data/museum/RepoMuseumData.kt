package me.owdding.skyblockpv.data.museum

import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktcodecs.NamedCodec
import me.owdding.skyblockpv.utils.Utils

object RepoMuseumData {

    @NamedCodec("museum_data")
    @GenerateCodec
    data class Data(
        val special: List<String>,
        val categories: Map<String, MuseumCategory>
    )

    @GenerateCodec
    data class MuseumCategory(
        @NamedCodec("museum§item") val items: List<MuseumItem>,
        val armors: List<MuseumArmor>
    )

    val categories: MutableMap<String, MuseumCategory> = mutableMapOf()
    val special: MutableList<String> = mutableListOf()

    init {
        val (special, categories) = Utils.loadRepoData<Data>("museum_data")
        this.categories.putAll(categories)
        this.special.addAll(special)
    }

    val entries = categories.values.flatMap { (items, armor) -> listOf(items, armor).flatten() }
    fun getById(id: String): MuseumRepoEntry? = entries.firstOrNull { it.id == id }

}

