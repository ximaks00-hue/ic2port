package dev.ic2port.menu;

import dev.ic2port.blockentity.PersonalTankBlockEntity;
import dev.ic2port.setup.MenuTypeRegistry;
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

public class PersonalTankMenu extends AbstractContainerMenu {

    private final PersonalTankBlockEntity blockEntity;
    private final ContainerData data;

    public PersonalTankMenu(final int containerId, final Inventory playerInventory, final FriendlyByteBuf extraData) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, extraData), new SimpleContainerData(2));
    }

    public PersonalTankMenu(
            final int containerId,
            final Inventory playerInventory,
            final PersonalTankBlockEntity blockEntity) {
        this(containerId, playerInventory, blockEntity, new SimpleContainerData(2));
    }

    public PersonalTankMenu(
            final int containerId,
            final Inventory playerInventory,
            final PersonalTankBlockEntity blockEntity,
            final ContainerData data) {
        super(MenuTypeRegistry.PERSONAL_TANK_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        this.data = data;

        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
        addDataSlots(data);
    }

    public BlockPos getBlockPos() {
        return blockEntity.getBlockPos();
    }

    public int getFluidScaled(final int scale) {
        int capacity = data.get(1);
        if (capacity <= 0) {
            return 0;
        }
        return data.get(0) * scale / capacity;
    }

    public int getFriendCount() {
        return blockEntity.getFriends().size();
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
        return ItemStack.EMPTY;
    }

    private static PersonalTankBlockEntity getBlockEntity(
            final Inventory playerInventory,
            final FriendlyByteBuf extraData) {
        BlockEntity blockEntity = playerInventory.player.level().getBlockEntity(extraData.readBlockPos());
        if (blockEntity instanceof PersonalTankBlockEntity tank) {
            return tank;
        }
        throw new IllegalStateException("Personal tank block entity not found");
    }

    private void addPlayerInventory(final Inventory playerInventory) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
    }

    private void addPlayerHotbar(final Inventory playerInventory) {
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }
}
