package me.owdding.skyblockpv.utils

import me.owdding.ktcodecs.GenerateCodec
import me.owdding.ktmodules.Module
import me.owdding.lib.events.FinishRepoLoadingEvent
import me.owdding.skyblockpv.generated.SkyBlockPvCodecs
import me.owdding.skyblockpv.screens.PvTab
import me.owdding.skyblockpv.utils.components.PvToast
import tech.thatgravyboat.skyblockapi.api.events.base.Subscription
import tech.thatgravyboat.skyblockapi.utils.extentions.toTitleCase

@Module
object PageIssueManager {
    var issues: PageIssue? = null

    fun notifyGlobal() {
        issues?.global?.let { issue ->
            if (issue.isBlank() || issue.isEmpty()) return
            PvToast.addPageIssueToast("Global Issue", issue)
        }
    }

    fun notifyTab(tab: PvTab) {
        issues?.tabs[tab.name]?.let { issue ->
            PvToast.addPageIssueToast("${tab.name.toTitleCase()} Issue", issue)
        }
    }

    @Subscription
    fun onRemote(event: FinishRepoLoadingEvent) {
        issues = Utils.loadRemoteRepoData("pv/issues", SkyBlockPvCodecs.getCodec<PageIssue>())
    }

    @GenerateCodec
    data class PageIssue(
        val global: String?,
        val tabs: Map<String, String> = emptyMap(),
    )
}
