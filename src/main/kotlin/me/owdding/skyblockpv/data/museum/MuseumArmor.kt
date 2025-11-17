package me.owdding.skyblockpv.data.museum

import me.owdding.ktcodecs.FieldName
import me.owdding.ktcodecs.GenerateCodec

@GenerateCodec
data class MuseumArmor(
    @FieldName("armor_id") override val id: String,
    @FieldName("parent_id") override val parentId: String?,
    @FieldName("items") val armorIds: List<String> = emptyList(),
) : MuseumRepoEntry
