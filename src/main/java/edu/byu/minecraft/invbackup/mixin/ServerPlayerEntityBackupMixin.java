package edu.byu.minecraft.invbackup.mixin;

import edu.byu.minecraft.InventoryBackup;
import edu.byu.minecraft.invbackup.PlayerBackupHolder;
import edu.byu.minecraft.invbackup.data.LogType;
import edu.byu.minecraft.invbackup.data.PlayerBackupData;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;
import java.util.stream.Collectors;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityBackupMixin implements PlayerBackupHolder {

    private static final String BACKUP_KEY = InventoryBackup.MOD_ID + "-playerBackups";

    private final EnumMap<LogType, List<PlayerBackupData>> playerBackups = new EnumMap<>(LogType.class);

    @Inject(method = "readCustomData", at = @At("HEAD"))
    public void injectReadCustomData(ReadView baseReadView, CallbackInfo ci) {
        ReadView backupReadView = baseReadView.getReadView(BACKUP_KEY);
        for (LogType type : LogType.values()) {
            ReadView.TypedListReadView<PlayerBackupData> typedListView =
                    backupReadView.getTypedListView(type.name(), PlayerBackupData.CODEC);
            if (!typedListView.isEmpty()) {
                playerBackups.put(type, typedListView.stream().collect(Collectors.toCollection(ArrayList::new)));
            }
        }
    }

    @Inject(method = "writeCustomData", at = @At("HEAD"))
    public void injectWriteCustomData(WriteView baseWriteView, CallbackInfo ci) {
        WriteView backupWriteView = baseWriteView.get(BACKUP_KEY);
        for (Map.Entry<LogType, List<PlayerBackupData>> entry : playerBackups.entrySet()) {
            WriteView.ListAppender<PlayerBackupData> listAppender =
                    backupWriteView.getListAppender(entry.getKey().name(), PlayerBackupData.CODEC);
            for (PlayerBackupData backupData : entry.getValue()) {
                listAppender.add(backupData);
            }
        }
    }
    
    public void inventoryBackup$addBackup(PlayerBackupData backup) {
        if (!playerBackups.containsKey(backup.logType())) {
            playerBackups.put(backup.logType(), new LinkedList<>());
        }
        playerBackups.get(backup.logType()).add(backup);

        int max = InventoryBackup.maxSaves(backup.logType());

        while (max < playerBackups.get(backup.logType()).size()) {
            playerBackups.get(backup.logType()).removeFirst();
        }
    }

    public EnumMap<LogType, List<PlayerBackupData>> inventoryBackup$getPlayerBackups() {
        return playerBackups;
    }
}
