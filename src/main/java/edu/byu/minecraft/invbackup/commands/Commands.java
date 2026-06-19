package edu.byu.minecraft.invbackup.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import edu.byu.minecraft.InventoryBackup;
import edu.byu.minecraft.invbackup.data.LogType;
import edu.byu.minecraft.invbackup.data.PlayerBackupData;
import edu.byu.minecraft.invbackup.gui.AllBackupListGui;
import edu.byu.minecraft.invbackup.gui.PlayerBackupListGui;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands.CommandSelection;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import java.util.function.Predicate;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class Commands {


    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess,
                                CommandSelection environment) {
        dispatcher.register(literal("invbackup").requires(Permissions.require("inventorybackup", 3)
                        .and(((Predicate<CommandSourceStack>) CommandSourceStack::isPlayer)))
                .then(literal("restore").executes(Commands::listAll)
                        .then(argument("player", StringArgumentType.word())
                                .suggests(SuggestionProviders::allPlayers)
                                .executes(Commands::listPlayer)))
                .then(literal("forcebackup").executes(Commands::forceBackupAll)
                        .then(argument("player", EntityArgument.player()).executes(Commands::forceBackupPlayer)))
                .then(literal("help").executes(Commands::help))
                .executes(Commands::help)
        );
    }


    private static int listAll(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer executor = context.getSource().getPlayer();
        try {
            new AllBackupListGui(executor).open();
        } catch (Exception e) {
            InventoryBackup.LOGGER.error("Error listing backups", e);
        }
        return 0;
    }


    private static int listPlayer(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer executor = context.getSource().getPlayer();
        ServerPlayer target = getPlayer(context);
        try {
            new PlayerBackupListGui(target.getUUID(), target.getGameProfile().name(), executor).open();
        } catch (Exception e) {
            InventoryBackup.LOGGER.error("Error listing backups for player {}",
                    EntityArgument.getPlayer(context, "player").getName(), e);
        }
        return 0;
    }


    private static int forceBackupAll(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        try {
            for (ServerPlayer player : context.getSource().getServer().getPlayerList().getPlayers()) {
                PlayerBackupData backupData = PlayerBackupData.forPlayer(player, LogType.FORCE);
                InventoryBackup.data.addBackup(backupData);
            }
            context.getSource().sendSystemMessage(Component.nullToEmpty("Backups created"));
        }
        catch (Exception e) {
            InventoryBackup.LOGGER.error("Error creating backups for all players", e);
        }
        return 0;
    }


    private static int forceBackupPlayer(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        try {
            ServerPlayer executor = context.getSource().getPlayer();
            ServerPlayer target = EntityArgument.getPlayer(context, "player");
            PlayerBackupData backupData = PlayerBackupData.forPlayer(target, LogType.FORCE);
            InventoryBackup.data.addBackup(backupData);
            assert executor != null;
            executor.sendSystemMessage(Component.nullToEmpty("Backup created"));
        }
        catch (Exception e) {
            InventoryBackup.LOGGER.error("Error creating backup for player {}",
                    EntityArgument.getPlayer(context, "player").getName(), e);
        }
        return 0;
    }

    private static int help(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer executor = context.getSource().getPlayer();
        assert executor != null;
        executor.sendSystemMessage(Component.nullToEmpty("""
                Usage for /invbackup:
                    /invbackup restore                  - Opens menu of all players to restore
                    /invbackup restore <username>       - Opens menu of player with username
                    /invbackup forcebackup              - Creates backups of all online players
                    /invbackup forcebackup <username>   - Creates backups of player with username
                """));
        return 0;
    }

    public static ServerPlayer getPlayer(CommandContext<CommandSourceStack> context) {
        String playerName = StringArgumentType.getString(context, "player");
        MinecraftServer server = context.getSource().getServer();
        return InventoryBackup.getPlayer(playerName, server);
    }


}

