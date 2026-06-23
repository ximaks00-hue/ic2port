package dev.ic2port.menu;

import dev.ic2port.blockentity.OreScannerBlockEntity;
import dev.ic2port.setup.MenuTypeRegistry;
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

public class OreScannerMenu extends AbstractContainerMenu {

    private static final int PLAYER_INVENTORY_START_Y = 84;
    private static final int HOTBAR_Y = 142;

    private final OreScannerBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;

    public OreScannerMenu(final int containerId, final Inventory playerInventory, final FriendlyByteBuf extraData) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, extraData), new SimpleContainerData(5));
    }

    public OreScannerMenu(
            final int containerId,
            final Inventory playerInventory,
            final OreScannerBlockEntity blockEntity,
            final ContainerData data) {
        super(MenuTypeRegistry.ORE_SCANNER_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        this.level = playerInventory.player.level();
        this.data = data;
        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
        addDataSlots(data);
    }

    public int getEnergyScaled(final int height) {
        int energy = data.get(0);
        int max = data.get(1);
        if (max <= 0 || energy <= 0) {
            return 0;
        }
        return (int) ((long) energy * height / max);
    }

    public int getScanScaled(final int width) {
        int total = data.get(4);
        int progress = data.get(3);
        if (total <= 0) {
            return 0;
        }
        return (int) ((long) progress * width / total);
    }

    public boolean triggerScan(final Player player) {
        if (level.isClientSide) {
            return false;
        }
        return blockEntity.startScan(player);
    }

    @Override
    public ItemStack quickMoveStack(final Player player, final int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(final Player player) {
        return blockEntity != null
                && level.getBlockEntity(blockEntity.getBlockPos()) == blockEntity
                && player.distanceToSqr(
                        blockEntity.getBlockPos().getX() + 0.5D,
                        blockEntity.getBlockPos().getY() + 0.5D,
                        blockEntity.getBlockPos().getZ() + 0.5D) <= 64.0D;
    }

    private void addPlayerInventory(final Inventory playerInventory) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9,
                        8 + col * 18, PLAYER_INVENTORY_START_Y + row * 18));
            }
        }
    }

    private void addPlayerHotbar(final Inventory playerInventory) {
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, HOTBAR_Y));
        }
    }

    private static OreScannerBlockEntity getBlockEntity(
            final Inventory playerInventory,
            final FriendlyByteBuf extraData) {
        BlockEntity blockEntity = playerInventory.player.level().getBlockEntity(extraData.readBlockPos());
        if (blockEntity instanceof OreScannerBlockEntity scanner) {
            return scanner;
        }
        throw new IllegalStateException("Expected OreScannerBlockEntity at provided BlockPos");
    }
}
