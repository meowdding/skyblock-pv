package me.owdding.skyblockpv.data.museum

import me.owdding.ktcodecs.FieldName
import me.owdding.ktcodecs.GenerateCodec
import tech.thatgravyboat.skyblockapi.api.remote.RepoItemsAPI
import tech.thatgravyboat.skyblockapi.utils.extentions.cleanName
import tech.thatgravyboat.skyblockapi.utils.extentions.getRawLore

@GenerateCodec
data class MuseumArmor(
    @FieldName("armor_id") override val id: String,
    @FieldName("parent_id") override val parentId: String?,
    @FieldName("items") val armorIds: List<String> = emptyList(),
) : MuseumRepoEntry {
    override fun matches(query: String): Boolean = buildSet {
        add(id)
        parentId?.let(::add)
        addAll(armorIds)

    }.flatMap {
        val item = RepoItemsAPI.getItemOrNull(it) ?: return@flatMap listOf(it)

        buildList {
            add(it)
            add(item.cleanName)
            item.getRawLore().forEach(::add)
        }
    }.any { it.contains(query, true) }
}
