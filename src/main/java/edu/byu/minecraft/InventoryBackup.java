package edu.byu.minecraft;

import com.mojang.authlib.GameProfile;
import edu.byu.minecraft.invbackup.commands.Commands;
import edu.byu.minecraft.invbackup.data.LogType;
import edu.byu.minecraft.invbackup.data.PlayerBackupData;
import edu.byu.minecraft.invbackup.data.SaveData;
import edu.byu.minecraft.invbackup.mixin.PlayerManagerAccessor;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.TagValueInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;

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

    private void entityUnload(Entity entity, ServerLevel world) {
        if (entity instanceof ServerPlayer player && player.getHealth() > 0 && !player.hasDisconnected()) {
            PlayerBackupData backupData = PlayerBackupData.forPlayer(player, LogType.WORLD_CHANGE);
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

    public static ServerPlayer getPlayer(String playerName, MinecraftServer server) {
        ServerPlayer requestedPlayer = server.getPlayerList().getPlayerByName(playerName);

        if (requestedPlayer == null) {
            UUID uuid = InventoryBackup.data.getPlayers().entrySet().stream()
                    .filter(entry -> playerName.equals(entry.getValue()))
                    .findAny().map(Map.Entry::getKey).orElse(null);
            if(uuid == null) {
                throw new RuntimeException("Cannot find player with name " + playerName);
            }
            GameProfile profile = new GameProfile(uuid, playerName);
            requestedPlayer = new ServerPlayer(server, server.overworld(), profile, ClientInformation.createDefault());
            CompoundTag nbt = server.getPlayerList().loadPlayerData(new NameAndId(profile)).orElseThrow();
            requestedPlayer.load(TagValueInput.create(new ProblemReporter.Collector(() -> MOD_ID), server.registryAccess(), nbt));
        }

        return requestedPlayer;
    }

    public static void savePlayerData(ServerPlayer player) {
        PlayerList pm = player.level().getServer().getPlayerList();
        if(!pm.getPlayers().contains(player)) {
            ((PlayerManagerAccessor) pm).callSavePlayerData(player);
        }
    }

}