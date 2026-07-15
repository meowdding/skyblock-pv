package me.owdding.skyblockpv.screens.windowed.tabs.foraging

import com.mojang.authlib.GameProfile
import me.owdding.lib.displays.DisplayWidget
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.data.api.skills.SkillTreeType
import me.owdding.skyblockpv.screens.windowed.tabs.base.LoadoutSkillTreeScreen
import net.minecraft.client.gui.layouts.Layout

class ForagingSkillTreeScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : LoadoutSkillTreeScreen, BaseForagingScreen(gameProfile, profile) {
    override val skillTreeType: SkillTreeType = SkillTreeType.FORAGING
    override val type: ForagingCategory = ForagingCategory.HOTF

    override var selected: Int = profile?.skillTrees?.selectedForagingTree ?: 1
    override fun rebuildScreen() = safelyRebuild()

    override fun getLayout(bg: DisplayWidget): Layout = createLayout(bg)
}
