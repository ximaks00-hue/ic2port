package dev.ic2port.menu;

import dev.ic2port.blockentity.MachineBufferBlockEntity;
import dev.ic2port.item.ITransportUpgrade;
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

public class MachineBufferMenu extends AbstractContainerMenu {

    private final MachineBufferBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;

    public MachineBufferMenu(final int containerId, final Inventory playerInventory, final FriendlyByteBuf extraData) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, extraData), new SimpleContainerData(2));
    }

    public MachineBufferMenu(
            final int containerId,
            final Inventory playerInventory,
            final MachineBufferBlockEntity blockEntity,
            final ContainerData data) {
        super(MenuTypeRegistry.MACHINE_BUFFER_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        this.level = playerInventory.player.level();
        this.data = data;

        IItemHandler handler = blockEntity.getFullItemHandler();
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int index = col + row * 3;
                this.addSlot(new SlotItemHandler(handler, index, 62 + col * 18, 17 + row * 18));
            }
        }
        for (int index = 0; index < MachineMenuLayout.UPGRADE_SLOT_COUNT; index++) {
            this.addSlot(new SlotItemHandler(handler, MachineBufferBlockEntity.UPGRADE_SLOT_START + index,
                    MachineMenuLayout.UPGRADE_SLOT_X,
                    MachineMenuLayout.UPGRADE_SLOT_START_Y + index * MachineMenuLayout.UPGRADE_SLOT_SPACING) {
                @Override
                public boolean mayPlace(final ItemStack stack) {
                    return stack.isEmpty() || stack.getItem() instanceof ITransportUpgrade;
                }
            });
        }

        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
        addDataSlots(data);
    }

    public int getEnergyScaled(final int scale) {
        int max = data.get(1);
        if (max <= 0) {
            return 0;
        }
        return data.get(0) * scale / max;
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
        int bufferEnd = MachineBufferBlockEntity.BUFFER_SLOTS;
        int upgradeEnd = MachineBufferBlockEntity.TOTAL_SLOTS;
        int playerStart = upgradeEnd;
        if (index < bufferEnd) {
            if (!this.moveItemStackTo(sourceStack, playerStart, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (index < upgradeEnd) {
            if (!this.moveItemStackTo(sourceStack, 0, bufferEnd, false)) {
                return ItemStack.EMPTY;
            }
        } else if (sourceStack.getItem() instanceof ITransportUpgrade) {
            if (!this.moveItemStackTo(sourceStack, bufferEnd, upgradeEnd, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(sourceStack, 0, bufferEnd, false)) {
            return ItemStack.EMPTY;
        }
        if (sourceStack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return result;
    }

    private static MachineBufferBlockEntity getBlockEntity(final Inventory playerInventory, final FriendlyByteBuf extraData) {
        BlockEntity blockEntity = playerInventory.player.level().getBlockEntity(extraData.readBlockPos());
        if (blockEntity instanceof MachineBufferBlockEntity buffer) {
            return buffer;
        }
        throw new IllegalStateException("Machine buffer block entity not found");
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
