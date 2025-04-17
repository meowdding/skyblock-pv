
// Todo move to sbapi
inline fun <reified T : Enum<T>> JsonElement?.asEnum(mapper: (T) -> String = { it.name }): T? {
    val content = this.asString("").takeUnless { it.isBlank() } ?: return null

    T::class.java.enumConstants.forEach {
        if (mapper(it).equals(content, true)) {
            return it
        }
    }

    return null
}
