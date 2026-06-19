package edu.byu.minecraft.invbackup.mixin;

import edu.byu.minecraft.InventoryBackup;
import edu.byu.minecraft.invbackup.data.LogType;
import edu.byu.minecraft.invbackup.data.PlayerBackupData;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public class PlayerManagerMixin {
    @Inject(method = "placeNewPlayer", at = @At("TAIL"))
    public void injectOnPlayerConnect(Connection connection, ServerPlayer player,
                                      CommonListenerCookie clientData, CallbackInfo ci) {
        InventoryBackup.data.checkPlayer(player);
        PlayerBackupData backupData = PlayerBackupData.forPlayer(player, LogType.JOIN);
        InventoryBackup.data.addBackup(backupData);
    }

    @Inject(method="remove", at=@At("HEAD"))
    public void injectRemove(ServerPlayer player, CallbackInfo ci) {
        PlayerBackupData backupData = PlayerBackupData.forPlayer(player, LogType.QUIT);
        InventoryBackup.data.addBackup(backupData);
    }
}
