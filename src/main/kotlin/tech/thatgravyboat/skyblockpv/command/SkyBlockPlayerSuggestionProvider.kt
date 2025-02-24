package tech.thatgravyboat.skyblockpv.command

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
    override fun getSuggestions(context: CommandContext<FabricClientCommandSource?>, builder: SuggestionsBuilder): CompletableFuture<Suggestions?>? {
        PartyAPI.members.forEach { member ->
            if (SharedSuggestionProvider.matchesSubStr(builder.remaining, member.name)) builder.suggest(member.name)
        }
        FriendsAPI.friends.forEach { friend ->
            if (SharedSuggestionProvider.matchesSubStr(builder.remaining, friend.name)) builder.suggest(friend.name)
        }
        McClient.players.forEach { playerInfo ->
            if (SharedSuggestionProvider.matchesSubStr(builder.remaining, playerInfo.profile.name)) builder.suggest(playerInfo.profile.name)
        }
        return builder.buildFuture()
    }
}
