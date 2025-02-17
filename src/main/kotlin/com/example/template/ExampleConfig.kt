package com.example.template

import com.teamresourceful.resourcefulconfig.api.annotations.Comment
import com.teamresourceful.resourcefulconfig.api.annotations.Config
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigEntry
import com.teamresourceful.resourcefulconfig.api.annotations.ConfigInfo
import com.teamresourceful.resourcefulconfig.api.types.entries.Observable
import com.teamresourceful.resourcefulconfig.api.types.options.EntryType

@ConfigInfo(
    title = "Example Config",
    description = "This is an example config for the Example Mod.",
)
@Config("example")
object ExampleConfig {

    @field:ConfigEntry(
        id = "example",
        type = EntryType.BOOLEAN,
    )
    @field:Comment("This is an example config entry.")
    var example: Observable<Boolean> = Observable.of(true)

    init {
        example.addListener { old, new ->
            println("Example config entry changed from $old to $new.")
        }
    }
}