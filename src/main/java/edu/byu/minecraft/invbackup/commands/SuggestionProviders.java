package edu.byu.minecraft.invbackup.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import edu.byu.minecraft.InventoryBackup;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class SuggestionProviders {
    public static CompletableFuture<Suggestions> allPlayers(CommandContext<ServerCommandSource> ctx, SuggestionsBuilder builder) {
        InventoryBackup.data.getPlayers().values().stream()
                .filter(s -> CommandSource.shouldSuggest(builder.getRemaining().toLowerCase(), s.toLowerCase()))
                .forEach(builder::suggest);
        return builder.buildFuture();
    }
}
