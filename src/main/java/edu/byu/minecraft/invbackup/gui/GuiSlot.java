package edu.byu.minecraft.invbackup.gui;

import edu.byu.minecraft.invbackup.data.PlayerBackupData;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilderInterface;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.rmi.registry.Registry;
import java.util.UUID;

public record GuiSlot(@Nullable GuiElementInterface element, @Nullable Slot slot) {
    private static final GuiSlot EMPTY = GuiSlot.of(new GuiElement(ItemStack.EMPTY, GuiElementInterface.EMPTY_CALLBACK));

    public static GuiSlot of(GuiElementInterface element) {
        return new GuiSlot(element, null);
    }

    public static GuiSlot of(GuiElementBuilderInterface<?> element) {
        return new GuiSlot(element.build(), null);
    }

    public static GuiSlot of(Slot slot) {
        return new GuiSlot(null, slot);
    }

    public static GuiSlot nextPage(PagedGui gui) {
        if (gui.canNextPage()) {
            return GuiSlot.of(
                    Config.nextButton.get(true).builder()
                            .setCallback((x, y, z) -> {
                                PagedGui.playClickSound(gui.getPlayer());
                                gui.nextPage();
                            })
            );
        } else {
            return GuiSlot.of(Config.nextButton.get(false).builder());
        }
    }

    public static GuiSlot previousPage(PagedGui gui) {

        if (gui.canPreviousPage()) {
            return GuiSlot.of(
                    Config.previousButton.get(true).builder()
                            .setCallback((x, y, z) -> {
                                PagedGui.playClickSound(gui.getPlayer());
                                gui.previousPage();
                            })
            );
        } else {
            return GuiSlot.of(Config.previousButton.get(false).builder()
            );
        }
    }

    public static GuiSlot empty() {
        return EMPTY;
    }

    public static GuiSlot back(Runnable back) {
        return GuiSlot.of(
                Config.backButton.builder()
                        .setCallback((x, y, z, d) -> {
                            PagedGui.playClickSound(d.getPlayer());
                            back.run();
                        })
        );
    }

    public static GuiSlot teleport(ServerPlayerEntity player, Identifier world, Vec3d pos) {
        return GuiSlot.of(Config.teleportButton.builder().setCallback(() -> {
            PagedGui.playClickSound(player);
            MinecraftServer server = player.getServer();
            if (server == null) return;
            ServerWorld toWorld = server.getWorld(RegistryKey.of(RegistryKeys.WORLD, world));
            if (toWorld == null) return;

            player.teleport(toWorld, pos.getX(), pos.getY(), pos.getZ(), 0, 0);
            player.teleport(pos.getX(), pos.getY(), pos.getZ());
        }));
    }

    public static GuiSlot restore(MinecraftServer server, PlayerBackupData data) {
        ServerPlayerEntity targetPlayer = server.getPlayerManager().getPlayer(data.getUuid());
        if (targetPlayer == null) return GuiSlot.of(Config.restoreOfflineButton.builder());
        else return GuiSlot.of(Config.restoreButton.builder().setCallback(() -> {
            targetPlayer.getInventory().clone(data.getMainInventory());
            for (int i = 0; i < data.getEnderChest().size(); i++) {
                targetPlayer.getEnderChestInventory().setStack(i, data.getEnderChest().getStack(i));
            }
            targetPlayer.setExperienceLevel(data.getExperienceLevel());
            targetPlayer.setExperiencePoints((int)(data.getExperienceProgress() * targetPlayer.getNextLevelExperience()));
        }));
    }
}
