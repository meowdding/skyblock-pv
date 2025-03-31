package tech.thatgravyboat.skyblockpv.museum

import com.google.gson.JsonObject

class ItemParser(key: String) : MuseumProcessor(key) {
    override fun process(item: JsonObject) {
        val id = item["id"].asString
        val museumData = item["museum_data"].asJsonObject

        val output = JsonObject()
        output["id"] = id

        if (museumData.has("parent")) {
            museumData["parent"].asJsonObject[id]?.let {
                output["parent"] = it
            }
        }

        if (museumData.has("mapped_item_ids")) {
            museumData["mapped_item_ids"]?.asJsonArray?.takeUnless { it.isEmpty }?.let {
                output["mapped_item_ids"] = it
            }
        }

        value.add(output)
    }
}
