package me.owdding.skyblockpv.api.data.abstraction

interface HotmDataGetter {

    val nodes: Map<String, Int>
    val toggledNodes: List<String>
    val experience: Long
    val miningAbility: String?

    data class Layered(val entries: List<HotmDataGetter>) : HotmDataGetter {
        override val nodes: Map<String, Int> = entries.firstNotNullOfOrNull { it.nodes.takeUnless { it.isEmpty() } } ?: emptyMap()
        override val toggledNodes: List<String> = entries.firstNotNullOfOrNull { it.toggledNodes.takeUnless { it.isEmpty() } } ?: emptyList()
        override val experience: Long = entries.maxOf { it.experience }
        override val miningAbility: String? = entries.firstNotNullOfOrNull { it.miningAbility }

        override fun getAbilityLevel(): Int = entries.maxOf { it.getAbilityLevel() }
        override fun getHotmLevel(): Int = entries.maxOf { it.getHotmLevel() }
    }

    companion object {
        val levelToExp = mapOf(
            1 to 0,
            2 to 3_000,
            3 to 12_000,
            4 to 37_000,
            5 to 97_000,
            6 to 197_000,
            7 to 347_000,
            8 to 557_000,
            9 to 847_000,
            10 to 1_247_000,
        )
    }

    fun getAbilityLevel() =  if ((nodes["special_0"] ?: 0) < 1) 1 else 2
    fun getHotmLevel(): Int = levelToExp.entries.findLast { it.value <= experience }?.key ?: 0

    fun getXpToNextLevel() = experience - (levelToExp[getHotmLevel()] ?: 0)
    fun getXpRequiredForNextLevel(): Int {
        val level = (getHotmLevel() + 1).coerceAtMost(10)
        return (levelToExp[level] ?: 0) - (levelToExp[level - 1] ?: 0)
    }

}
