package dev.ic2port.menu;

import dev.ic2port.blockentity.CropmatronBlockEntity;
import dev.ic2port.setup.ItemRegistry;
import dev.ic2port.setup.MenuTypeRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class CropmatronMenu extends AbstractContainerMenu {

    private static final int SUPPLY_X = 80;
    private static final int SUPPLY_Y = 35;
    private static final int PLAYER_INVENTORY_START_Y = 84;
    private static final int HOTBAR_Y = 142;

    private final CropmatronBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;

    public CropmatronMenu(final int containerId, final Inventory playerInventory, final FriendlyByteBuf extraData) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, extraData), new SimpleContainerData(2));
    }

    public CropmatronMenu(
            final int containerId,
            final Inventory playerInventory,
            final CropmatronBlockEntity blockEntity,
            final ContainerData data) {
        super(MenuTypeRegistry.CROPMATRON_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        this.level = playerInventory.player.level();
        this.data = data;

        IItemHandler inputHandler = blockEntity.getFullItemHandler();
        this.addSlot(new SlotItemHandler(inputHandler, 0, SUPPLY_X, SUPPLY_Y) {
            @Override
            public boolean mayPlace(final ItemStack stack) {
                return stack.is(ItemRegistry.FERTILIZER.get())
                        || stack.is(ItemRegistry.HYDRATION_CELL.get())
                        || stack.is(ItemRegistry.WEED_EX.get());
            }
        });

        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
        addDataSlots(data);
    }

    public int getEnergyScaled(final int height) {
        int max = data.get(1);
        return max <= 0 ? 0 : data.get(0) * height / max;
    }

    public int getStoredEnergy() {
        return data.get(0);
    }

    public int getMaxEnergy() {
        return data.get(1);
    }

    @Override
    public ItemStack quickMoveStack(final Player player, final int index) {
        ItemStack result = ItemStack.EMPTY;
        net.minecraft.world.inventory.Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stackInSlot = slot.getItem();
        result = stackInSlot.copy();
        if (index == 0) {
            if (!this.moveItemStackTo(stackInSlot, 1, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (stackInSlot.is(ItemRegistry.FERTILIZER.get())
                || stackInSlot.is(ItemRegistry.HYDRATION_CELL.get())
                || stackInSlot.is(ItemRegistry.WEED_EX.get())) {
            if (!this.moveItemStackTo(stackInSlot, 0, 1, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }

        if (stackInSlot.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return result;
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
                this.addSlot(new net.minecraft.world.inventory.Slot(
                        playerInventory,
                        col + row * 9 + 9,
                        8 + col * 18,
                        PLAYER_INVENTORY_START_Y + row * 18));
            }
        }
    }

    private void addPlayerHotbar(final Inventory playerInventory) {
        for (int col = 0; col < 9; col++) {
            this.addSlot(new net.minecraft.world.inventory.Slot(
                    playerInventory,
                    col,
                    8 + col * 18,
                    HOTBAR_Y));
        }
    }

    private static CropmatronBlockEntity getBlockEntity(
            final Inventory playerInventory,
            final FriendlyByteBuf extraData) {
        BlockEntity blockEntity = playerInventory.player.level().getBlockEntity(extraData.readBlockPos());
        if (blockEntity instanceof CropmatronBlockEntity cropmatron) {
            return cropmatron;
        }
        throw new IllegalStateException("Expected cropmatron block entity");
    }
}
