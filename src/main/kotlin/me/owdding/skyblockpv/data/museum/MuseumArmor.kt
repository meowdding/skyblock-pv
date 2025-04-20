package me.owdding.skyblockpv.data.museum

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import com.teamresourceful.resourcefullib.common.codecs.CodecExtras
import kotlin.jvm.optionals.getOrNull

data class MuseumArmor(override val id: String, override val parentId: String?, val armorIds: List<String>): MuseumRepoEntry

val MUSEUM_ARMOR_CODEC = RecordCodecBuilder.create {
    it.group(
        Codec.STRING.fieldOf("armor_id").forGetter(MuseumArmor::id),
        Codec.STRING.optionalFieldOf("parent_id").forGetter(CodecExtras.optionalFor(MuseumArmor::parentId)),
        Codec.STRING.listOf().optionalFieldOf("items", emptyList()).forGetter(MuseumArmor::armorIds)
    ).apply(it) { id, parent, items ->
        MuseumArmor(id, parent.getOrNull(), items)
    }
}
