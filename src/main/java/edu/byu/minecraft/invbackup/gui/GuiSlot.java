package edu.byu.minecraft.invbackup.gui;

import edu.byu.minecraft.InventoryBackup;
import edu.byu.minecraft.invbackup.data.PlayerBackupData;
import eu.pb4.sgui.api.elements.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public record GuiSlot(@Nullable GuiElement element, @Nullable Slot slot) {

    static final GuiSlot EMPTY;
    static final ItemStack EMPTY_STACK;
    static {
        EMPTY_STACK = new ItemStack(Items.BLACK_STAINED_GLASS_PANE);
        EMPTY_STACK.set(DataComponents.ITEM_NAME, Component.empty());
        EMPTY = GuiSlot.of(GuiElementBuilder.from(EMPTY_STACK).hideTooltip().hideDefaultTooltip());
    }


    public static GuiSlot of(GuiElementBuilderCreator<?> element) {
        return new GuiSlot(element.build(), null);
    }

    public static GuiSlot of(Slot slot) {
        return new GuiSlot(null, slot);
    }

    public static GuiElementBuilder builder(Item base, String... text) {
        return builder(base.getDefaultInstance(), text);
    }

    public static GuiElementBuilder builder(ItemStack stack, String... text) {
        List<Component> list = new ArrayList<>();
        for (String s : text) {
            list.add(Component.nullToEmpty(s));
        }
        if (list.isEmpty()) {
            list.add(Component.empty());
        }
        return GuiElementBuilder.from(stack).setName(list.removeFirst()).setLore(list);
    }


    public static GuiSlot nextPage(PagedGui gui) {
        if (gui.canNextPage()) {
            return GuiSlot.of(GuiConfig.NEXT_PAGE_BUTTON.setCallback(() -> {
                PagedGui.playClickSound(gui.getPlayer());
                gui.nextPage();
            }));
        } else {
            return EMPTY;
        }
    }


    public static GuiSlot previousPage(PagedGui gui) {
        if (gui.canPreviousPage()) {
            return GuiSlot.of(GuiConfig.PREVIOUS_PAGE_BUTTON.setCallback(() -> {
                PagedGui.playClickSound(gui.getPlayer());
                gui.previousPage();
            }));
        } else {
            return EMPTY;
        }
    }


    public static GuiSlot empty() {
        return EMPTY;
    }


    public static GuiSlot back(Runnable back) {
        return GuiSlot.of(GuiConfig.PREVIOUS_MENU_BUTTON.setCallback((x, y, z, d) -> {
            PagedGui.playClickSound(d.getPlayer());
            back.run();
        }));
    }


    public static GuiSlot teleport(ServerPlayer player, Identifier world, Vec3 pos, String target) {
        return GuiSlot.of(GuiConfig.teleportButton(target).setCallback(() -> {
            PagedGui.playClickSound(player);
            ServerLevel destWorld = player.level().getServer().getLevel(ResourceKey.create(Registries.DIMENSION, world));
            if (destWorld == null) return;

            player.teleportTo(destWorld, pos.x(), pos.y(), pos.z(), Relative.DELTA, 0, 0, false);
            player.randomTeleport(pos.x(), pos.y(), pos.z(), false);
            PagedGui.playClickSound(player, BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.CHORUS_FRUIT_TELEPORT));
        }));
    }


    public static GuiSlot restore(MinecraftServer server, PlayerBackupData data) {
        String playerName = InventoryBackup.data.getPlayers().get(data.uuid());
        if(playerName == null) return GuiSlot.empty();
        return GuiSlot.of(GuiConfig.RESTORE_INVENTORY_BUTTON.setCallback(() -> {
            ServerPlayer target = InventoryBackup.getPlayer(playerName, server);
            data.restore(target);
            InventoryBackup.savePlayerData(target);
        }));
    }

}
