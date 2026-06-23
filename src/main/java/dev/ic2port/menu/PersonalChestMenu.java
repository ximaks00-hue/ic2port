package dev.ic2port.menu;

import dev.ic2port.blockentity.PersonalChestBlockEntity;
import dev.ic2port.setup.MenuTypeRegistry;
import dev.ic2port.util.MachineMenuLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class PersonalChestMenu extends AbstractContainerMenu {

    private final PersonalChestBlockEntity blockEntity;
    private final ContainerData data;

    public PersonalChestMenu(final int containerId, final Inventory playerInventory, final FriendlyByteBuf extraData) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, extraData), new SimpleContainerData(1));
    }

    public PersonalChestMenu(
            final int containerId,
            final Inventory playerInventory,
            final PersonalChestBlockEntity blockEntity) {
        this(containerId, playerInventory, blockEntity, new SimpleContainerData(1));
    }

    public PersonalChestMenu(
            final int containerId,
            final Inventory playerInventory,
            final PersonalChestBlockEntity blockEntity,
            final ContainerData data) {
        super(MenuTypeRegistry.PERSONAL_CHEST_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        this.data = data;

        IItemHandler handler = blockEntity.getFullItemHandler();
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 9; col++) {
                int index = col + row * 9;
                this.addSlot(new SlotItemHandler(handler, index, 8 + col * 18, 18 + row * 18));
            }
        }

        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
        addDataSlots(data);
    }

    public BlockPos getBlockPos() {
        return blockEntity.getBlockPos();
    }

    public int getFriendCount() {
        return data.get(0);
    }

    public boolean isOwner(final Player player) {
        return blockEntity.getOwnerUuid() == null || blockEntity.getOwnerUuid().equals(player.getUUID());
    }

    @Override
    public boolean stillValid(final Player player) {
        return blockEntity != null
                && blockEntity.getLevel() == player.level()
                && blockEntity.canAccess(player)
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
        int chestSlots = PersonalChestBlockEntity.SLOT_COUNT;
        if (index < chestSlots) {
            if (!this.moveItemStackTo(sourceStack, chestSlots, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(sourceStack, 0, chestSlots, false)) {
            return ItemStack.EMPTY;
        }
        if (sourceStack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return result;
    }

    private static PersonalChestBlockEntity getBlockEntity(
            final Inventory playerInventory,
            final FriendlyByteBuf extraData) {
        BlockEntity blockEntity = playerInventory.player.level().getBlockEntity(extraData.readBlockPos());
        if (blockEntity instanceof PersonalChestBlockEntity chest) {
            return chest;
        }
        throw new IllegalStateException("Personal chest block entity not found");
    }

    private void addPlayerInventory(final Inventory playerInventory) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9,
                        8 + col * 18, 140 + row * 18));
            }
        }
    }

    private void addPlayerHotbar(final Inventory playerInventory) {
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 198));
        }
    }
}
