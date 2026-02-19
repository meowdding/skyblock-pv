package me.owdding.skyblockpv.screens.windowed.tabs.mining

import com.mojang.authlib.GameProfile
import me.owdding.lib.displays.DisplayWidget
import me.owdding.lib.repo.TreeNode
import me.owdding.lib.repo.TreeRepoData
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.data.api.skills.SkillTree
import me.owdding.skyblockpv.screens.windowed.tabs.base.SkillTreeItems
import me.owdding.skyblockpv.screens.windowed.tabs.base.SkillTreeScreen
import net.minecraft.client.gui.layouts.Layout

class MiningSkillTreeScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : SkillTreeScreen, BaseMiningScreen(gameProfile, profile) {
    override val treeType: String = "hotm"
    override val coreNode: String = "center_of_the_forest"
    override val items: SkillTreeItems = SkillTreeItems.MINING

    override fun skillTree(): SkillTree? = profile.skillTrees?.mining
    override fun nodes(): List<TreeNode> = TreeRepoData.hotm

    override fun getLayout(bg: DisplayWidget): Layout = createLayout(bg)
}
