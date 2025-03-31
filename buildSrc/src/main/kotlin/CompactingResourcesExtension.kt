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

abstract class CompactedResources<T: JsonElement>(private val factory: () -> T, val output: String) {
    protected var value: T? = null

    abstract fun add(fileName: String, element: JsonElement)
    abstract fun getPath(): Array<String>

    fun setup() {
        value = factory()
    }

    fun complete(): JsonElement {
        val data = value!!
        value = null
        return data
    }
}

data class ExternalResource(val url: String, val name: String, val json: Boolean)
