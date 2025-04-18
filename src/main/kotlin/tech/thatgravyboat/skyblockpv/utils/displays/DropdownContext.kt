package tech.thatgravyboat.skyblockpv.utils.displays

import tech.thatgravyboat.lib.displays.Display


class DropdownContext(val fadeTime: Int = 200) {
    var lastUpdated = 0L
    var currentDropdown: Display? = null
        set(value) {
            val difference = System.currentTimeMillis() - lastUpdated
            if (field == null || value == null) {
                lastUpdated = if (difference > fadeTime) {
                    System.currentTimeMillis()
                } else {
                    System.currentTimeMillis() - (fadeTime - difference)
                }
            }
            field = value
        }

    fun isCurrentDropdown(that: Display): Boolean {
        return currentDropdown == null || currentDropdown === that
    }
}
