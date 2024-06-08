package edu.byu.minecraft.invbackup.mixin;

import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(PlayerManager.class)
public interface PlayerManagerAccessor {
    @Invoker
    public void callSavePlayerData(ServerPlayerEntity player);
}
