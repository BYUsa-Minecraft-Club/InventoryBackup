package edu.byu.minecraft.invbackup.mixin;

import edu.byu.minecraft.InventoryBackup;
import edu.byu.minecraft.invbackup.data.LogType;
import edu.byu.minecraft.invbackup.data.PlayerBackupData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class ServerPlayerEntityMixin {

    @Inject(method = "die", at = @At("HEAD"))
    public void injectOnDeath(DamageSource damageSource, CallbackInfo ci) {
        ServerPlayer player = ((ServerPlayer) (Object) this);
        String deathMessage = player.getCombatTracker().getDeathMessage().getString();
        PlayerBackupData backupData = PlayerBackupData.forPlayer(player, LogType.DEATH, deathMessage);
        InventoryBackup.data.addBackup(backupData);
    }

}
