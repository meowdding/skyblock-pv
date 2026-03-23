plugins {
    id("dev.kikugie.stonecutter")
    id("net.fabricmc.fabric-loom-remap") apply false
    id("net.fabricmc.fabric-loom") apply false
}

stonecutter active "1.21.11"

stonecutter parameters {
    swaps["mod_version"] = "\"" + property("version") + "\";"
    swaps["minecraft"] = "\"" + node.metadata.version + "\";"

    Replacements.read(project).replacements.forEach { (name, replacement) ->
        when (replacement) {
            is StringReplacement -> replacements.string {
                if (replacement.named) {
                    id = name
                }
                direction = eval(current.version, replacement.condition)
                replace(replacement.from, replacement.to)
            }

            is RegexReplacement -> replacements.regex {
                if (replacement.named) {
                    id = name
                }
                direction = eval(current.version, replacement.condition)
                replace(
                    replacement.regex to replacement.to,
                    replacement.reverseRegex to replacement.reverse
                )
            }
        }
    }
}
