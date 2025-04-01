package tech.thatgravyboat.skyblockpv.data.museum

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import java.util.*
import kotlin.jvm.optionals.getOrNull

data class MuseumArmor(val id: String, val parentId: String?, val armorIds: List<String>)

val MUSEUM_ARMOR_CODEC = RecordCodecBuilder.create {
    it.group(
        Codec.STRING.fieldOf("armor_id").forGetter(MuseumArmor::id),
        Codec.STRING.optionalFieldOf("parent_id").forGetter { Optional.empty() },
        Codec.STRING.listOf().optionalFieldOf("items", emptyList()).forGetter(MuseumArmor::armorIds)
    ).apply(it, ::initArmor)
}

private fun initArmor(id: String, parentId: Optional<String>, items: List<String>): MuseumArmor {
    return MuseumArmor(id, parentId.getOrNull(), items)
}
