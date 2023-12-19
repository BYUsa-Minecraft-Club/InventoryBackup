package edu.byu.minecraft.invbackup.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import edu.byu.minecraft.InventoryBackup;
import edu.byu.minecraft.invbackup.data.LogType;
import edu.byu.minecraft.invbackup.data.PlayerBackupData;
import edu.byu.minecraft.invbackup.gui.AllBackupListGui;
import edu.byu.minecraft.invbackup.gui.PlayerBackupListGui;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager.RegistrationEnvironment;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.function.Predicate;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class Commands {


    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess,
                                RegistrationEnvironment environment) {
        dispatcher.register(literal("invbackup").requires(Permissions.require("inventorybackup", 3)
                        .and(((Predicate<ServerCommandSource>) ServerCommandSource::isExecutedByPlayer)))
                .then(literal("restore").executes(Commands::listAll)
                        .then(argument("player", EntityArgumentType.player()).executes(Commands::listPlayer)))
                .then(literal("forcebackup").executes(Commands::forceBackupAll)
                        .then(argument("player", EntityArgumentType.player()).executes(Commands::forceBackupPlayer))));
    }


    private static int listAll(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity executor = context.getSource().getPlayer();
        try {
            new AllBackupListGui(executor).open();
        } catch (Exception e) {
            executor.sendMessage(Text.of(e.getMessage()));
        }
        return 0;
    }


    private static int listPlayer(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity executor = context.getSource().getPlayer();
        ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");
        try {
            new PlayerBackupListGui(target.getUuid(), target.getGameProfile().getName(), executor).open();
        } catch (Exception e) {
            executor.sendMessage(Text.of(e.getMessage()));
        }
        return 0;
    }


    private static int forceBackupAll(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity executor = context.getSource().getPlayer();
        for (ServerPlayerEntity player : executor.getServer().getPlayerManager().getPlayerList()) {
            PlayerBackupData backupData = new PlayerBackupData(player, LogType.FORCE, System.currentTimeMillis());
            InventoryBackup.data.addBackup(backupData);
        }
        executor.sendMessage(Text.of("Backups created"));
        return 0;
    }


    private static int forceBackupPlayer(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity executor = context.getSource().getPlayer();
        ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");
        PlayerBackupData backupData = new PlayerBackupData(target, LogType.FORCE, System.currentTimeMillis());
        InventoryBackup.data.addBackup(backupData);
        executor.sendMessage(Text.of("Backup created"));
        return 0;
    }


}

