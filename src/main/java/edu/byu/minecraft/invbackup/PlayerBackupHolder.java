package edu.byu.minecraft.invbackup;

import edu.byu.minecraft.invbackup.data.LogType;
import edu.byu.minecraft.invbackup.data.PlayerBackupData;

import java.util.EnumMap;
import java.util.List;

public interface PlayerBackupHolder {
    void inventoryBackup$addBackup(PlayerBackupData backup);
    EnumMap<LogType, List<PlayerBackupData>> getPlayerBackups();
}
