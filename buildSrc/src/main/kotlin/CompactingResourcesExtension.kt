import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

open class CompactingResourcesExtension {
    internal val compactors: MutableList<CompactedResources<*>> = mutableListOf()
    internal val externalResources: MutableList<ExternalResource> = mutableListOf()
    var basePath: String? = null

    fun compactToArray(folder: String, output: String = folder) {
        compactors.add(CompactToArray(folder, output))
    }

    fun compactToObject(folder: String, output: String = folder) {
        compactors.add(CompactToObject(folder, output))
    }

    fun downloadResource(url: String, output: String, json: Boolean = true) {
        externalResources.add(ExternalResource(url, output, json))
    }

    fun substituteFromDifferentFile(folder: String, mainFile: String, output: String = folder) {
        compactors.add(SubstituteFromDifferentFile(folder, mainFile, output))
    }
}

class CompactToObject(val folder: String, val outputFile: String) : CompactedResources<JsonObject>(::JsonObject, outputFile) {
    override fun getPath() = arrayOf("$folder/*.json", "$folder/*.jsonc")

    override fun add(fileName: String, element: JsonElement) {
        value!!.add(fileName, element)
    }
}

class CompactToArray(private val folder: String, outputFile: String) : CompactedResources<JsonArray>(::JsonArray, outputFile) {
    override fun getPath() = arrayOf("$folder/*.json", "$folder/*.jsonc")

    override fun add(fileName: String, element: JsonElement) {
        value!!.add(element)
    }
}

class SubstituteFromDifferentFile(private val folder: String, val mainFile: String, val outputFile: String) :
    CompactedResources<JsonElement>({ JsonArray() }, outputFile) {
    val loadedJsons = mutableMapOf<String, JsonElement>()

    override fun setup() {}

    override fun complete(): JsonElement {
        val mainFile = loadedJsons[mainFile]
        if (mainFile == null) {
            throw IllegalStateException("File $mainFile not found in folder $folder")
        }

        walk(mainFile)

        return mainFile
    }

    private fun walk(jsonObject: JsonElement) {
        when (jsonObject) {
            is JsonObject -> {
                for ((key, value) in jsonObject.entrySet()) {
                    if (value is JsonObject && value.has("@from")) {
                        val from = value.get("@from").asString
                        val json = loadedJsons[from]?.asJsonObject ?: throw IllegalStateException("File $from not found in folder $folder")

                        val copyKey = if (value.has("key")) {
                            value.get("key").asString
                        } else {
                            "@default"
                        }

                        jsonObject.add(key, json.get(copyKey))
                    } else {
                        walk(value)
                    }
                }
            }

            is JsonArray -> {
                for (element in jsonObject) {
                    if (element is JsonObject && element.has("@from")) {
                        val from = element.get("@from").asString
                        val json = loadedJsons[from]?.asJsonObject ?: throw IllegalStateException("File $from not found in folder $folder")

                        val copyKey = if (element.has("key")) {
                            element.get("key").asString
                        } else {
                            "@default"
                        }

                        jsonObject.remove(element)
                        jsonObject.add(json.get(copyKey))
                    } else {
                        walk(element)
                    }
                }
            }
        }
    }

    override fun add(fileName: String, element: JsonElement) {
        loadedJsons[fileName] = element
    }

    override fun getPath() = arrayOf("$folder/*.json", "$folder/*.jsonc")
}

abstract class CompactedResources<T : JsonElement>(private val factory: () -> T, val output: String) {
    protected var value: T? = null

    abstract fun add(fileName: String, element: JsonElement)
    abstract fun getPath(): Array<String>

    open fun setup() {
        value = factory()
    }

    open fun complete(): JsonElement {
        val data = value!!
        value = null
        return data
    }
}

data class ExternalResource(val url: String, val name: String, val json: Boolean)
