package edu.byu.minecraft;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.DimensionArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.server.command.AttributeCommand;
import net.minecraft.server.command.CommandManager.RegistrationEnvironment;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

import java.util.function.Predicate;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class Commands {

    private static final CommandSyntaxException NO_INV_FOUND =
            new SimpleCommandExceptionType(Text.literal("Requested Inventory does not exist")).create();


    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess,
                                RegistrationEnvironment environment) {
        dispatcher.register(literal("invbackup").requires(Permissions.require("inventorybackup", 3)
                        .and(((Predicate<ServerCommandSource>) ServerCommandSource::isExecutedByPlayer)))
                .then(literal("view").then(argument("dimension", DimensionArgumentType.dimension())
                        .then(argument("player", EntityArgumentType.player())
                        .then(argument("number", IntegerArgumentType.integer(1, InventoryBackup.MAX_BACKUP_SIZE))
                        .executes(Commands::view)))))
                .then(literal("restore").then(argument("dimension", DimensionArgumentType.dimension())
                        .then(argument("player", EntityArgumentType.player())
                        .then(argument("number", IntegerArgumentType.integer(1, InventoryBackup.MAX_BACKUP_SIZE))
                        .executes(Commands::restore))))));
    }


    private static int restore(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerWorld world = DimensionArgumentType.getDimensionArgument(ctx, "dimension");
        ServerPlayerEntity player = EntityArgumentType.getPlayer(ctx, "player");
        int number = IntegerArgumentType.getInteger(ctx, "number");

        PlayerInventory inv = InventoryBackup.data.retrieve(world.getRegistryKey().getValue(), player.getEntityName(), number - 1);
        if(inv == null) throw NO_INV_FOUND;

        player.getInventory().clone(inv);

        return 1;
    }


    private static int view(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity executor = ctx.getSource().getPlayerOrThrow();
        InventoryBackup.data.save(executor);

        ServerWorld world = DimensionArgumentType.getDimensionArgument(ctx, "dimension");
        ServerPlayerEntity player = EntityArgumentType.getPlayer(ctx, "player");
        int number = IntegerArgumentType.getInteger(ctx, "number");

        PlayerInventory inv = InventoryBackup.data.retrieve(world.getRegistryKey().getValue(), player.getEntityName(), number - 1);
        if(inv == null) throw NO_INV_FOUND;

        executor.getInventory().clone(inv);

        return 1;
    }

}

