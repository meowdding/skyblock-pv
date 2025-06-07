package me.owdding.skyblockpv.utils

data class StringReader(val text: String) {
    var cursor: Int = 0
    val maxIndex: Int = text.length - 1

    fun canRead() = cursor <= maxIndex
    fun peek() = if (canRead()) text[cursor] else null
    fun read() = text[cursor++]
    fun skip() {
        cursor++
    }

    fun readUntil(terminating: Char): String = with(StringBuilder()) {
        while (canRead() && peek() != terminating) {
            append(read())
        }
        skip()
        this
    }.toString()

}
