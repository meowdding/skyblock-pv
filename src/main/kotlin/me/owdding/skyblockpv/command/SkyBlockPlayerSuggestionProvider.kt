package me.owdding.skyblockpv.command

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.commands.SharedSuggestionProvider
import tech.thatgravyboat.skyblockapi.api.profile.friends.FriendsAPI
import tech.thatgravyboat.skyblockapi.api.profile.party.PartyAPI
import tech.thatgravyboat.skyblockapi.helpers.McClient
import java.util.concurrent.CompletableFuture

object SkyBlockPlayerSuggestionProvider : SuggestionProvider<FabricClientCommandSource> {
    fun getSuggestions(input: String) =
        collectAllNames(SuggestionTypes.PARTY, SuggestionTypes.FRIENDS).filter { input.isBlank() || canSuggest(it, input.lowercase()) }

    override fun getSuggestions(context: CommandContext<FabricClientCommandSource?>, builder: SuggestionsBuilder): CompletableFuture<Suggestions?>? {
        collectAllNames().filter { canSuggest(it, builder.remaining.lowercase()) }.forEach(builder::suggest)
        return builder.buildFuture()
    }

    private fun collectAllNames(vararg sources: SuggestionTypes = SuggestionTypes.entries.toTypedArray()) = sources.flatMap { it.supplier() }

    private fun canSuggest(name: String, input: String): Boolean = SharedSuggestionProvider.matchesSubStr(input, name.lowercase())

    enum class SuggestionTypes(val supplier: () -> List<String>) {
        PARTY({ PartyAPI.members.mapNotNull { it.name } }),
        FRIENDS({ FriendsAPI.friends.map { it.name } }),
        LOBBY({ McClient.players.map { it.profile.name!! } }),
        ;
    }
}
