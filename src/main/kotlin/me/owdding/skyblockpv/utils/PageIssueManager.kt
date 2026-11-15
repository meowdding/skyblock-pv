package me.owdding.skyblockpv.utils

import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktmodules.Module
import me.owdding.skyblockpv.SkyBlockPv
import me.owdding.skyblockpv.screens.PvTab
import me.owdding.skyblockpv.utils.components.PvToast
import tech.thatgravyboat.skyblockapi.utils.extentions.toTitleCase

@Module
object PageIssueManager {

    private val issues: PageIssue? by SkyBlockPv.repoDelegate("pv/issues")

    fun notifyGlobal() {
        issues?.global?.let { issue ->
            toast("global", issue)
        }
    }

    fun notifyTab(tab: PvTab) {
        val name = tab.name
        issues?.tabs[name]?.let { issue ->
            toast(name, issue)
        }
    }

    private fun toast(tab: String, issue: String) {
        if (issue.isBlank()) return
        PvToast.addPageIssueToast("${tab.toTitleCase()} Issue", issue)
    }

    @GenerateCodec
    data class PageIssue(
        val global: String?,
        val tabs: Map<String, String> = emptyMap(),
    )
}
