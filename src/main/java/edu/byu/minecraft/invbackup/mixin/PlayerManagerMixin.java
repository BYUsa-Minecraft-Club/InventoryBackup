package edu.byu.minecraft.invbackup.mixin;

import edu.byu.minecraft.InventoryBackup;
import edu.byu.minecraft.invbackup.data.LogType;
import edu.byu.minecraft.invbackup.data.PlayerBackupData;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
    @Inject(method = "onPlayerConnect", at = @At("TAIL"))
    public void injectOnPlayerConnect(ClientConnection connection, ServerPlayerEntity player,
                                      ConnectedClientData clientData, CallbackInfo ci) {
        InventoryBackup.data.checkPlayer(player);
        PlayerBackupData backupData = new PlayerBackupData(player, LogType.JOIN);
        InventoryBackup.data.addBackup(backupData);
    }

    @Inject(method="remove", at=@At("HEAD"))
    public void injectRemove(ServerPlayerEntity player, CallbackInfo ci) {
        PlayerBackupData backupData = new PlayerBackupData(player, LogType.QUIT);
        InventoryBackup.data.addBackup(backupData);
    }
}
