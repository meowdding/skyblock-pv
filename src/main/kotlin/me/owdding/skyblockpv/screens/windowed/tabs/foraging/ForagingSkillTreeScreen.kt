package me.owdding.skyblockpv.screens.windowed.tabs.foraging

import com.mojang.authlib.GameProfile
import me.owdding.lib.displays.DisplayWidget
import me.owdding.lib.repo.TreeNode
import me.owdding.lib.repo.TreeRepoData
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.data.api.skills.SkillTree
import me.owdding.skyblockpv.screens.windowed.tabs.base.SkillTreeItems
import me.owdding.skyblockpv.screens.windowed.tabs.base.SkillTreeScreen
import net.minecraft.client.gui.layouts.Layout

class ForagingSkillTreeScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : SkillTreeScreen, BaseForagingScreen(gameProfile, profile) {
    override val treeType: String = "hotf"
    override val coreNode: String = "core_of_the_mountain"
    override val items: SkillTreeItems = SkillTreeItems.FORAGING
    override val type: ForagingCategory = ForagingCategory.HOTF

    override fun skillTree(): SkillTree? = profile.skillTrees?.foraging
    override fun nodes(): List<TreeNode> = TreeRepoData.hotf

    override fun getLayout(bg: DisplayWidget): Layout = createLayout(bg)
}
