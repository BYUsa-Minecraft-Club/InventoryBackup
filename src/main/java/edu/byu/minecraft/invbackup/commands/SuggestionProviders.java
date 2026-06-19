package edu.byu.minecraft.invbackup.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import edu.byu.minecraft.InventoryBackup;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;

public class SuggestionProviders {
    public static CompletableFuture<Suggestions> allPlayers(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        List<String> onlinePlayers = List.of(ctx.getSource().getServer().getPlayerList().getPlayerNamesArray());
        Map<Boolean, List<String>> hi = InventoryBackup.data.getPlayers().values().stream()
                .filter(s -> SharedSuggestionProvider.matchesSubStr(builder.getRemaining().toLowerCase(), s.toLowerCase()))
                .collect(Collectors.partitioningBy(onlinePlayers::contains));
        List<String> suggestions = hi.get(!hi.get(true).isEmpty());
        suggestions.forEach(builder::suggest);
        return builder.buildFuture();
    }
}
