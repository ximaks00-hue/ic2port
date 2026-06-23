package dev.ic2port.menu;

import dev.ic2port.blockentity.VillagerOMatBlockEntity;
import dev.ic2port.setup.MenuTypeRegistry;
import dev.ic2port.util.MachineMenuLayout;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class VillagerOMatMenu extends AbstractContainerMenu {

    private final VillagerOMatBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;

    public VillagerOMatMenu(final int containerId, final Inventory playerInventory, final FriendlyByteBuf extraData) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, extraData), new SimpleContainerData(8));
    }

    public VillagerOMatMenu(
            final int containerId,
            final Inventory playerInventory,
            final VillagerOMatBlockEntity blockEntity,
            final ContainerData data) {
        super(MenuTypeRegistry.VILLAGER_O_MAT_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        this.level = playerInventory.player.level();
        this.data = data;

        IItemHandler handler = blockEntity.getFullItemHandler();

        for (int slot = 0; slot < VillagerOMatBlockEntity.INPUT_SLOTS; slot++) {
            this.addSlot(new SlotItemHandler(handler, slot, 26 + slot * 18, 20));
        }
        for (int slot = 0; slot < VillagerOMatBlockEntity.OUTPUT_SLOTS; slot++) {
            int index = VillagerOMatBlockEntity.SLOT_OUTPUT_START + slot;
            this.addSlot(new SlotItemHandler(handler, index, 98 + slot * 18, 20) {
                @Override
                public boolean mayPlace(final ItemStack stack) {
                    return false;
                }
            });
        }

        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
        addDataSlots(data);
    }

    public VillagerOMatBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public int getEnergyScaled(final int scale) {
        int max = data.get(1);
        if (max <= 0) {
            return 0;
        }
        return data.get(0) * scale / max;
    }

    public int getCooldownScaled(final int scale) {
        return data.get(2) * scale / VillagerOMatBlockEntity.TRADE_INTERVAL;
    }

    public int getXpScaled(final int scale) {
        return Math.min(scale, data.get(3));
    }

    public int getSelectedVillagerIndex() {
        return data.get(4);
    }

    public int getVillagerCount() {
        return data.get(5);
    }

    public int getEnabledTradeMask() {
        return data.get(6);
    }

    public int getTradeCount() {
        return data.get(7);
    }

    public int getStoredXp() {
        return data.get(3);
    }

    public boolean isTradeEnabled(final int tradeIndex) {
        return (getEnabledTradeMask() & (1 << tradeIndex)) != 0;
    }

    @Override
    public boolean stillValid(final Player player) {
        return blockEntity != null
                && blockEntity.getLevel() == level
                && player.distanceToSqr(
                blockEntity.getBlockPos().getX() + 0.5D,
                blockEntity.getBlockPos().getY() + 0.5D,
                blockEntity.getBlockPos().getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public ItemStack quickMoveStack(final Player player, final int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack sourceStack = slot.getItem();
        result = sourceStack.copy();
        int inputEnd = VillagerOMatBlockEntity.INPUT_SLOTS;
        int outputEnd = VillagerOMatBlockEntity.TOTAL_SLOTS;
        int playerStart = outputEnd;
        if (index < inputEnd) {
            if (!this.moveItemStackTo(sourceStack, playerStart, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (index < outputEnd) {
            if (!this.moveItemStackTo(sourceStack, 0, inputEnd, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(sourceStack, 0, inputEnd, false)) {
            return ItemStack.EMPTY;
        }
        if (sourceStack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return result;
    }

    private static VillagerOMatBlockEntity getBlockEntity(final Inventory playerInventory, final FriendlyByteBuf extraData) {
        BlockEntity blockEntity = playerInventory.player.level().getBlockEntity(extraData.readBlockPos());
        if (blockEntity instanceof VillagerOMatBlockEntity mat) {
            return mat;
        }
        throw new IllegalStateException("Villager-O-Mat block entity not found");
    }

    private void addPlayerInventory(final Inventory playerInventory) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9,
                        8 + col * 18, MachineMenuLayout.PLAYER_INVENTORY_START_Y + row * 18));
            }
        }
    }

    private void addPlayerHotbar(final Inventory playerInventory) {
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, MachineMenuLayout.HOTBAR_Y));
        }
    }
}
