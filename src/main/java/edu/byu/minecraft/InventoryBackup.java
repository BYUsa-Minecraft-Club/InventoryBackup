package edu.byu.minecraft;

import com.mojang.authlib.GameProfile;
import edu.byu.minecraft.invbackup.commands.Commands;
import edu.byu.minecraft.invbackup.data.GlobalSaveData;
import edu.byu.minecraft.invbackup.data.LogType;
import edu.byu.minecraft.invbackup.data.PlayerBackupData;
import edu.byu.minecraft.invbackup.PlayerBackupHolder;
import edu.byu.minecraft.invbackup.mixin.PlayerManagerAccessor;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.util.ErrorReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class InventoryBackup implements ModInitializer {

    public static final String MOD_ID = "inventorybackup";

    private static final int MAX_SAVES_JOIN = 10;

    private static final int MAX_SAVES_QUIT = 10;

    private static final int MAX_SAVES_DEATH = 10;

    private static final int MAX_SAVES_WORLD_CHANGE = 10;

    private static final int MAX_SAVES_FORCE = 10;

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static GlobalSaveData data;


    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(this::serverStarted);
        ServerEntityEvents.ENTITY_UNLOAD.register(this::entityUnload);
        CommandRegistrationCallback.EVENT.register(Commands::register);
    }

    private void serverStarted(MinecraftServer server) {
        data = GlobalSaveData.getServerState(server);
    }

    private void entityUnload(Entity entity, ServerWorld world) {
        if (entity instanceof ServerPlayerEntity player && player.getHealth() > 0 && !player.isDisconnected()) {
            PlayerBackupData backupData = PlayerBackupData.forPlayer(player, LogType.WORLD_CHANGE);
            ((PlayerBackupHolder) player).inventoryBackup$addBackup(backupData);
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

    public static ServerPlayerEntity getPlayer(String playerName, MinecraftServer server) {
        ServerPlayerEntity requestedPlayer = server.getPlayerManager().getPlayer(playerName);

        if (requestedPlayer == null) {
            UUID uuid = InventoryBackup.data.getPlayers().entrySet().stream()
                    .filter(entry -> playerName.equals(entry.getValue()))
                    .findAny().map(Map.Entry::getKey).orElse(null);
            if(uuid == null) {
                throw new RuntimeException("Cannot find player with name " + playerName);
            }
            GameProfile profile = new GameProfile(uuid, playerName);
            requestedPlayer = new ServerPlayerEntity(server, server.getOverworld(), profile, SyncedClientOptions.createDefault());
            Optional<ReadView> readViewOpt = server.getPlayerManager().loadPlayerData(requestedPlayer, new ErrorReporter.Impl(() -> MOD_ID));
        }

        return requestedPlayer;
    }

    public static void savePlayerData(ServerPlayerEntity player) {
        PlayerManager pm = Objects.requireNonNull(player.getServer()).getPlayerManager();
        if(!pm.getPlayerList().contains(player)) {
            ((PlayerManagerAccessor) pm).callSavePlayerData(player);
        }
    }

}