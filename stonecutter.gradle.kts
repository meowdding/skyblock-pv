plugins {
    id("dev.kikugie.stonecutter")
    id("fabric-loom") version "1.14-SNAPSHOT" apply false
}

stonecutter active "1.21.11"

stonecutter parameters {
    swaps["mod_version"] = "\"" + property("version") + "\";"
    swaps["minecraft"] = "\"" + node.metadata.version + "\";"

    Replacements.read(project).replacements.forEach { (name, replacement) ->
        when (replacement) {
            is StringReplacement if replacement.named -> replacements.string(name) {
                direction = eval(current.version, replacement.condition)
                replace(replacement.from, replacement.to)
            }

            is RegexReplacement if replacement.named -> replacements.regex(name) {
                direction = eval(current.version, replacement.condition)
                replace(replacement.regex, replacement.to)
                reverse(replacement.reverseRegex, replacement.reverse)
            }

            is StringReplacement -> replacements.string {
                direction = eval(current.version, replacement.condition)
                replace(replacement.from, replacement.to)
            }

            is RegexReplacement -> replacements.regex {
                direction = eval(current.version, replacement.condition)
                replace(replacement.regex, replacement.to)
                reverse(replacement.reverseRegex, replacement.reverse)
            }
        }
    }
}
