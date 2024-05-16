package edu.byu.minecraft;

import edu.byu.minecraft.invbackup.commands.Commands;
import edu.byu.minecraft.invbackup.data.LogType;
import edu.byu.minecraft.invbackup.data.PlayerBackupData;
import edu.byu.minecraft.invbackup.data.SaveData;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InventoryBackup implements ModInitializer {

    public static final String MOD_ID = "inventorybackup";

    private static final int MAX_SAVES_JOIN = 10;

    private static final int MAX_SAVES_QUIT = 10;

    private static final int MAX_SAVES_DEATH = 10;

    private static final int MAX_SAVES_WORLD_CHANGE = 10;

    private static final int MAX_SAVES_FORCE = 10;

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static SaveData data;


    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(this::serverStarted);
        ServerEntityEvents.ENTITY_UNLOAD.register(this::entityUnload);
        CommandRegistrationCallback.EVENT.register(Commands::register);
    }

    private void serverStarted(MinecraftServer server) {
        data = SaveData.getServerState(server);
    }

    private void entityUnload(Entity entity, ServerWorld world) {
        if (entity instanceof ServerPlayerEntity player && player.getHealth() > 0 && !player.isDisconnected()) {
            PlayerBackupData backupData = new PlayerBackupData(player, LogType.WORLD_CHANGE);
            InventoryBackup.data.addBackup(backupData);
        }
    }

    public static int maxSaves(LogType type) {
        return switch (type) {
            case JOIN -> InventoryBackup.MAX_SAVES_JOIN;
            case QUIT -> InventoryBackup.MAX_SAVES_QUIT;
            case DEATH -> InventoryBackup.MAX_SAVES_DEATH;
            case WORLD_CHANGE -> InventoryBackup.MAX_SAVES_WORLD_CHANGE;
            case FORCE -> InventoryBackup.MAX_SAVES_FORCE;
        };
    }

}