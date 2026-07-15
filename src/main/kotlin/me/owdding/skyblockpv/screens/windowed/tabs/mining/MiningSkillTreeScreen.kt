package me.owdding.skyblockpv.screens.windowed.tabs.mining

import com.mojang.authlib.GameProfile
import me.owdding.lib.displays.DisplayWidget
import me.owdding.skyblockpv.api.data.profile.SkyBlockProfile
import me.owdding.skyblockpv.data.api.skills.SkillTreeType
import me.owdding.skyblockpv.screens.windowed.tabs.base.LoadoutSkillTreeScreen
import net.minecraft.client.gui.layouts.Layout

class MiningSkillTreeScreen(gameProfile: GameProfile, profile: SkyBlockProfile? = null) : LoadoutSkillTreeScreen, BaseMiningScreen(gameProfile, profile) {
    override var selected: Int = profile?.skillTrees?.selectedMiningTree ?: 1

    override val skillTreeType: SkillTreeType = SkillTreeType.MINING
    override fun rebuildScreen() = safelyRebuild()

    override fun getLayout(bg: DisplayWidget): Layout = createLayout(bg)
}
