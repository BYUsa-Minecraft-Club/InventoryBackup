package edu.byu.minecraft;

import edu.byu.minecraft.invbackup.commands.Commands;
import edu.byu.minecraft.invbackup.data.LogType;
import edu.byu.minecraft.invbackup.data.PlayerBackupData;
import edu.byu.minecraft.invbackup.data.SaveData;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InventoryBackup implements ModInitializer {

    public static final String MOD_ID = "inventorybackup";

    public static final int maxSavesJoin = 10;

    public static final int maxSavesQuit = 10;

    public static final int maxSavesDeath = 10;

    public static final int maxSavesWorldChange = 10;

    public static final int maxSavesForce = 10;

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static SaveData data;


    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> data = SaveData.getServerState(server));

        ServerEntityEvents.ENTITY_UNLOAD.register((entity, world) -> {
            if (entity instanceof ServerPlayerEntity player && player.getHealth() > 0 && !player.isDisconnected()) {
                PlayerBackupData backupData =
                        new PlayerBackupData(player, LogType.WORLD_CHANGE, System.currentTimeMillis());
                InventoryBackup.data.addBackup(backupData);
            }
        });

        CommandRegistrationCallback.EVENT.register(Commands::register);
    }

}