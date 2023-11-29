package edu.byu.minecraft;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InventoryBackup implements ModInitializer {

    public static final String MOD_ID = "inventorybackup";

    public static final int MAX_BACKUP_SIZE = 5;

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static SaveData data;


    @Override
    public void onInitialize() {
        //LOGGER.info("Hello Fabric world!");

        ServerLifecycleEvents.SERVER_STARTED.register(server -> data = SaveData.getServerState(server));

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTicks() % 2400 != 0) return;
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                data.save(player);
            }
        });

        //I believe this should save on death9
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> data.save(oldPlayer));

        ServerEntityEvents.ENTITY_UNLOAD.register((entity, world) -> {
            if(entity instanceof ServerPlayerEntity player) data.save(player);
        });

        CommandRegistrationCallback.EVENT.register(Commands::register);
    }

}